package com.chatchat.controller;

import com.chatchat.annotation.GlobalInterceptor;
import com.chatchat.entity.po.UserInfoBeauty;
import com.chatchat.entity.query.UserInfoBeautyQuery;
import com.chatchat.entity.query.UserInfoQuery;
import com.chatchat.entity.vo.PaginationResultVO;
import com.chatchat.entity.vo.ResponseVO;
import com.chatchat.service.UserInfoBeautyService;
import com.chatchat.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/admin")
public class AdminUserInfoBeautyController extends ABaseController{
    @Resource
    private UserInfoBeautyService userInfoBeautyService;


    /**
     * 获取靓号
     * @return
     */
    @RequestMapping("/loadBeautyAccountList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadBeautyAccountList(UserInfoBeautyQuery query) {

        query.setOrderBy("id desc");

        PaginationResultVO paginationResultVO = userInfoBeautyService.findListByPage(query);
        return getSuccessResponseVO(paginationResultVO);
    }
    /**
     * 添加靓号
     * @return
     */
    @RequestMapping("/saveBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveBeautyAccount(UserInfoBeauty userInfoBeauty) {

        userInfoBeautyService.saveAccount(userInfoBeauty);

        return getSuccessResponseVO(null);
    }


    /**
     * 添加靓号
     * @return
     */
    @RequestMapping("/deleteBeautyAccount")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO deleteBeautyAccount(Integer id) {

        userInfoBeautyService.deleteUserInfoBeautyById(id);

        return getSuccessResponseVO(null);
    }

}
