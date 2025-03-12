package com.chatchat.entity.enums;

public enum UserContactApplyStatusEnum {

    INIT(0,"未处理"),
    PASS(1,"已同意"),
    REJECT(2,"已拒绝"),
    BLACKLIST(3,"已拉黑");
    private Integer status;
    private String description;
    UserContactApplyStatusEnum(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
    public static UserContactApplyStatusEnum getByStatus(Integer status) {
        try {
            for (UserContactApplyStatusEnum item : UserContactApplyStatusEnum.values()) {
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
