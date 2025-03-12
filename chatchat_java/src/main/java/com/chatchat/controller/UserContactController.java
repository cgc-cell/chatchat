package com.chatchat.controller;

import com.chatchat.annotation.GlobalInterceptor;
import com.chatchat.entity.dto.TokenUserInfoDto;
import com.chatchat.entity.dto.UserContactSearchResultDto;
import com.chatchat.entity.enums.PageSize;
import com.chatchat.entity.enums.ResponseCodeEnum;
import com.chatchat.entity.enums.UserContactStatusEnum;
import com.chatchat.entity.enums.UserContactTypeEnum;
import com.chatchat.entity.po.UserContact;
import com.chatchat.entity.po.UserInfo;
import com.chatchat.entity.query.UserContactApplyQuery;
import com.chatchat.entity.query.UserContactQuery;
import com.chatchat.entity.vo.PaginationResultVO;
import com.chatchat.entity.vo.ResponseVO;
import com.chatchat.entity.vo.UserInfoVO;
import com.chatchat.exception.BusinessException;
import com.chatchat.service.UserContactApplyService;
import com.chatchat.service.UserContactService;
import com.chatchat.service.UserInfoService;
import com.chatchat.utils.CopyTools;
import jodd.util.ArraysUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/contact")
public class UserContactController extends ABaseController {

    @Resource
    private UserContactService userContactService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserContactApplyService userContactApplyService;

    @RequestMapping("/search")
    @GlobalInterceptor
    public ResponseVO searchUserContact(HttpServletRequest request,@NotEmpty String contactId) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        UserContactSearchResultDto resultDto=userContactService.searchContact(tokenUserInfoDto.getUserId(),contactId);
        return getSuccessResponseVO(resultDto);
    }

    @RequestMapping("/applyAdd")
    @GlobalInterceptor
    public ResponseVO applyAdd(HttpServletRequest request,@NotEmpty String contactId,String applyInfo) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        Integer joinType=userContactService.applyAdd(tokenUserInfoDto,contactId,applyInfo);
        return getSuccessResponseVO(joinType);
    }


    @RequestMapping("/loadApply")
    @GlobalInterceptor
    public ResponseVO loadApply(HttpServletRequest request,Integer pageNo) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        UserContactApplyQuery applyQuery=new UserContactApplyQuery();
        applyQuery.setReceiveUserId(tokenUserInfoDto.getUserId());
        applyQuery.setOrderBy("last_apply_time desc");
        applyQuery.setQueryContactInfo(true);
        applyQuery.setPageNo(pageNo);
        applyQuery.setPageSize(PageSize.SIZE15.getSize());
        PaginationResultVO resultVO = userContactApplyService.findListByPage(applyQuery);
        return getSuccessResponseVO(resultVO);
    }


    @RequestMapping("/dealWithApply")
    @GlobalInterceptor
    public ResponseVO dealWithApply(HttpServletRequest request, @NotNull Integer applyId,@NotNull Integer status) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        userContactApplyService.dealWithUserContactApply(tokenUserInfoDto.getUserId(),applyId,status);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadContact")
    @GlobalInterceptor
    public ResponseVO loadContact(HttpServletRequest request, @NotEmpty String contactType) {
        UserContactTypeEnum typeEnum=UserContactTypeEnum.getByName(contactType);
        if (typeEnum==null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);

        UserContactQuery contactQuery =new UserContactQuery();
        contactQuery.setUserId(tokenUserInfoDto.getUserId());
        contactQuery.setContactType(typeEnum.getType());
        switch (typeEnum){
            case USER:
                contactQuery.setQueryContactUserInfo(true);
                break;
            case GROUP:
                contactQuery.setQueryGroupInfo(true);
                break;
        }
        contactQuery.setStatusArray(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.BE_BLACKLIST.getStatus(),
                UserContactStatusEnum.BE_DEL.getStatus()
        });
        contactQuery.setOrderBy("last_update_time desc");
        List<UserContact> userContactList=userContactService.findListByParam(contactQuery);

        return getSuccessResponseVO(userContactList);
    }

    /**
     * 查看用户信息，不一定是好友
     * @param request
     * @param contactId
     * @return
     */
    @RequestMapping("/getUserInfo")
    @GlobalInterceptor
    public ResponseVO getUserInfo(HttpServletRequest request, @NotEmpty String contactId) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        UserInfo userInfo=userInfoService.getUserInfoByUserId(contactId);
        UserInfoVO userInfoVO= CopyTools.copy(userInfo,UserInfoVO.class);
        userInfoVO.setContactStatus(UserContactStatusEnum.NOT_FRIEND.getStatus());
        UserContact userContact = userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(),contactId);
        if (userContact==null) {
            userInfoVO.setContactStatus(UserContactStatusEnum.FRIEND.getStatus());
        }
        return getSuccessResponseVO(userInfoVO);
    }

    /**
     * 查看用户信息，必须是好友
     * @param request
     * @param contactId
     * @return
     */
    @RequestMapping("/getContactUserInfo")
    @GlobalInterceptor
    public ResponseVO getContactUserInfo(HttpServletRequest request, @NotEmpty String contactId) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);

        UserContact userContact = userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(),contactId);
        if (userContact==null|| !ArraysUtil.contains(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.BE_BLACKLIST.getStatus(),
                UserContactStatusEnum.BE_DEL.getStatus()
        },userContact.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        UserInfo userInfo=userInfoService.getUserInfoByUserId(contactId);
        UserInfoVO userInfoVO= CopyTools.copy(userInfo,UserInfoVO.class);
        return getSuccessResponseVO(userInfoVO);
    }


    /**
     * 删除联系人
     * @param request
     * @param contactId
     * @return
     */
    @RequestMapping("/deleteContact")
    @GlobalInterceptor
    public ResponseVO deleteContact(HttpServletRequest request, @NotEmpty String contactId) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        userContactService.removeUserContact(tokenUserInfoDto.getUserId(),contactId,UserContactStatusEnum.DEL);
        return getSuccessResponseVO(null);
    }
    /**
     * 删除联系人
     * @param request
     * @param contactId
     * @return
     */
    @RequestMapping("/addContact2BlackList")
    @GlobalInterceptor
    public ResponseVO addContact2BlackList(HttpServletRequest request, @NotEmpty String contactId) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        userContactService.removeUserContact(tokenUserInfoDto.getUserId(),contactId,UserContactStatusEnum.BLACKLIST);
        return getSuccessResponseVO(null);
    }



}
