package com.chatchat.entity.enums;

public enum MessageStatusEnum {


    SENDING(0,"发送中"),
    SENDED(1,"发送完成");
    private Integer status;
    private String description;
    MessageStatusEnum(Integer status, String description) {
        this.status = status;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }
    public static MessageStatusEnum getByStatus(Integer status) {
        try {
            for (MessageStatusEnum item : MessageStatusEnum.values()) {
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
