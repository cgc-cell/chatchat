package com.chatchat.entity.enums;

public enum UserStatusEnum {


    ENABLE(0,"正常"),
    DISABLE(1,"封禁");
    private Integer status;
    private String description;
    UserStatusEnum(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
    public static UserStatusEnum getByStatus(Integer status) {
        try {
            for (UserStatusEnum item : UserStatusEnum.values()) {
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
