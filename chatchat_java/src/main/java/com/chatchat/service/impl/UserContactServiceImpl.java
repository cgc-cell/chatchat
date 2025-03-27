package com.chatchat.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.chatchat.constants.Constants;
import com.chatchat.entity.dto.MessageSendDto;
import com.chatchat.entity.dto.SysSettingDto;
import com.chatchat.entity.dto.TokenUserInfoDto;
import com.chatchat.entity.dto.UserContactSearchResultDto;
import com.chatchat.entity.enums.*;
import com.chatchat.entity.po.*;
import com.chatchat.entity.query.*;
import com.chatchat.exception.BusinessException;
import com.chatchat.mappers.*;
import com.chatchat.redis.RedisComponent;
import com.chatchat.utils.CopyTools;
import com.chatchat.websocket.ChannelContextUtils;
import com.chatchat.websocket.MessageHandler;
import jodd.util.ArraysUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatchat.entity.vo.PaginationResultVO;
import com.chatchat.service.UserContactService;
import com.chatchat.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 *  业务接口实现
 */
@Service("userContactService")
public class UserContactServiceImpl implements UserContactService {

	@Resource
	private UserContactMapper<UserContact, UserContactQuery> userContactMapper;
	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
	@Resource
	private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;
	@Resource
	private UserContactApplyMapper<UserContactApply, UserContactApplyQuery> userContactApplyMapper;
	@Resource
	private RedisComponent redisComponent;
	@Resource
	private ChatSessionMapper<ChatSession,ChatSessionQuery> chatSessionMapper;
	@Resource
	private ChatSessionUserMapper<ChatSessionUser,ChatSessionUserQuery> chatSessionUserMapper;
	@Resource
	private ChatMessageMapper<ChatMessage,ChatMessageQuery> chatMessageMapper;
    @Autowired
    private MessageHandler messageHandler;
	@Resource
	private ChannelContextUtils channelContextUtils;


	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserContact> findListByParam(UserContactQuery param) {
		return this.userContactMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserContactQuery param) {
		return this.userContactMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserContact> findListByPage(UserContactQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserContact> list = this.findListByParam(param);
		PaginationResultVO<UserContact> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserContact bean) {
		return this.userContactMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserContact> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userContactMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserContact> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userContactMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserContact bean, UserContactQuery param) {
		StringTools.checkParam(param);
		return this.userContactMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserContactQuery param) {
		StringTools.checkParam(param);
		return this.userContactMapper.deleteByParam(param);
	}

	/**
	 * 根据UserIdAndContactId获取对象
	 */
	@Override
	public UserContact getUserContactByUserIdAndContactId(String userId, String contactId) {
		return this.userContactMapper.selectByUserIdAndContactId(userId, contactId);
	}

	/**
	 * 根据UserIdAndContactId修改
	 */
	@Override
	public Integer updateUserContactByUserIdAndContactId(UserContact bean, String userId, String contactId) {
		return this.userContactMapper.updateByUserIdAndContactId(bean, userId, contactId);
	}

	/**
	 * 根据UserIdAndContactId删除
	 */
	@Override
	public Integer deleteUserContactByUserIdAndContactId(String userId, String contactId) {
		return this.userContactMapper.deleteByUserIdAndContactId(userId, contactId);
	}

	@Override
	public UserContactSearchResultDto searchContact(String userId, String contactId) {
		UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);
		if(typeEnum == null) {
			return null;
		}
		UserContactSearchResultDto dto = new UserContactSearchResultDto();
		switch (typeEnum){
			case USER:
				UserInfo userInfo = userInfoMapper.selectByUserId(contactId);
				if(userInfo == null) {
					return null;
				}
				dto= CopyTools.copy(userInfo,UserContactSearchResultDto.class);
				break;
			case GROUP:
				GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
				if(groupInfo == null) {
					return null;
				}
				dto.setContactName(groupInfo.getGroupName());
				break;
		}
		dto.setContactType(typeEnum.toString());
		dto.setContactId(contactId);
		if(userId.equals(contactId)){
			dto.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			return dto;
		}
		//查询是否是好友
		UserContact userContact = userContactMapper.selectByUserIdAndContactId(userId, contactId);
		dto.setStatus(userContact==null?UserContactStatusEnum.NOT_FRIEND.getStatus():userContact.getStatus());
		return dto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer applyAdd(TokenUserInfoDto userInfoDto, String contactId, String applyInfo) {
		UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);
		if(typeEnum == null) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		String applyUserId=userInfoDto.getUserId();
		//默认申请信息
		applyInfo=StringTools.isEmpty(applyInfo)? String.format(Constants.APPLY_INFO_TEMPLATE,userInfoDto.getNickName()) :applyInfo;
		Long curTime = System.currentTimeMillis();
		Integer joinType = null;
		String receiveUserId = contactId;
		//查询对方好友是否已经添加，如果对方已经拉黑则无法添加
		UserContact userContact = userContactMapper.selectByUserIdAndContactId(applyUserId,contactId);
		if(userContact != null && ArraysUtil.contains(new Integer[]{
				UserContactStatusEnum.BE_BLACKLIST.getStatus(),
				UserContactStatusEnum.BE_BLACKLIST_BEFORE_ADD.getStatus(),
		},userContact.getStatus())) {
			throw new BusinessException("你已经被拉黑，无法添加");
		}
		if(UserContactTypeEnum.GROUP==typeEnum) {
			GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
			if(groupInfo == null || groupInfo.getStatus().equals(GroupStatusEnum.DISABLE.getStatus())) {
				throw new BusinessException("该群组不存在或已解散");
			}
			receiveUserId=groupInfo.getGroupOwnerId();
			joinType=groupInfo.getJoinType();

		}
		else{
			UserInfo userInfo = userInfoMapper.selectByUserId(contactId);
			if(userInfo == null) {
				throw new BusinessException("该用户不存在");
			}
			joinType = userInfo.getJoinType();
		}

		if(joinType.equals(JoinTypeEnum.JOIN.getType())){
			this.addContact(applyUserId,receiveUserId,contactId,typeEnum.getType(),applyInfo);
			return joinType;
		}

		UserContactApply dbUserContactApply = this.userContactApplyMapper.selectByApplyUserIdAndReceiveUserIdAndContactId(applyUserId,receiveUserId,contactId);
		if(dbUserContactApply == null) {
			UserContactApply userContactApply = new UserContactApply();
			userContactApply.setApplyUserId(applyUserId);
			userContactApply.setReceiveUserId(receiveUserId);
			userContactApply.setContactId(contactId);
			userContactApply.setContactType(typeEnum.getType());
			userContactApply.setLastApplyTime(curTime);
			userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
			userContactApply.setApplyInfo(applyInfo);
			this.userContactApplyMapper.insert(userContactApply);
		}else{
			UserContactApply  userContactApply = new UserContactApply();
			userContactApply.setLastApplyTime(curTime);
			userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
			userContactApply.setApplyInfo(applyInfo);
			this.userContactApplyMapper.updateByApplyId(userContactApply, dbUserContactApply.getApplyId());
		}

		if(dbUserContactApply==null ||!dbUserContactApply.getStatus().equals(UserContactApplyStatusEnum.INIT.getStatus())){
			// 发送ws消息
			MessageSendDto messageSendDto=new MessageSendDto();
			messageSendDto.setMessageType(MessageTypeEnum.CONTACT_APPLY.getType());
			messageSendDto.setMessageContent(applyInfo);
			messageSendDto.setContactId(receiveUserId);
			messageHandler.sendMessage(messageSendDto);
		}

		return joinType;
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) {
		SysSettingDto sysSettingDto = redisComponent.getSysSetting();
		// 查看群组人数
		if(contactType.equals(UserContactTypeEnum.GROUP.getType())) {
			UserContactQuery contactQuery = new UserContactQuery();
			contactQuery.setContactId(contactId);
			contactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			Integer memberCount = userContactMapper.selectCount(contactQuery);
			if(memberCount >= sysSettingDto.getMaxGroupMemberCount()) {
				throw new BusinessException("成员已满，无法加入");
			}
		}
		Date currentTime=new Date();
		// 同意，双方添加好友
		List<UserContact> contactList = new ArrayList<UserContact>();
		//申请人添加对方为好友
		UserContact userContact = new UserContact();
		userContact.setContactId(contactId);
		userContact.setUserId(applyUserId);
		userContact.setContactType(contactType);
		userContact.setCreateTime(currentTime);
		userContact.setLastUpdateTime(currentTime);
		userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
		contactList.add(userContact);
		//如果对方是用户，对方添加申请人为好友，如果是群组，不用添加
		if(contactType.equals(UserContactTypeEnum.USER.getType())) {
			userContact=new UserContact();
			userContact.setContactId(applyUserId);
			userContact.setUserId(contactId);
			userContact.setContactType(contactType);
			userContact.setCreateTime(currentTime);
			userContact.setLastUpdateTime(currentTime);
			userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			contactList.add(userContact);

		}
		userContactMapper.insertOrUpdateBatch(contactList);
		// 接受人也添加申请人为好友，添加缓存
		if (UserContactTypeEnum.USER.getType().equals(contactType)) {
			redisComponent.addUserContact(receiveUserId,applyUserId);
		}
		redisComponent.addUserContact(applyUserId,contactId);

		// 创建会话，发送消息
		String sessionId=null;
		if(UserContactTypeEnum.USER.getType().equals(contactType)) {
			sessionId=StringTools.getChatSessionId4User(new String[]{applyUserId,contactId});
		}else{
			sessionId=StringTools.getChatSessionId4Group(contactId);
		}
		List<ChatSessionUser> chatSessionUserList= new ArrayList<>();
		if(UserContactTypeEnum.USER.getType().equals(contactType)) {
			ChatSession chatSession=new ChatSession();
			chatSession.setSessionId(sessionId);
			chatSession.setLastMessage(applyInfo);
			chatSession.setLastReceiveTime(currentTime.getTime());
			this.chatSessionMapper.insertOrUpdate(chatSession);
			ChatSessionUser applySessionUser =new ChatSessionUser();
			applySessionUser.setUserId(applyUserId);
			applySessionUser.setSessionId(sessionId);
			applySessionUser.setContactId(contactId);
			UserInfo receiveUserInfo =this.userInfoMapper.selectByUserId(contactId);
			applySessionUser.setContactName(receiveUserInfo.getNickName());
			this.chatSessionUserMapper.insertOrUpdate(applySessionUser);

			ChatSessionUser receiveSessionUser =new ChatSessionUser();
			receiveSessionUser.setUserId(receiveUserId);
			receiveSessionUser.setSessionId(sessionId);
			receiveSessionUser.setContactId(applyUserId);

			UserInfo applyUserInfo =this.userInfoMapper.selectByUserId(applyUserId);
			receiveSessionUser.setContactName(applyUserInfo.getNickName());
			this.chatSessionUserMapper.insertOrUpdate(receiveSessionUser);

			// 记录消息表
			ChatMessage chatMessage=new ChatMessage();
			chatMessage.setSessionId(sessionId);
			chatMessage.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
			chatMessage.setContactId(contactId);
			chatMessage.setMessageContent(applyInfo);
			chatMessage.setSendUserId(applyUserId);
			chatMessage.setSendTime(currentTime.getTime());
			chatMessage.setSendUserNickName(applyUserInfo.getNickName());
			chatMessage.setContactType(contactType);
			chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
			this.chatMessageMapper.insertOrUpdate(chatMessage);

			MessageSendDto messageSendDto=CopyTools.copy(chatMessage,MessageSendDto.class);
			// 发送给接受申请的人
			messageHandler.sendMessage(messageSendDto);
			// 发送给申请人
			messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND_SELF.getType());
			messageSendDto.setContactId(applyUserId);
			messageSendDto.setExtendData(receiveUserInfo);
			messageHandler.sendMessage(messageSendDto);
		}
		else{
			//加入群组
			ChatSessionUser chatSessionUser =new ChatSessionUser();
			chatSessionUser.setUserId(applyUserId);
			chatSessionUser.setSessionId(sessionId);
			chatSessionUser.setContactId(contactId);
			GroupInfo groupInfo=groupInfoMapper.selectByGroupId(contactId);
			chatSessionUser.setContactName(groupInfo.getGroupName());
			this.chatSessionUserMapper.insertOrUpdate(chatSessionUser);

			UserInfo applyUserInfo =this.userInfoMapper.selectByUserId(applyUserId);
			String addMessage= String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(),applyUserInfo.getNickName());

