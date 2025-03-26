package com.chatchat.utils;
import com.chatchat.constants.Constants;
import com.chatchat.entity.enums.UserContactTypeEnum;
import com.chatchat.exception.BusinessException;
import jodd.util.RandomString;
import jodd.util.StringUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import javax.validation.constraints.NotEmpty;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;


public class StringTools {

    public static void checkParam(Object param) {
        try {
            Field[] fields = param.getClass().getDeclaredFields();
            boolean notEmpty = false;
            for (Field field : fields) {
                String methodName = "get" + StringTools.upperCaseFirstLetter(field.getName());
                Method method = param.getClass().getMethod(methodName);
                Object object = method.invoke(param);
                if (object != null && object instanceof java.lang.String && !StringTools.isEmpty(object.toString())
                        || object != null && !(object instanceof java.lang.String)) {
                    notEmpty = true;
                    break;
                }
            }
            if (!notEmpty) {
                throw new BusinessException("多参数更新，删除，必须有非空条件");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("校验参数是否为空失败");
        }
    }

    public static String upperCaseFirstLetter(String field) {
        if (isEmpty(field)) {
            return field;
        }
        //如果第二个字母是大写，第一个字母不大写
        if (field.length() > 1 && Character.isUpperCase(field.charAt(1))) {
            return field;
        }
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }

    public static String getGroupId() {
        return  UserContactTypeEnum.GROUP.getPrefix() +getRandomNumber(Constants.LENGTH_11);
    }
    public static String getUserId() {
        return  UserContactTypeEnum.USER.getPrefix() +getRandomNumber(Constants.LENGTH_11);
    }
    public static String getRandomNumber(Integer length ) {
        return RandomStringUtils.random(length,false,true);
    }
    public static String getRandomString(Integer length ) {
        return RandomStringUtils.random(length,true,true);
    }
    public static String encodeMD5(String str) {
        return StringTools.isEmpty(str)?null:DigestUtils.md5Hex(str);
    }
    public static String cleanHtmlTag(String content) {
        if(isEmpty(content)){
            return content;
        }
        content = content.replace("<", "&lt;");
        content = content.replace(">", "&gt;");
        content = content.replace("\n\r","<br>");
        content = content.replace("\n","<br>");
        content = content.replace("\r","<br>");
        return content;
    }

    public static String getChatSessionId4User(String[] userIds) {
        Arrays.sort(userIds);
        return encodeMD5(StringUtil.join(userIds,","));
    }

    public static String getChatSessionId4Group(String groupId) {
        return encodeMD5(groupId);
    }

    public static String getFileSuffix(String fileName) {
        if (isEmpty(fileName)) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public static boolean isNumber(@NotEmpty String str) {
        String checkStr = "^[0-9]+$";
        if(isEmpty(str)){
            return false;
        }
        return str.matches(checkStr);

    }
}
