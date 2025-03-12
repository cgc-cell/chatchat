package com.chatchat.controller;

import com.chatchat.annotation.GlobalInterceptor;
import com.chatchat.entity.dto.TokenUserInfoDto;
import com.chatchat.entity.po.UserInfo;
import com.chatchat.entity.query.UserInfoQuery;
import com.chatchat.entity.vo.PaginationResultVO;
import com.chatchat.entity.vo.ResponseVO;
import com.chatchat.entity.vo.UserInfoVO;
import com.chatchat.exception.BusinessException;
import com.chatchat.service.UserInfoService;
import com.chatchat.utils.CopyTools;
import com.chatchat.utils.StringTools;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@RestController
@RequestMapping("/admin")
public class AdminUserInfoController extends ABaseController{
    @Resource
    private UserInfoService userInfoService;


    /**
     * 获取用户信息
     * @return
     */
    @RequestMapping("/loadUsers")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUsers(UserInfoQuery userInfoQuery) {

        userInfoQuery.setOrderBy("create_time desc");

        PaginationResultVO paginationResultVO = userInfoService.findListByPage(userInfoQuery);
        return getSuccessResponseVO(paginationResultVO);
    }

    /**
     * 更改用户状态
     * @return
     */
    @RequestMapping("/updateUserStatus")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO updateUserStatus(@NotNull Integer status, @NotEmpty String userId) {
        userInfoService.updateUserStatus(status, userId);
        return getSuccessResponseVO(null);
    }

    /**
     * 强制用户下线
     * @return
     */
    @RequestMapping("/forceOffLine")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO forceOffLine(@NotEmpty String userId) {
        userInfoService.forceOffLine(userId);
        return getSuccessResponseVO(null);
    }

}