			ChatSession chatSession=new ChatSession();
			chatSession.setSessionId(sessionId);
			chatSession.setLastMessage(addMessage);
			chatSession.setLastReceiveTime(currentTime.getTime());
			this.chatSessionMapper.insertOrUpdate(chatSession);

			ChatMessage chatMessage=new ChatMessage();
			chatMessage.setSessionId(sessionId);
			chatMessage.setMessageType(MessageTypeEnum.ADD_GROUP.getType());
			chatMessage.setMessageContent(addMessage);
			chatMessage.setSendTime(currentTime.getTime());
			chatMessage.setContactId(contactId);
			chatMessage.setContactType(contactType);
			chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
			this.chatMessageMapper.insertOrUpdate(chatMessage);


			redisComponent.addUserContact(applyUserId,groupInfo.getGroupId());
			//将联系人的channel添加到群组
			channelContextUtils.addUser2Group(applyUserId,groupInfo.getGroupId());

			MessageSendDto messageSendDto=CopyTools.copy(chatMessage,MessageSendDto.class);
			UserContactQuery userContactQuery=new UserContactQuery();
			userContactQuery.setContactId(contactId);
			userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			Integer memberCount=userContactMapper.selectCount(userContactQuery);
			messageSendDto.setMemberCount(memberCount);
			messageSendDto.setContactNickName(groupInfo.getGroupName());
			messageHandler.sendMessage(messageSendDto);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeUserContact(String userId, String contactId, UserContactStatusEnum contactStatus) {
		//移除好友
		UserContact userContact = new UserContact();
		userContact.setStatus(contactStatus.getStatus());
		userContactMapper.updateByUserIdAndContactId(userContact,userId,contactId);

		UserContact friendContact = new UserContact();
		if(contactStatus.equals(UserContactStatusEnum.DEL)) {
			friendContact.setStatus(UserContactStatusEnum.BE_DEL.getStatus());
		}else if(contactStatus.equals(UserContactStatusEnum.BLACKLIST)) {
			friendContact.setStatus(UserContactStatusEnum.BE_BLACKLIST.getStatus());
		}
		userContactMapper.updateByUserIdAndContactId(friendContact,contactId,userId);

		// 从我的列表缓存中删除好友
		redisComponent.removeUserContact(userId,contactId);
		// 从好友的列表缓存中删除我
		redisComponent.removeUserContact(contactId,userId);

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addContact2Robot(String userID) {
		Date currentTime=new Date();
		SysSettingDto sysSettingDto = redisComponent.getSysSetting();
		String contactId = sysSettingDto.getRobotUid();
		String contactName=sysSettingDto.getRobotNickname();
		String sendMsg=sysSettingDto.getRobotWelcome();
		sendMsg=StringTools.cleanHtmlTag(sendMsg);
		//添加机器人为好友
		UserContact userContact = new UserContact();
		userContact.setUserId(userID);
		userContact.setContactId(contactId);
		userContact.setContactType(UserContactTypeEnum.USER.getType());
		userContact.setCreateTime(currentTime);
		userContact.setLastUpdateTime(currentTime);
		userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
		userContactMapper.insertOrUpdate(userContact);
		//增加会话信息
		String sessionId=StringTools.getChatSessionId4User(new String[]{userID,contactId});
		ChatSession chatSession = new ChatSession();
		chatSession.setSessionId(sessionId);
		chatSession.setLastMessage(sendMsg);
		chatSession.setLastReceiveTime(currentTime.getTime());
		this.chatSessionMapper.insertOrUpdate(chatSession);

		ChatSessionUser chatSessionUser = new ChatSessionUser();
		chatSessionUser.setSessionId(sessionId);
		chatSessionUser.setContactId(contactId);
		chatSessionUser.setContactName(contactName);
		chatSessionUser.setUserId(userID);
		this.chatSessionUserMapper.insertOrUpdate(chatSessionUser);

		ChatMessage chatMessage = new ChatMessage();
		chatMessage.setSessionId(sessionId);
		chatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
		chatMessage.setMessageContent(sendMsg);
		chatMessage.setSendUserId(contactId);
		chatMessage.setSendUserNickName(contactName);
		chatMessage.setSendTime(currentTime.getTime());
		chatMessage.setContactId(userID);
		chatMessage.setContactType(UserContactTypeEnum.USER.getType());
		chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
		chatMessageMapper.insertOrUpdate(chatMessage);

	}
}