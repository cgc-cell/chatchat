package com.chatchat.entity.vo;

import com.chatchat.entity.enums.DateTimePatternEnum;
import com.chatchat.utils.DateUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户信息表
 */
public class UserInfoVO implements Serializable {


	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 昵称
	 */
	private String nickName;

	/**
	 * 可添加类型，0：直接加，1：同意后加好友
	 */
	private Integer joinType;

	/**
	 * 性别，0：女，1：男
	 */
	private Integer sex;

	/**
	 * 个性签名
	 */
	private String personalSignature;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 最后登录时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date lastLoginTime;

	/**
	 * 地区
	 */
	private String areaName;

	/**
	 * 地区编号
	 */
	private String areaCode;

	/**
	 * 最后离线时间
	 */
	private Long lastOffTime;

	private String token;
	private Boolean admin;
	private Integer contactStatus;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

	public Integer getContactStatus() {
		return contactStatus;
	}

	public void setContactStatus(Integer contactStatus) {
		this.contactStatus = contactStatus;
	}

	public void setUserId(String userId){
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return this.email;
	}

	public void setNickName(String nickName){
		this.nickName = nickName;
	}

	public String getNickName(){
		return this.nickName;
	}

	public void setJoinType(Integer joinType){
		this.joinType = joinType;
	}

	public Integer getJoinType(){
		return this.joinType;
	}

	public void setSex(Integer sex){
		this.sex = sex;
	}

	public Integer getSex(){
		return this.sex;
	}

	public void setPersonalSignature(String personalSignature){
		this.personalSignature = personalSignature;
	}

	public String getPersonalSignature(){
		return this.personalSignature;
	}

	public void setCreateTime(Date createTime){
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	public void setLastLoginTime(Date lastLoginTime){
		this.lastLoginTime = lastLoginTime;
	}

	public Date getLastLoginTime(){
		return this.lastLoginTime;
	}

	public void setAreaName(String areaName){
		this.areaName = areaName;
	}

	public String getAreaName(){
		return this.areaName;
	}

	public void setAreaCode(String areaCode){
		this.areaCode = areaCode;
	}

	public String getAreaCode(){
		return this.areaCode;
	}

	public void setLastOffTime(Long lastOffTime){
		this.lastOffTime = lastOffTime;
	}

	public Long getLastOffTime(){
		return this.lastOffTime;
	}

	@Override
	public String toString (){
		return "用户id:"+(userId == null ? "空" : userId)+"，邮箱:"+(email == null ? "空" : email)+"，昵称:"+(nickName == null ? "空" : nickName)+"，可添加类型，0：直接加，1：同意后加好友:"+(joinType == null ? "空" : joinType)+"，性别，0：女，1：男:"+(sex == null ? "空" : sex)+"，个性签名:"+(personalSignature == null ? "空" : personalSignature)+"，创建时间:"+(createTime == null ? "空" : DateUtil.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，最后登录时间:"+(lastLoginTime == null ? "空" : DateUtil.format(lastLoginTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern()))+"，地区:"+(areaName == null ? "空" : areaName)+"，地区编号:"+(areaCode == null ? "空" : areaCode)+"，最后离线时间:"+(lastOffTime == null ? "空" : lastOffTime);
	}
}
