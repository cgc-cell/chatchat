package com.chatchat.controller;

import com.chatchat.annotation.GlobalInterceptor;
import com.chatchat.entity.enums.ResponseCodeEnum;
import com.chatchat.entity.enums.UserContactStatusEnum;
import com.chatchat.entity.po.GroupInfo;
import com.chatchat.entity.po.UserInfoBeauty;
import com.chatchat.entity.query.GroupInfoQuery;
import com.chatchat.entity.query.UserInfoBeautyQuery;
import com.chatchat.entity.vo.PaginationResultVO;
import com.chatchat.entity.vo.ResponseVO;
import com.chatchat.exception.BusinessException;
import com.chatchat.service.GroupInfoService;
import com.chatchat.service.UserInfoBeautyService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/admin")
public class AdminGroupController extends ABaseController{
    @Resource
    private GroupInfoService groupInfoService;


    /**
     * 获取群组列表
     * @return
     */
    @RequestMapping("/loadGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadGroup(GroupInfoQuery query) {

        query.setOrderBy("create_time desc");
        query.setQueryGroupOwner(true);
        query.setQueryGroupMemberCount(true);
        PaginationResultVO paginationResultVO = groupInfoService.findListByPage(query);
        return getSuccessResponseVO(paginationResultVO);
    }


    /**
     * 解散群组
     * @return
     */
    @RequestMapping("/dissolutionGroup")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO dissolutionGroup(@NotEmpty String groupId) {
        GroupInfo groupInfo=groupInfoService.getGroupInfoByGroupId(groupId);
        if(groupInfo==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        groupInfoService.dissolutionGroup(groupId,groupInfo.getGroupOwnerId());
        return getSuccessResponseVO(null);
    }
}
