package com.chatchat.entity.enums;

import com.chatchat.utils.StringTools;

public enum JoinTypeEnum {

    JOIN(0, "直接添加"),
    APPLY(1, "同意后添加");
    private Integer type;
    private String description;

    JoinTypeEnum(Integer type, String description) {
        this.type = type;
        this.description = description;
    }

    public static JoinTypeEnum getByName(String name) {
        try {
            if(StringTools.isEmpty(name)) {
                return null;
            }
            return JoinTypeEnum.valueOf(name.toUpperCase());
        }catch (Exception e) {
            return null;
        }
    }

    public static JoinTypeEnum getByType(Integer status) {
        try {
            for (JoinTypeEnum item : JoinTypeEnum.values()) {
                if (item.getType().equals(status)) {
                    return item;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
