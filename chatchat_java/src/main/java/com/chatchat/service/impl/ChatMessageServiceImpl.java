package com.chatchat.service.impl;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.chatchat.constants.Constants;
import com.chatchat.entity.config.AppConfig;
import com.chatchat.entity.dto.MessageSendDto;
import com.chatchat.entity.dto.SysSettingDto;
import com.chatchat.entity.dto.TokenUserInfoDto;
import com.chatchat.entity.enums.*;
import com.chatchat.entity.po.ChatSession;
import com.chatchat.entity.po.ChatSessionUser;
import com.chatchat.entity.po.UserContact;
import com.chatchat.entity.query.*;
import com.chatchat.exception.BusinessException;
import com.chatchat.mappers.ChatSessionMapper;
import com.chatchat.mappers.ChatSessionUserMapper;
import com.chatchat.mappers.UserContactMapper;
import com.chatchat.redis.RedisComponent;
import com.chatchat.utils.CopyTools;
import com.chatchat.utils.DateUtil;
import com.chatchat.websocket.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatchat.entity.po.ChatMessage;
import com.chatchat.entity.vo.PaginationResultVO;
import com.chatchat.mappers.ChatMessageMapper;
import com.chatchat.service.ChatMessageService;
import com.chatchat.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 *  业务接口实现
 */
@Service("chatMessageService")
public class ChatMessageServiceImpl implements ChatMessageService {

	private static final Logger log = LoggerFactory.getLogger(ChatMessageServiceImpl.class);
	@Resource
	private ChatMessageMapper<ChatMessage, ChatMessageQuery> chatMessageMapper;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;
    @Resource
    private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;

