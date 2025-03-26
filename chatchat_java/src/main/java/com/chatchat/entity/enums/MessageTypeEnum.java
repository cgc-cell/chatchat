package com.chatchat.entity.enums;

import com.chatchat.utils.StringTools;

public enum MessageTypeEnum {
    INIT(0,"","连接ws获取消息"),
    ADD_FRIEND(1,"","添加好友打招呼"),
    CHAT(2,"","普通聊天信息"),
    GROUP_CREATE(3,"群组已经创建好了，可以和好友一起畅聊了","群创建消息"),
    CONTACT_APPLY(4,"","好友申请"),
    MEDIA_CHAT(5,"","媒体文件"),
    FILE_UPLOADED(6,"","文件上传完成"),
    FORCE_OFFlINE(7,"","强制下线"),
    DISSOLUTION_GROUP(8,"群聊已解散","解散群聊"),
    ADD_GROUP(9,"%s加入了群组","加入群组"),
    GROUP_NAME_UPDATE(10,"","更新群昵称"),
    LEAVE_GROUP(11,"%s退出了群聊","退群"),
    REMOVE_GROUP(12,"%s被管理员移出了群聊","踢出群聊"),
    ADD_FRIEND_SELF(13, "", "添加好友打招呼"),
    NICK_NAME_UPDATE(14, "", "更新用户昵称");
    private final Integer type;
    private final String initMessage;
    private final String description;
    MessageTypeEnum(Integer type, String initMessage, String description) {
        this.type = type;
        this.initMessage = initMessage;
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public String getInitMessage() {
        return initMessage;
    }

    public String getDescription() {
        return description;
    }
    public static MessageTypeEnum getByName(String name) {
        try {
            if(StringTools.isEmpty(name)) {
                return null;
            }
            return MessageTypeEnum.valueOf(name.toUpperCase());
        }catch (Exception e) {
            return null;
        }
    }
    public static MessageTypeEnum getByType(Integer type) {
        try {
            for (MessageTypeEnum item : MessageTypeEnum.values()) {
                if(item.getType().equals(type)) {
                    return item;
                }
            }
            return null;
        }catch (Exception e) {
            return null;
        }
    }
}
