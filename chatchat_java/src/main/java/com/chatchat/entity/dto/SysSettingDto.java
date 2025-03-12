package com.chatchat.entity.dto;

import com.chatchat.constants.Constants;

import java.io.Serializable;

public class SysSettingDto implements Serializable {
    private Integer maxGroupCount=5;
    private Integer maxGroupMemberCount=500;
    private Integer maxImageSize =3;
    private Integer maxVideoSize =5;
    private Integer maxFileSize =3;
    private String robotUid= Constants.ROBOT_UID;
    private String robotNickname = "聊天助手";
    private String robotWelcome = "欢迎使用ChatChat";

    public Integer getMaxGroupMemberCount() {
        return maxGroupMemberCount;
    }

    public void setMaxGroupMemberCount(Integer maxGroupMemberCount) {
        this.maxGroupMemberCount = maxGroupMemberCount;
    }

    public Integer getMaxImageSize() {
        return maxImageSize;
    }

    public void setMaxImageSize(Integer maxImageSize) {
        this.maxImageSize = maxImageSize;
    }

    public Integer getMaxVideoSize() {
        return maxVideoSize;
    }

    public void setMaxVideoSize(Integer maxVideoSize) {
        this.maxVideoSize = maxVideoSize;
    }

    public Integer getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(Integer maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public String getRobotUid() {
        return robotUid;
    }

    public void setRobotUid(String robotUid) {
        this.robotUid = robotUid;
    }

    public String getRobotNickname() {
        return robotNickname;
    }

    public void setRobotNickname(String robotNickname) {
        this.robotNickname = robotNickname;
    }

    public String getRobotWelcome() {
        return robotWelcome;
    }

    public void setRobotWelcome(String robotWelcome) {
        this.robotWelcome = robotWelcome;
    }

    public Integer getMaxGroupCount() {
        return maxGroupCount;
    }

    public void setMaxGroupCount(Integer maxGroupCount) {
        this.maxGroupCount = maxGroupCount;
    }
}
