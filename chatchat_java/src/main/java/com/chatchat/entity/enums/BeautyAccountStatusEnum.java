package com.chatchat.entity.enums;

import com.chatchat.utils.StringTools;

public enum BeautyAccountStatusEnum {

    NOT_USED(0,"未使用"),
    USED(1,"已使用");
    private Integer status;
    private String description;
    BeautyAccountStatusEnum(Integer type, String description) {
        this.status = type;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
    public static BeautyAccountStatusEnum getByStatus(Integer status) {
        try {
            for (BeautyAccountStatusEnum item : BeautyAccountStatusEnum.values()) {
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
