package com.chatchat.entity.enums;

public enum GroupStatusEnum {


    DISABLE(0,"解散"),
    NORMAL(1,"正常");
    private Integer status;
    private String description;
    GroupStatusEnum(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
    public static GroupStatusEnum getByStatus(Integer status) {
        try {
            for (GroupStatusEnum item : GroupStatusEnum.values()) {
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
