package com.chatchat.service;

import java.io.File;
import java.util.List;

import com.chatchat.entity.dto.MessageSendDto;
import com.chatchat.entity.dto.TokenUserInfoDto;
import com.chatchat.entity.query.ChatMessageQuery;
import com.chatchat.entity.po.ChatMessage;
import com.chatchat.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;


/**
 *  业务接口
 */
public interface ChatMessageService {

	/**
	 * 根据条件查询列表
	 */
	List<ChatMessage> findListByParam(ChatMessageQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(ChatMessageQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ChatMessage> findListByPage(ChatMessageQuery param);

	/**
	 * 新增
	 */
	Integer add(ChatMessage bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<ChatMessage> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<ChatMessage> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(ChatMessage bean,ChatMessageQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(ChatMessageQuery param);

	/**
	 * 根据MessageId查询对象
	 */
	ChatMessage getChatMessageByMessageId(Long messageId);


	/**
	 * 根据MessageId修改
	 */
	Integer updateChatMessageByMessageId(ChatMessage bean,Long messageId);


	/**
	 * 根据MessageId删除
	 */
	Integer deleteChatMessageByMessageId(Long messageId);

    MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto);

	void saveMessageFile(String userId,Long messageId, MultipartFile file, MultipartFile cover);

	File downloadFile(TokenUserInfoDto tokenUserInfoDto,Long fileId,Boolean showCover);
}