package com.chatchat.websocket;

import com.chatchat.constants.Constants;
import com.chatchat.entity.dto.MessageSendDto;
import com.chatchat.entity.dto.WSInitData;
import com.chatchat.entity.enums.MessageTypeEnum;
import com.chatchat.entity.enums.UserContactApplyStatusEnum;
import com.chatchat.entity.enums.UserContactTypeEnum;
import com.chatchat.entity.po.ChatMessage;
import com.chatchat.entity.po.ChatSessionUser;
import com.chatchat.entity.po.UserContactApply;
import com.chatchat.entity.po.UserInfo;
import com.chatchat.entity.query.ChatMessageQuery;
import com.chatchat.entity.query.ChatSessionUserQuery;
import com.chatchat.entity.query.UserContactApplyQuery;
import com.chatchat.entity.query.UserInfoQuery;
import com.chatchat.mappers.ChatMessageMapper;
import com.chatchat.mappers.ChatSessionUserMapper;
import com.chatchat.mappers.UserContactApplyMapper;
import com.chatchat.mappers.UserInfoMapper;
import com.chatchat.redis.RedisComponent;
import com.chatchat.service.ChatSessionUserService;
import com.chatchat.utils.JsonUtils;
import com.chatchat.utils.StringTools;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ChannelContextUtils {
    private static final Logger log = LoggerFactory.getLogger(ChannelContextUtils.class);

    private static final ConcurrentHashMap<String,Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private ChatSessionUserMapper<ChatSessionUser,ChatSessionUserQuery> chatSessionUserMapper;
    @Resource
    private ChatMessageMapper<ChatMessage,ChatMessageQuery> chatMessageMapper;
    @Resource
    private UserContactApplyMapper<UserContactApply,UserContactApplyQuery> userContactApplyMapper;


    public void addContext(String userId, Channel channel){
        String channelId = channel.id().toString();
        log.info("channelId:{}",channelId);
        AttributeKey<String> attributeKey = null;
        if (!AttributeKey.exists(channelId)) {
            attributeKey = AttributeKey.newInstance(channelId);
        }
        else{
            attributeKey = AttributeKey.valueOf(channelId);
        }
        channel.attr(attributeKey).set(userId);
        USER_CONTEXT_MAP.put(userId, channel);
        redisComponent.saveUserHeartBeat(userId);

        List<String> contactIdList=redisComponent.getUserContactList(userId);
        for(String contactId:contactIdList){
            if(contactId.startsWith(UserContactTypeEnum.GROUP.getPrefix())){
                add2Group(contactId,channel);
            }
        }

        //更新用户最后连接时间
        UserInfo updateInfo = new UserInfo();
        updateInfo.setLastLoginTime(new Date());
        userInfoMapper.updateByUserId(updateInfo,userId);

        //给用户发消息
        UserInfo userInfo=userInfoMapper.selectByUserId(userId);
        Long sourceLastOffTime=userInfo.getLastOffTime();
        Long lastOffTime=sourceLastOffTime;
        if(sourceLastOffTime==null){
            lastOffTime=System.currentTimeMillis()-Constants.MILLISECOND_3DAY_AGO;
        }
        // 查询用户所有的会话信息
        ChatSessionUserQuery chatSessionUserQuery=new ChatSessionUserQuery();
        chatSessionUserQuery.setUserId(userId);
        chatSessionUserQuery.setOrderBy("last_receive_time desc");
        List<ChatSessionUser> chatSessionUsers=chatSessionUserMapper.selectList(chatSessionUserQuery);
        WSInitData wsInitData=new WSInitData();
        wsInitData.setChatSessionUserList(chatSessionUsers);
        //查询聊天消息
        List<String> groupIdList=contactIdList.stream()
                .filter(item->item.startsWith(UserContactTypeEnum.GROUP.getPrefix()))
                .collect(Collectors.toList());
        groupIdList.add(userId);
        ChatMessageQuery messageQuery=new ChatMessageQuery();
        messageQuery.setLastReceiveTime(lastOffTime);
        messageQuery.setContactIdList(groupIdList);
        List<ChatMessage> chatMessages=chatMessageMapper.selectList(messageQuery);
        wsInitData.setMessageList(chatMessages);

        //查询好友申请
        UserContactApplyQuery applyQuery=new UserContactApplyQuery();
        applyQuery.setReceiveUserId(userId);
        applyQuery.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
        Integer applyCount=userContactApplyMapper.selectCount(applyQuery);
        wsInitData.setApplyCount(applyCount);

        MessageSendDto<WSInitData> messageSendDto= new MessageSendDto<>();
        messageSendDto.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDto.setExtendData(wsInitData);
        sendMsg(messageSendDto,userId);

    }

    /**
     * 把当前channel加入group中
     * @param groupId
     * @param channel
     */
    private void add2Group(String groupId,Channel channel){
        ChannelGroup group = GROUP_CONTEXT_MAP.get(groupId);
        if(group == null){
            group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId, group);
        }
        if(channel == null){
            return;
        }
        group.add(channel);
    }

    public void removeContext(Channel channel){
        Attribute<String> attribute=channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId=attribute.get();
        if(StringTools.isEmpty(userId)){
            USER_CONTEXT_MAP.remove(userId);
        }
        redisComponent.removeUserHeartBeat(userId);
        // 更新用户最后离线时间
        UserInfo updateInfo = new UserInfo();
        updateInfo.setLastOffTime(System.currentTimeMillis());
        userInfoMapper.updateByUserId(updateInfo,userId);
    }
    public void  sendMessage(MessageSendDto messageSendDto){
        UserContactTypeEnum contactTypeEnum=UserContactTypeEnum.getByPrefix(messageSendDto.getContactId());
        switch(contactTypeEnum){
            case GROUP:
                send2Group(messageSendDto);
                break;
            case USER:
                send2User(messageSendDto);
                break;
        }
    }
    public void send2User(MessageSendDto messageSendDto){
        String contactId=messageSendDto.getContactId();
        if(StringTools.isEmpty(contactId)){
            return;
        }
        sendMsg(messageSendDto,contactId);
        //强制下线
        if(MessageTypeEnum.FORCE_OFFlINE.getType().equals(messageSendDto.getMessageType())){
            closeContext(messageSendDto.getContactId());
        }
    }
    public void send2Group(MessageSendDto messageSendDto){
        if(StringTools.isEmpty(messageSendDto.getContactId())){
            return;
        }
        ChannelGroup channelGroup=GROUP_CONTEXT_MAP.get(messageSendDto.getContactId());
        if(channelGroup == null){
            return;
        }
        channelGroup.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));

        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(messageSendDto.getMessageType());
        if(MessageTypeEnum.LEAVE_GROUP.equals(messageTypeEnum)||MessageTypeEnum.REMOVE_GROUP.equals(messageTypeEnum)){
            String userId= (String) messageSendDto.getExtendData();
            redisComponent.removeUserContact(userId,messageSendDto.getContactId());
            Channel channel  = USER_CONTEXT_MAP.get(userId);
            if(channel == null) {
                return;
            }
            channelGroup.remove(channel);
        }
        if(MessageTypeEnum.DISSOLUTION_GROUP.equals(messageTypeEnum)){
            GROUP_CONTEXT_MAP.remove(messageSendDto.getContactId());
            channelGroup.close();
        }
    }
    /**
     * 发送消息
     */
    public  static void sendMsg(MessageSendDto messageSendDto,String receiverId){
        Channel channel = USER_CONTEXT_MAP.get(receiverId);
        if(channel==null){
            return;
        }
        if (MessageTypeEnum.ADD_FRIEND_SELF.getType().equals(messageSendDto.getMessageType())) {
            UserInfo userInfo= (UserInfo) messageSendDto.getExtendData();
            messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
            messageSendDto.setContactId(userInfo.getUserId());
            messageSendDto.setContactNickName(userInfo.getNickName());
            messageSendDto.setExtendData(null);
        }else {
            messageSendDto.setContactId(messageSendDto.getSendUserId());
            messageSendDto.setContactNickName(messageSendDto.getSendUserNickName());
        }
        channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));
    }

    public void closeContext(String userId){
        if(StringTools.isEmpty(userId)){
            return;
        }
        redisComponent.clearUserTokenByUserId(userId);
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if(channel==null){
            return;
        }
        channel.close();

    }

    public void addUser2Group(String userId, String groupId) {
        Channel channel = USER_CONTEXT_MAP.get(userId);
        add2Group(groupId, channel);
    }
}
