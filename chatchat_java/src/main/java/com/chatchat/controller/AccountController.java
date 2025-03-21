package com.chatchat.controller;

import com.chatchat.annotation.GlobalInterceptor;
import com.chatchat.constants.Constants;
import com.chatchat.entity.dto.MessageSendDto;
import com.chatchat.entity.dto.TokenUserInfoDto;
import com.chatchat.entity.po.UserInfo;
import com.chatchat.entity.vo.ResponseVO;
import com.chatchat.entity.vo.UserInfoVO;
import com.chatchat.exception.BusinessException;
import com.chatchat.redis.RedisComponent;
import com.chatchat.redis.RedisUtils;
import com.chatchat.service.UserInfoService;
import com.chatchat.utils.CopyTools;
import com.chatchat.websocket.MessageHandler;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.utils.CaptchaUtil;
import org.apache.catalina.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController("accountController")
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Resource
    private RedisUtils redisUtils;
    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private MessageHandler messageHandler;

    @RequestMapping("/checkCode")
    public ResponseVO checkCaptcha(HttpServletRequest request) {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100,42);
        String verifyCode = captcha.text();
        String captchaBase64 = captcha.toBase64();
        String verifyCodeKey= UUID.randomUUID().toString();
        logger.info("验证码是{}", verifyCode);
        redisUtils.setex(Constants.REDIS_KEY_VERIFY_CODE+verifyCodeKey,verifyCode,Constants.VERIFY_CODE_TIME_1MIN*10);
        Map<String,String> result=new HashMap<String,String>();
        result.put("verifyCode",captchaBase64);
        result.put("verifyCodeKey",verifyCodeKey);
        return getSuccessResponseVO(result);
    }


    @RequestMapping("/register")
    public ResponseVO register(@NotEmpty String nickname,
                               @NotEmpty @Email String email,
                               @NotEmpty String password,
                               @NotEmpty String verifyCode,
                               @NotEmpty String verifyCodeKey) {
        try {
            if(!verifyCode.equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_VERIFY_CODE+verifyCodeKey))){
                throw new BusinessException("图像验证码不正确");
            }
            userInfoService.register(email,nickname,password);
        }finally {
            redisUtils.delete(Constants.REDIS_KEY_VERIFY_CODE+verifyCodeKey);
        }
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/login")
    public ResponseVO login(@NotEmpty @Email String email,
                               @NotEmpty String password,
                               @NotEmpty String verifyCode,
                               @NotEmpty String verifyCodeKey) {
        try {
            if(!verifyCode.equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_VERIFY_CODE+verifyCodeKey))){
                throw new BusinessException("图像验证码不正确");
            }
            UserInfoVO userInfoVO=userInfoService.login(email,password);
            return getSuccessResponseVO(userInfoVO);
        }finally {
            redisUtils.delete(Constants.REDIS_KEY_VERIFY_CODE+verifyCodeKey);
        }
    }

    @GlobalInterceptor
    @RequestMapping("/getSystemSetting")
    public ResponseVO getSystemSetting() {
        return getSuccessResponseVO(redisComponent.getSysSetting());
    }


    @RequestMapping("/test")
    public ResponseVO test() {

        MessageSendDto messageSendDto=new MessageSendDto();
        messageSendDto.setMessageContent("sdfkjaskdjfjkasdjfkals"+System.currentTimeMillis());
        messageHandler.sendMessage(messageSendDto);
        return getSuccessResponseVO(null);
    }


}