	@Resource
	private MessageHandler messageHandler;
	@Resource
	private AppConfig appConfig;
    @Autowired
    private UserContactMapper<UserContact,UserContactQuery> userContactMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ChatMessage> findListByParam(ChatMessageQuery param) {
		return this.chatMessageMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ChatMessageQuery param) {
		return this.chatMessageMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ChatMessage> findListByPage(ChatMessageQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ChatMessage> list = this.findListByParam(param);
		PaginationResultVO<ChatMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ChatMessage bean) {
		return this.chatMessageMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ChatMessage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatMessageMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ChatMessage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatMessageMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(ChatMessage bean, ChatMessageQuery param) {
		StringTools.checkParam(param);
		return this.chatMessageMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(ChatMessageQuery param) {
		StringTools.checkParam(param);
		return this.chatMessageMapper.deleteByParam(param);
	}

	/**
	 * 根据MessageId获取对象
	 */
	@Override
	public ChatMessage getChatMessageByMessageId(Long messageId) {
		return this.chatMessageMapper.selectByMessageId(messageId);
	}

	/**
	 * 根据MessageId修改
	 */
	@Override
	public Integer updateChatMessageByMessageId(ChatMessage bean, Long messageId) {
		return this.chatMessageMapper.updateByMessageId(bean, messageId);
	}

	/**
	 * 根据MessageId删除
	 */
	@Override
	public Integer deleteChatMessageByMessageId(Long messageId) {
		return this.chatMessageMapper.deleteByMessageId(messageId);
	}

    @Override
	@Transactional(rollbackFor = BusinessException.class)
    public MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) {
		//不是机器人，判断好友状态
		if(!Constants.ROBOT_UID.equals(tokenUserInfoDto.getUserId())){
			List<String> contactList = redisComponent.getUserContactList(tokenUserInfoDto.getUserId());
			if(!contactList.contains(chatMessage.getContactId())){
				UserContactTypeEnum userContactType=UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
				if(UserContactTypeEnum.USER.equals(userContactType)){
					throw new BusinessException(ResponseCodeEnum.CODE_902);
				}else {
					throw new BusinessException(ResponseCodeEnum.CODE_903);
				}
			}
		}
		String sessionId;
		String sendUserId=tokenUserInfoDto.getUserId();
		String contactId=chatMessage.getContactId();
		UserContactTypeEnum userContactType=UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
		if (UserContactTypeEnum.USER.equals(userContactType)) {
			sessionId=StringTools.getChatSessionId4User(new String[]{contactId,sendUserId});
		}else{
			sessionId=StringTools.getChatSessionId4Group(contactId);
		}
		chatMessage.setSessionId(sessionId);
		Long currentTime=System.currentTimeMillis();
		chatMessage.setSendTime(currentTime);
		MessageTypeEnum messageTypeEnum=MessageTypeEnum.getByType(chatMessage.getMessageType());
		if(messageTypeEnum==null|| !ArrayUtils.contains(new Integer[]{
				MessageTypeEnum.CHAT.getType(),MessageTypeEnum.MEDIA_CHAT.getType()
		},chatMessage.getMessageType())) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		Integer status=MessageTypeEnum.MEDIA_CHAT.equals(messageTypeEnum)?MessageStatusEnum.SENDING.getStatus():MessageStatusEnum.SENDED.getStatus();
		chatMessage.setStatus(status);

		String messageContent=StringTools.cleanHtmlTag(chatMessage.getMessageContent());
		chatMessage.setMessageContent(messageContent);
		//更新会话
		ChatSession chatSession = new ChatSession();
		chatSession.setLastMessage(messageContent);
		if (UserContactTypeEnum.GROUP.equals(userContactType)) {
			chatSession.setLastMessage(tokenUserInfoDto.getUserId()+":"+messageContent);
		}
		chatSession.setLastReceiveTime(currentTime);

		chatSessionMapper.updateBySessionId(chatSession,sessionId);

		//更新信息表
		chatMessage.setContactType(userContactType.getType());
		chatMessageMapper.insert(chatMessage);

		//发送ws消息
		MessageSendDto messageSendDto= CopyTools.copy(chatMessage,MessageSendDto.class);
		if(Constants.ROBOT_UID.equals(chatMessage.getContactId())){
			SysSettingDto sysSettingDto= redisComponent.getSysSetting();
			TokenUserInfoDto robot=new TokenUserInfoDto();
			robot.setUserId(sysSettingDto.getRobotUid());
			robot.setNickName(sysSettingDto.getRobotNickname());
			ChatMessage robotChatMessage=new ChatMessage();
			robotChatMessage.setContactId(sendUserId);
			// TODO ai 接口
			robotChatMessage.setMessageContent("我只是个机器人，不知道你在说什么~");
			robotChatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
			saveMessage(robotChatMessage,robot);
		}else{
			messageHandler.sendMessage(messageSendDto);
		}

        return messageSendDto;
    }

	@Override
	public void saveMessageFile(String userId, Long messageId, MultipartFile file, MultipartFile cover) {
		ChatMessage chatMessage=chatMessageMapper.selectByMessageId(messageId);
		if(chatMessage==null){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(!chatMessage.getSendUserId().equals(userId)){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}

		SysSettingDto sysSettingDto=redisComponent.getSysSetting();
		if(file.getSize()>sysSettingDto.getMaxFileSize()*Constants.FILE_SIZE_MB){
			throw new BusinessException("文件大小超过限制");
		}
		String fileName=file.getOriginalFilename();
		String fileExtName=StringTools.getFileSuffix(fileName);
		String fileRealName=messageId+fileExtName;
		String month = DateUtil.format(new Date(chatMessage.getSendTime()),DateTimePatternEnum.YYYY_MM.getPattern());
		File Folder=new File(appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+month);
		if(!Folder.exists()){
			Folder.mkdirs();
		}
		File uploadFile=new File(Folder.getPath()+"/"+fileRealName);
		try{
			file.transferTo(uploadFile);
			if(cover!=null){
				cover.transferTo(new File(Folder.getPath()+"/"+fileRealName+Constants.COVER_IMAGE_SUFFIX));
			}
		}catch (Exception e){
			log.error("上传文件失败",e);
			throw new BusinessException("上传文件失败");
		}
		ChatMessage updateMessage=new ChatMessage();
		updateMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
		ChatMessageQuery chatMessageQuery=new ChatMessageQuery();
		chatMessageQuery.setMessageId(chatMessage.getMessageId());
		chatMessageQuery.setStatus(MessageStatusEnum.SENDING.getStatus());
		chatMessageMapper.updateByParam(updateMessage,chatMessageQuery);

		MessageSendDto messageSendDto= new MessageSendDto();
		messageSendDto.setMessageType(MessageTypeEnum.FILE_UPLOADED.getType());
		messageSendDto.setMessageId(chatMessage.getMessageId());
		messageSendDto.setStatus(MessageStatusEnum.SENDED.getStatus());
		messageSendDto.setContactId(chatMessage.getContactId());
		messageSendDto.setMessageContent(MessageTypeEnum.FILE_UPLOADED.getDescription());
		messageHandler.sendMessage(messageSendDto);

	}

	@Override
	public File downloadFile(TokenUserInfoDto userInfoDto, Long fileId, Boolean showCover) {
		ChatMessage chatMessage = chatMessageMapper.selectByMessageId(fileId);
		String contactId=chatMessage.getContactId();
		UserContactTypeEnum contactTypeEnum=UserContactTypeEnum.getByPrefix(contactId);
		if(contactTypeEnum==UserContactTypeEnum.USER&&!userInfoDto.getUserId().equals(contactId)){
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		if(contactTypeEnum==UserContactTypeEnum.GROUP){
			UserContactQuery userContactQuery=new UserContactQuery();
			userContactQuery.setUserId(userInfoDto.getUserId());
			userContactQuery.setContactId(contactId);
			userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			Integer count=userContactMapper.selectCount(userContactQuery);
			if(count<1){
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
		}
		String month=DateUtil.format(new Date(chatMessage.getSendTime()),DateTimePatternEnum.YYYY_MM.getPattern());
		File Folder=new File(appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+month);
		if(!Folder.exists()){
			Folder.mkdirs();
		}
		String fileName=chatMessage.getFileName();
		String fileExtName=StringTools.getFileSuffix(fileName);
		String fileRealName=chatMessage.getMessageId()+fileExtName;
		if(showCover!=null&&showCover){
			fileRealName= fileRealName+Constants.COVER_IMAGE_SUFFIX;
		}
		File file=new File(Folder.getPath()+"/"+fileRealName);
		if(!file.exists()){
			log.error("文件不存在，messageId:{}",fileId);
			throw new BusinessException(ResponseCodeEnum.CODE_602);
		}

		return file;
	}
}