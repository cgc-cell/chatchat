package com.chatchat.redis;

import com.chatchat.constants.Constants;
import com.chatchat.entity.dto.SysSettingDto;
import com.chatchat.entity.dto.TokenUserInfoDto;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component("redisComponent")
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    public Long getUserHeartBeat(String userId) {
        return (Long) redisUtils.get(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }

    public void saveUserHeartBeat(String userId) {
        redisUtils.setex(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId,System.currentTimeMillis(),Constants.REDIS_KEY_EXPIRES_HEART_BEAT);
    }
    public void removeUserHeartBeat(String userId) {
        redisUtils.delete(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }
    public void saveTokenUserInfoDto(TokenUserInfoDto tokenUserInfoDto) {
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN+tokenUserInfoDto.getToken(),tokenUserInfoDto,Constants.VERIFY_CODE_TIME_DAY* 5L);
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN_USERID+tokenUserInfoDto.getUserId(),tokenUserInfoDto.getToken(),Constants.VERIFY_CODE_TIME_DAY* 5L);
    }
    public TokenUserInfoDto getTokenUserInfoDto(String token) {
       TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN+token);
       return tokenUserInfoDto;
    }

    public TokenUserInfoDto getTokenUserInfoDtoByUserId(String userId) {
        String token =(String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN+userId);
        return getTokenUserInfoDto(token);
    }


    public SysSettingDto getSysSetting() {
        SysSettingDto sysSettingDto= (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        sysSettingDto=null==sysSettingDto?new SysSettingDto():sysSettingDto;
        return sysSettingDto;
    }
    public void saveSysSetting(SysSettingDto sysSettingDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingDto);
    }

    public void clearUserContact(String userId) {
        redisUtils.delete(Constants.REDIS_KEY_USER_CONTACT+userId);
    }

    public void addUserContactBatch(String userId, List<String> contactIdList) {
        redisUtils.lPushAll(Constants.REDIS_KEY_USER_CONTACT+userId,contactIdList,Constants.REDIS_KEY_TOKEN_EXPIRES);
    }
    public void addUserContact(String userId, String contactId) {
        List<String> contactIdList = getUserContactList(userId);
        if (contactIdList.contains(contactId)) {
            return;
        }
        redisUtils.lPush(Constants.REDIS_KEY_USER_CONTACT+userId,contactId,Constants.REDIS_KEY_TOKEN_EXPIRES);

    }

    public List<String> getUserContactList(String userId) {
        return (List<String>) redisUtils.getQueueList(Constants.REDIS_KEY_USER_CONTACT+userId);
    }

    public void clearUserTokenByUserId(String userId) {
        String token= (String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID+userId);
        redisUtils.delete(Constants.REDIS_KEY_WS_TOKEN+token);
        redisUtils.delete(Constants.REDIS_KEY_WS_TOKEN_USERID+userId);
    }

}
