package com.chatchat.entity.enums;

public enum UserContactStatusEnum {

    NOT_FRIEND(0,"非好友"),
    FRIEND(1,"好友"),
    DEL(2,"已删除好友"),
    BE_DEL(3,"已被好友删除"),
    BLACKLIST(4,"已拉黑好友"),
    BE_BLACKLIST(5,"已被好友拉黑"),
    BE_BLACKLIST_BEFORE_ADD(6,"在对方添加之前已被好友拉黑");
    private Integer status;
    private String description;
    UserContactStatusEnum(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
    public static UserContactStatusEnum getByStatus(Integer status) {
        try {
            for (UserContactStatusEnum item : UserContactStatusEnum.values()) {
                if(item.getStatus().equals(status)) {
                    return item;
                }
            }
            return null;
        }catch (Exception e) {
            return null;
        }
    }
}
