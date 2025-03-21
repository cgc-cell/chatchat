package com.chatchat.entity.query;



/**
 * 参数
 */
public class ChatSessionUserQuery extends BaseParam {


	/**
	 * 用户id
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 联系人id
	 */
	private String contactId;

	private String contactIdFuzzy;

	/**
	 * 会话id
	 */
	private String sessionId;

	private String sessionIdFuzzy;

	/**
	 * 会话名称
	 */
	private String contactName;

	private String contactNameFuzzy;


	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setUserIdFuzzy(String userIdFuzzy){
		this.userIdFuzzy = userIdFuzzy;
	}

	public String getUserIdFuzzy(){
		return this.userIdFuzzy;
	}

	public void setContactId(String contactId){
		this.contactId = contactId;
	}

	public String getContactId(){
		return this.contactId;
	}

	public void setContactIdFuzzy(String contactIdFuzzy){
		this.contactIdFuzzy = contactIdFuzzy;
	}

	public String getContactIdFuzzy(){
		return this.contactIdFuzzy;
	}

	public void setSessionId(String sessionId){
		this.sessionId = sessionId;
	}

	public String getSessionId(){
		return this.sessionId;
	}

	public void setSessionIdFuzzy(String sessionIdFuzzy){
		this.sessionIdFuzzy = sessionIdFuzzy;
	}

	public String getSessionIdFuzzy(){
		return this.sessionIdFuzzy;
	}

	public void setContactName(String contactName){
		this.contactName = contactName;
	}

	public String getContactName(){
		return this.contactName;
	}

	public void setContactNameFuzzy(String contactNameFuzzy){
		this.contactNameFuzzy = contactNameFuzzy;
	}

	public String getContactNameFuzzy(){
		return this.contactNameFuzzy;
	}

}
