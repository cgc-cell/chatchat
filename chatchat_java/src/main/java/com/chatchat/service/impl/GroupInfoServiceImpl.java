package com.chatchat.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.chatchat.constants.Constants;
import com.chatchat.entity.config.AppConfig;
import com.chatchat.entity.dto.MessageSendDto;
import com.chatchat.entity.dto.SysSettingDto;
import com.chatchat.entity.enums.*;
import com.chatchat.entity.po.*;
import com.chatchat.entity.query.*;
import com.chatchat.exception.BusinessException;
import com.chatchat.mappers.*;
import com.chatchat.redis.RedisComponent;
import com.chatchat.service.ChatSessionUserService;
import com.chatchat.utils.CopyTools;
import com.chatchat.websocket.ChannelContextUtils;
import com.chatchat.websocket.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatchat.entity.vo.PaginationResultVO;
import com.chatchat.service.GroupInfoService;
import com.chatchat.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 *  业务接口实现
 */
@Service("groupInfoService")
public class GroupInfoServiceImpl implements GroupInfoService {

	@Resource
	private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

	@Resource
	private AppConfig appConfig;
	@Resource
	private ChatSessionMapper<ChatSession, ChatSessionQuery> chatSessionMapper;
	@Resource
	private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;
	@Resource
	private ChatMessageMapper<ChatMessage,ChatMessageQuery> chatMessageMapper;
	@Autowired
	private MessageHandler messageHandler;

