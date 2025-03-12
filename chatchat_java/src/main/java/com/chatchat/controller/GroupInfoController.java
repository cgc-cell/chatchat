package com.chatchat.controller;

import java.io.IOException;
import java.util.List;

import com.chatchat.annotation.GlobalInterceptor;
import com.chatchat.entity.dto.TokenUserInfoDto;
import com.chatchat.entity.enums.GroupStatusEnum;
import com.chatchat.entity.enums.UserContactStatusEnum;
import com.chatchat.entity.po.UserContact;
import com.chatchat.entity.query.GroupInfoQuery;
import com.chatchat.entity.po.GroupInfo;
import com.chatchat.entity.query.UserContactQuery;
import com.chatchat.entity.vo.GroupInfoVO;
import com.chatchat.entity.vo.ResponseVO;
import com.chatchat.exception.BusinessException;
import com.chatchat.service.GroupInfoService;
import com.chatchat.service.UserContactService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Controller
 */
@RestController("groupInfoController")
@RequestMapping("/group")
public class GroupInfoController extends ABaseController {

    @Resource
    private GroupInfoService groupInfoService;

    @Resource
    private UserContactService userContactService;

    @RequestMapping("/saveGroup")
    @GlobalInterceptor
    public ResponseVO saveGroupInfo(HttpServletRequest request,
                                    String groupId,
                                    @NotEmpty String groupName,
                                    String groupNotice,
                                    @NotNull Integer joinType,
                                    MultipartFile avatarFile,
                                    MultipartFile avatarCover) throws IOException {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        GroupInfo groupInfo=new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setGroupOwnerId(tokenUserInfoDto.getUserId());
        groupInfo.setGroupName(groupName);
        groupInfo.setGroupNotice(groupNotice);
        groupInfo.setJoinType(joinType);
        this.groupInfoService.saveGroup(groupInfo,avatarFile,avatarCover);


        return getSuccessResponseVO(null);

    }


    @RequestMapping("/loadMyGroup")
    @GlobalInterceptor
    public ResponseVO loadMyGroup(HttpServletRequest request){
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        GroupInfoQuery groupInfoQuery=new GroupInfoQuery();
        groupInfoQuery.setGroupOwnerId(tokenUserInfoDto.getUserId());
        groupInfoQuery.setOrderBy("create_time desc");
        List<GroupInfo> groupInfoList=this.groupInfoService.findListByParam(groupInfoQuery);
        return getSuccessResponseVO(groupInfoList);

    }


    @RequestMapping("/loadGroupInfo")
    @GlobalInterceptor
    public ResponseVO loadGroupInfo(HttpServletRequest request,@NotEmpty String groupId){
        GroupInfo groupInfo=getGroupInfoDetailCommon(request,groupId);
        UserContactQuery userContactQuery=new UserContactQuery();
        userContactQuery.setContactId(groupId);
        Integer memberCount=this.userContactService.findCountByParam(userContactQuery);
        groupInfo.setMemberCount(memberCount);
        return getSuccessResponseVO(groupInfo);
    }


    public GroupInfo getGroupInfoDetailCommon(HttpServletRequest request, String groupId){
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        UserContact userContact=userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(),groupId);
        if(null==userContact|| !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())){
            throw new BusinessException("你不在群聊或者群聊不存在或已解散");
        }
        GroupInfo groupInfo=this.groupInfoService.getGroupInfoByGroupId(groupId);
        if(null==groupInfo|| !GroupStatusEnum.NORMAL.getStatus().equals(groupInfo.getStatus())){
            throw new BusinessException("群聊不存在或已解散");
        }
        return groupInfo;
    }

    @RequestMapping("/getGroupInfo4Chat")
    @GlobalInterceptor
    public ResponseVO getGroupInfo4Chat(HttpServletRequest request,@NotEmpty String groupId){
        GroupInfo groupInfo=getGroupInfoDetailCommon(request,groupId);
        UserContactQuery userContactQuery=new UserContactQuery();
        userContactQuery.setContactId(groupId);
        userContactQuery.setQueryUserInfo(true);
        userContactQuery.setOrderBy("create_time asc");
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> userContactList=this.userContactService.findListByParam(userContactQuery);
        GroupInfoVO groupInfoVO=new GroupInfoVO();
        groupInfoVO.setGroupInfo(groupInfo);
        groupInfoVO.setUserContactList(userContactList);

        return getSuccessResponseVO(groupInfoVO);
    }


}