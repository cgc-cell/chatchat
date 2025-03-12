package com.chatchat.constants;

import com.chatchat.entity.enums.UserContactTypeEnum;

public class Constants {
    public static final String REDIS_KEY_VERIFY_CODE = "chatchat:verifycode";
    public static final Integer VERIFY_CODE_TIME_1MIN = 60;
    public static final Integer LENGTH_11=11;
    public static final String REDIS_KEY_WS_USER_HEART_BEAT = "chatchat:ws:user:heartbeat";
    public static final Integer LENGTH_22 = 22;
    public static final String REDIS_KEY_WS_TOKEN = "chatchat:ws:token";
    public static final Integer VERIFY_CODE_TIME_DAY = VERIFY_CODE_TIME_1MIN*60*24;
    public static final String REDIS_KEY_WS_TOKEN_USERID = "chatchat:ws:token:userid";
    public static final String ROBOT_UID = UserContactTypeEnum.USER.getPrefix()+"robot";
    public static final String REDIS_KEY_SYS_SETTING = "chatchat:sys:setting";
    public static final String FILE_FOLDER_FILE = "/file/";
    public static final String FILE_FOLDER_AVATAR = "/avatar";
    public static final String IMAGE_SUFFIX = ".png";
    public static final String COVER_IMAGE_SUFFIX = ".png";
    public static final String APPLY_INFO_TEMPLATE = "我是%s";
}