	@Resource
	private ChannelContextUtils channelContextUtils;
	@Resource
	private ChatSessionUserService chatSessionUserService;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<GroupInfo> findListByParam(GroupInfoQuery param) {
		return this.groupInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(GroupInfoQuery param) {
		return this.groupInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<GroupInfo> findListByPage(GroupInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<GroupInfo> list = this.findListByParam(param);
		PaginationResultVO<GroupInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(GroupInfo bean) {
		return this.groupInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<GroupInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.groupInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<GroupInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.groupInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(GroupInfo bean, GroupInfoQuery param) {
		StringTools.checkParam(param);
		return this.groupInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(GroupInfoQuery param) {
		StringTools.checkParam(param);
		return this.groupInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据GroupId获取对象
	 */
	@Override
	public GroupInfo getGroupInfoByGroupId(String groupId) {
		return this.groupInfoMapper.selectByGroupId(groupId);
	}

	/**
	 * 根据GroupId修改
	 */
	@Override
	public Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId) {
		return this.groupInfoMapper.updateByGroupId(bean, groupId);
	}

	/**
	 * 根据GroupId删除
	 */
	@Override
	public Integer deleteGroupInfoByGroupId(String groupId) {
		return this.groupInfoMapper.deleteByGroupId(groupId);
	}

	/**
	 * 创建或者更新群组信息
	 * @param groupInfo
	 * @param avatarFile
	 * @param avatarCover
	 * @throws IOException
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
		Date currentDate = new Date();
		if(StringTools.isEmpty(groupInfo.getGroupId())){
			GroupInfoQuery query = new GroupInfoQuery();
			query.setGroupOwnerId(groupInfo.getGroupOwnerId());
			Integer count=this.groupInfoMapper.selectCount(query);
			SysSettingDto sysSettingDto= redisComponent.getSysSetting();
			if(count>=sysSettingDto.getMaxGroupCount()){
				throw new BusinessException("最多只支持创建"+sysSettingDto.getMaxGroupCount()+"个群组");
			}
			if(null ==avatarFile){
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
			groupInfo.setCreateTime(currentDate);
			groupInfo.setGroupId(StringTools.getGroupId());
			groupInfo.setStatus(GroupStatusEnum.NORMAL.getStatus());
			this.groupInfoMapper.insert(groupInfo);

			//将群组添加为联系人
			UserContact userContact = new UserContact();
			userContact.setUserId(groupInfo.getGroupOwnerId());
			userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			userContact.setContactType(UserContactTypeEnum.GROUP.getType());
			userContact.setContactId(groupInfo.getGroupId());
			userContact.setCreateTime(currentDate);
			userContact.setLastUpdateTime(currentDate);

			this.userContactMapper.insert(userContact);

			//创建会话
			String sessionId=StringTools.getChatSessionId4Group(groupInfo.getGroupId());
			ChatSession chatSession = new ChatSession();
			chatSession.setSessionId(sessionId);
			chatSession.setLastReceiveTime(currentDate.getTime());
			chatSession.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
			this.chatSessionMapper.insertOrUpdate(chatSession);

			ChatSessionUser chatSessionUser = new ChatSessionUser();
			chatSessionUser.setUserId(groupInfo.getGroupOwnerId());
			chatSessionUser.setContactId(groupInfo.getGroupId());
			chatSessionUser.setContactName(groupInfo.getGroupName());
			chatSessionUser.setSessionId(sessionId);
			this.chatSessionUserMapper.insertOrUpdate(chatSessionUser);

			//创建消息
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setSessionId(sessionId);
			chatMessage.setMessageType(MessageTypeEnum.GROUP_CREATE.getType());
			chatMessage.setMessageContent(MessageTypeEnum.GROUP_CREATE.getInitMessage());
			chatMessage.setSendTime(currentDate.getTime());
			chatMessage.setContactId(groupInfo.getGroupId());
			chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
			chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
			this.chatMessageMapper.insertOrUpdate(chatMessage);
			//将群组添加到联系人
			redisComponent.addUserContact(groupInfo.getGroupOwnerId(),groupInfo.getGroupId());
			//将联系人的channel添加到群组
			channelContextUtils.addUser2Group(groupInfo.getGroupOwnerId(),groupInfo.getGroupId());

			//发送ws消息
			chatSessionUser.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
			chatSessionUser.setLastReceiveTime(currentDate.getTime());
			chatSessionUser.setMemberCount(1);
			MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
			messageSendDto.setExtendData(chatSessionUser);
			messageSendDto.setMessageContent(chatSessionUser.getLastMessage());
			messageHandler.sendMessage(messageSendDto);

		}else{
			GroupInfo dbInfo = this.groupInfoMapper.selectByGroupId(groupInfo.getGroupId());
			if (!dbInfo.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())) {
				throw new BusinessException(ResponseCodeEnum.CODE_600);
			}
			this.groupInfoMapper.updateByGroupId(groupInfo,groupInfo.getGroupId());
			// 更新相关冗余信息
			if(!dbInfo.getGroupName().equals(groupInfo.getGroupName())){
				String contactNameUpdate =groupInfo.getGroupName();

				this.chatSessionUserService.updateContactName(contactNameUpdate,groupInfo.getGroupId());
			}

		}
		if (avatarFile == null) {
			return;
		}
		String baseDir = appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE;
		File targetFileFolder = new File(baseDir+Constants.FILE_FOLDER_AVATAR);
		if (!targetFileFolder.exists()) {
			targetFileFolder.mkdirs();
		}
		String filePath = targetFileFolder.getPath()+"/"+groupInfo.getGroupId()+Constants.IMAGE_SUFFIX;
		avatarFile.transferTo(new File(filePath));
		avatarCover.transferTo(new File(filePath+Constants.COVER_IMAGE_SUFFIX));
	}

    @Override
	public void dissolutionGroup(String groupId, String groupOwnerId) {
		GroupInfo dbGroup=this.groupInfoMapper.selectByGroupId(groupId);
		if (dbGroup==null||!dbGroup.getGroupOwnerId().equals(groupOwnerId)) {
			throw new BusinessException(ResponseCodeEnum.CODE_600);
		}
		GroupInfo updateGroup=new GroupInfo();
		updateGroup.setStatus(GroupStatusEnum.DISABLE.getStatus());
		this.groupInfoMapper.updateByGroupId(updateGroup,groupId);

		UserContactQuery userContactQuery=new UserContactQuery();
		userContactQuery.setContactId(groupId);
		userContactQuery.setContactType(UserContactTypeEnum.GROUP.getType());

		UserContact updateContact =new UserContact();
		updateContact.setStatus(UserContactStatusEnum.DEL.getStatus());
		this.userContactMapper.updateByParam(updateContact,userContactQuery);

		// TODO 移除相关联系人的缓存

		// TODO 发信息 1更新会话信息，2记录群消息，3发送解散消息

    }
}