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
    public static final String COVER_IMAGE_SUFFIX = "_cover.png";
    public static final String APPLY_INFO_TEMPLATE = "我是%s";
    public static final String APP_UPDATE_FOLDER = "/update/";
    public static final String APP_EXE_SUFFIX = ".exe";
    public static final String APP_NAME = "chatchat_setup";
    public static final Long REDIS_KEY_EXPIRES_HEART_BEAT=600L;
    public static final String REDIS_KEY_USER_CONTACT = "chatchat:ws:user:contact";
    public static final Long REDIS_KEY_TOKEN_EXPIRES=Constants.VERIFY_CODE_TIME_DAY* 5L;
    public static final Long MILLISECOND_3DAY_AGO =365*24*60*60*1000L;
    public static final String[] IMAGE_SUFFIXES = {".jpg", ".jpeg", ".png", ".gif",".bmp",".webp"};
    public static final String[] VIDEO_SUFFIXES = {".mp3", ".wav", ".mp4",".avi",".mov",".mvk",".rmvb"};
    public static final Long FILE_SIZE_MB = 1024 * 1024L;
}
