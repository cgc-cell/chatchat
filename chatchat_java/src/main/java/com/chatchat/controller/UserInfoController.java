package com.chatchat.controller;

import com.chatchat.annotation.GlobalInterceptor;
import com.chatchat.entity.dto.TokenUserInfoDto;
import com.chatchat.entity.enums.UserContactStatusEnum;
import com.chatchat.entity.po.UserInfo;
import com.chatchat.entity.vo.ResponseVO;
import com.chatchat.entity.vo.UserInfoVO;
import com.chatchat.exception.BusinessException;
import com.chatchat.service.UserInfoService;
import com.chatchat.utils.CopyTools;
import com.chatchat.utils.StringTools;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;

@RestController
@RequestMapping("/userInfo")
public class UserInfoController extends ABaseController{
    @Resource
    private UserInfoService userInfoService;


    /**
     * 获取当前用户信息
     * @param request
     * @return
     */
    @RequestMapping("/getMyUserInfo")
    @GlobalInterceptor
    public ResponseVO getMyUserInfo(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        UserInfo userInfo=userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
        UserInfoVO userInfoVO= CopyTools.copy(userInfo,UserInfoVO.class);
        userInfoVO.setAdmin(tokenUserInfoDto.isAdmin());
        return getSuccessResponseVO(userInfoVO);
    }


    /**
     * 修改当前用户信息
     * @param request
     * @return
     */
    @RequestMapping("/saveMyUserInfo")
    @GlobalInterceptor
    public ResponseVO saveMyUserInfo(HttpServletRequest request, UserInfo userInfo, MultipartFile avatarFile,MultipartFile avatarCover) throws IOException {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        userInfo.setUserId(tokenUserInfoDto.getUserId());
        userInfo.setStatus(null);
        userInfo.setCreateTime(null);
        userInfo.setLastLoginTime(null);
        userInfo.setPassword(null);
        userInfo.setLastOffTime(null);
        userInfoService.updateUserInfo(userInfo,avatarFile,avatarCover);
        return getMyUserInfo(request);
    }


    /**
     * 修改当前用户密码
     * @param request
     * @return
     */
    @RequestMapping("/updatePassword")
    @GlobalInterceptor
    public ResponseVO updatePassword(HttpServletRequest request,String oldPassword,String newPassword) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        UserInfo userInfo=userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
        if(!userInfo.getPassword().equals(oldPassword)){
            throw new BusinessException("旧密码错误");
        }
        userInfo.setPassword(StringTools.encodeMD5(newPassword));
        this.userInfoService.updateUserInfoByUserId(userInfo,tokenUserInfoDto.getUserId());
        // TODO 强制退出登录
        return getMyUserInfo(request);
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @RequestMapping("/logout")
    @GlobalInterceptor
    public ResponseVO logout(HttpServletRequest request) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);

        // TODO 退出登录 关闭ws连接
        return getMyUserInfo(request);
    }
}
