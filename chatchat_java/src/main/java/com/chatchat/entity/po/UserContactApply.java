package com.chatchat.entity.po;

import com.chatchat.entity.enums.UserContactApplyStatusEnum;

import java.io.Serializable;


/**
 * 
 */
public class UserContactApply implements Serializable {


	/**
	 * 自增id
	 */
	private Integer applyId;

	/**
	 * 申请人id
	 */
	private String applyUserId;

	/**
	 * 接收人id
	 */
	private String receiveUserId;

	/**
	 * 联系人类型，0：好友，1：群组
	 */
	private Integer contactType;

	/**
	 * 联系人群组id
	 */
	private String contactId;

	/**
	 * 最后申请时间
	 */
	private Long lastApplyTime;

	/**
	 * 状态，0：待处理，1：已同意，2：已拒绝
	 */
	private Integer status;

	/**
	 * 申请信息
	 */
	private String applyInfo;

	private String nickName;
	private String groupName;

	private String statusName;

	public String getStatusName() {
		UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
		return statusEnum==null?null:statusEnum.getDescription();
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}


	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public void setApplyId(Integer applyId){
		this.applyId = applyId;
	}

	public Integer getApplyId(){
		return this.applyId;
	}

	public void setApplyUserId(String applyUserId){
		this.applyUserId = applyUserId;
	}

	public String getApplyUserId(){
		return this.applyUserId;
	}

	public void setReceiveUserId(String receiveUserId){
		this.receiveUserId = receiveUserId;
	}

	public String getReceiveUserId(){
		return this.receiveUserId;
	}

	public void setContactType(Integer contactType){
		this.contactType = contactType;
	}

	public Integer getContactType(){
		return this.contactType;
	}

	public void setContactId(String contactId){
		this.contactId = contactId;
	}

	public String getContactId(){
		return this.contactId;
	}

	public void setLastApplyTime(Long lastApplyTime){
		this.lastApplyTime = lastApplyTime;
	}

	public Long getLastApplyTime(){
		return this.lastApplyTime;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setApplyInfo(String applyInfo){
		this.applyInfo = applyInfo;
	}

	public String getApplyInfo(){
		return this.applyInfo;
	}

	@Override
	public String toString (){
		return "自增id:"+(applyId == null ? "空" : applyId)+"，申请人id:"+(applyUserId == null ? "空" : applyUserId)+"，接收人id:"+(receiveUserId == null ? "空" : receiveUserId)+"，联系人类型，0：好友，1：群组:"+(contactType == null ? "空" : contactType)+"，联系人群组id:"+(contactId == null ? "空" : contactId)+"，最后申请时间:"+(lastApplyTime == null ? "空" : lastApplyTime)+"，状态，0：待处理，1：已同意，2：已拒绝:"+(status == null ? "空" : status)+"，申请信息:"+(applyInfo == null ? "空" : applyInfo);
	}
}
