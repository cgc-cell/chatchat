package com.chatchat.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chatchat.entity.enums.ResponseCodeEnum;
import com.chatchat.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonUtils {
    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    public static SerializerFeature[] FEATURES = new SerializerFeature[]{SerializerFeature.WriteMapNullValue,};
    public static  String convertObj2Json(Object obj){
        return JSON.toJSONString(obj, FEATURES);
    }
    public static <T> T convertJson2Obj(String json,Class<T> clazz){
        try {
            return JSONObject.parseObject(json,clazz);
        }catch (Exception e){
            log.error("convertJson2Obj异常，json:{}",json,e);
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
    }

    public static <T> List<T> convertJson2List(String json, Class<T> clazz){
        try {
            return JSON.parseArray(json,clazz);
        } catch (Exception e) {
            log.error("convertJson2List异常，json:{}",json,e);
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
    }
}
