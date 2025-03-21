package com.chatchat.entity.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;


/**
 * 会话消息
 */
public class ChatSession implements Serializable {


	/**
	 * 会话id
	 */
	private String sessionId;

	/**
	 * 最后接受的消息
	 */
	private String lastMessage;

	/**
	 * 最后接受消息时间（毫秒）
	 */
	private Long lastReceiveTime;


	public void setSessionId(String sessionId){
		this.sessionId = sessionId;
	}

	public String getSessionId(){
		return this.sessionId;
	}

	public void setLastMessage(String lastMessage){
		this.lastMessage = lastMessage;
	}

	public String getLastMessage(){
		return this.lastMessage;
	}

	public void setLastReceiveTime(Long lastReceiveTime){
		this.lastReceiveTime = lastReceiveTime;
	}

	public Long getLastReceiveTime(){
		return this.lastReceiveTime;
	}

	@Override
	public String toString (){
		return "会话id:"+(sessionId == null ? "空" : sessionId)+"，最后接受的消息:"+(lastMessage == null ? "空" : lastMessage)+"，最后接受消息时间（毫秒）:"+(lastReceiveTime == null ? "空" : lastReceiveTime);
	}
}
