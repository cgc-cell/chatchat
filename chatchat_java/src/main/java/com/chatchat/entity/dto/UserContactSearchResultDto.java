package com.chatchat.entity.dto;

import com.chatchat.entity.enums.UserContactStatusEnum;

public class UserContactSearchResultDto {
    private String contactId;
    private String contactType;
    private String contactName;
    private  Integer status;
    private String statusName;
    private Integer sex;
    private String areaName;

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusName() {
        UserContactStatusEnum contactStatusEnum=UserContactStatusEnum.getByStatus(this.status);
        return contactStatusEnum==null?null:contactStatusEnum.name();
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}
