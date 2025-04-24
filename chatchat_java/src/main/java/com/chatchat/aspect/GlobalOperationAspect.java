package com.chatchat.aspect;

import com.chatchat.annotation.GlobalInterceptor;
import com.chatchat.constants.Constants;
import com.chatchat.entity.dto.TokenUserInfoDto;
import com.chatchat.entity.enums.ResponseCodeEnum;
import com.chatchat.exception.BusinessException;
import com.chatchat.redis.RedisComponent;
import com.chatchat.redis.RedisUtils;
import com.chatchat.utils.StringTools;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component("globalOperationAspect")
public class GlobalOperationAspect {
    @Resource
    RedisUtils redisUtils;

    private static final Logger logger = LoggerFactory.getLogger(GlobalInterceptor.class);

    @Before("@annotation(com.chatchat.annotation.GlobalInterceptor)")
    public  void interceptorDo(JoinPoint point) {
        try{
            Method method=((MethodSignature)point.getSignature()).getMethod();
            GlobalInterceptor interceptor=method.getAnnotation(GlobalInterceptor.class);
            if(null==interceptor){
                return;
            }
            if (interceptor.checkLogin()||interceptor.checkAdmin()) {
                checkLogin(interceptor.checkAdmin());
            }
        }catch (BusinessException e){
            logger.error("全局拦截异常",e);
            throw e;
        }catch (Exception e){
            logger.error("全局拦截异常",e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }catch (Throwable e){
            logger.error("全局拦截异常",e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }

    }


    private void checkLogin(boolean checkAdmin) {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("token");
        if (StringTools.isEmpty(token)) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN+token);
        if(tokenUserInfoDto==null){
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if(checkAdmin && !tokenUserInfoDto.isAdmin()){
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
    }

}
