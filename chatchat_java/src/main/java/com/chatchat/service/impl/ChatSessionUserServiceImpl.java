package com.chatchat.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.chatchat.entity.dto.MessageSendDto;
import com.chatchat.entity.enums.MessageTypeEnum;
import com.chatchat.entity.enums.UserContactStatusEnum;
import com.chatchat.entity.enums.UserContactTypeEnum;
import com.chatchat.entity.po.UserContact;
import com.chatchat.entity.query.UserContactQuery;
import com.chatchat.mappers.UserContactMapper;
import com.chatchat.websocket.MessageHandler;
import org.springframework.stereotype.Service;

import com.chatchat.entity.enums.PageSize;
import com.chatchat.entity.query.ChatSessionUserQuery;
import com.chatchat.entity.po.ChatSessionUser;
import com.chatchat.entity.vo.PaginationResultVO;
import com.chatchat.entity.query.SimplePage;
import com.chatchat.mappers.ChatSessionUserMapper;
import com.chatchat.service.ChatSessionUserService;
import com.chatchat.utils.StringTools;


/**
 *  业务接口实现
 */
@Service("chatSessionUserService")
public class ChatSessionUserServiceImpl implements ChatSessionUserService {

	@Resource
	private ChatSessionUserMapper<ChatSessionUser, ChatSessionUserQuery> chatSessionUserMapper;
	@Resource
	private MessageHandler messageHandler;
	@Resource
	private UserContactMapper<UserContact,UserContactQuery> userContactMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ChatSessionUser> findListByParam(ChatSessionUserQuery param) {
		return this.chatSessionUserMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ChatSessionUserQuery param) {
		return this.chatSessionUserMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ChatSessionUser> findListByPage(ChatSessionUserQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ChatSessionUser> list = this.findListByParam(param);
		PaginationResultVO<ChatSessionUser> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ChatSessionUser bean) {
		return this.chatSessionUserMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ChatSessionUser> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatSessionUserMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ChatSessionUser> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.chatSessionUserMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(ChatSessionUser bean, ChatSessionUserQuery param) {
		StringTools.checkParam(param);
		return this.chatSessionUserMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(ChatSessionUserQuery param) {
		StringTools.checkParam(param);
		return this.chatSessionUserMapper.deleteByParam(param);
	}

	/**
	 * 根据UserIdAndContactId获取对象
	 */
	@Override
	public ChatSessionUser getChatSessionUserByUserIdAndContactId(String userId, String contactId) {
		return this.chatSessionUserMapper.selectByUserIdAndContactId(userId, contactId);
	}

	/**
	 * 根据UserIdAndContactId修改
	 */
	@Override
	public Integer updateChatSessionUserByUserIdAndContactId(ChatSessionUser bean, String userId, String contactId) {
		return this.chatSessionUserMapper.updateByUserIdAndContactId(bean, userId, contactId);
	}

	/**
	 * 根据UserIdAndContactId删除
	 */
	@Override
	public Integer deleteChatSessionUserByUserIdAndContactId(String userId, String contactId) {
		return this.chatSessionUserMapper.deleteByUserIdAndContactId(userId, contactId);
	}

	@Override
	public void updateContactName(String contactNameUpdate, String contactId) {
		ChatSessionUser updateSessionUser = new ChatSessionUser();
		updateSessionUser.setContactName(contactNameUpdate);
		ChatSessionUserQuery chatSessionUserQuery = new ChatSessionUserQuery();
		chatSessionUserQuery.setContactId(contactId);
		this.chatSessionUserMapper.updateByParam(updateSessionUser,chatSessionUserQuery);

		//修改群昵称，发送ws消息
		if(UserContactTypeEnum.GROUP.equals(UserContactTypeEnum.getByPrefix(contactId))){
			MessageSendDto messageSendDto = new MessageSendDto();
			messageSendDto.setContactType(UserContactTypeEnum.GROUP.getType());
			messageSendDto.setMessageType(MessageTypeEnum.GROUP_NAME_UPDATE.getType());
			messageSendDto.setContactId(contactId);
			messageSendDto.setExtendData(contactNameUpdate);
			messageHandler.sendMessage(messageSendDto);
		}else{
			UserContactQuery userContactQuery = new UserContactQuery();
			userContactQuery.setContactId(contactId);
			userContactQuery.setContactType(UserContactTypeEnum.USER.getType());
			userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			List<UserContact> userContactList=userContactMapper.selectList(userContactQuery);
			for(UserContact userContact:userContactList){
				MessageSendDto messageSendDto = new MessageSendDto();
				messageSendDto.setContactType(UserContactTypeEnum.USER.getType());
				messageSendDto.setMessageType(MessageTypeEnum.NICK_NAME_UPDATE.getType());
				messageSendDto.setContactId(userContact.getUserId());
				messageSendDto.setExtendData(contactNameUpdate);
				messageSendDto.setSendUserId(contactId);
				messageSendDto.setSendUserNickName(contactNameUpdate);
				messageHandler.sendMessage(messageSendDto);
			}
			}

	}


}