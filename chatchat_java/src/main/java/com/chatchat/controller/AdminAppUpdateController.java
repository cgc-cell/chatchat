package com.chatchat.controller;

import com.chatchat.annotation.GlobalInterceptor;
import com.chatchat.entity.enums.AppUpdateStatusEnum;
import com.chatchat.entity.enums.ResponseCodeEnum;
import com.chatchat.entity.po.AppUpdate;
import com.chatchat.entity.query.AppUpdateQuery;
import com.chatchat.entity.vo.PaginationResultVO;
import com.chatchat.entity.vo.ResponseVO;
import com.chatchat.exception.BusinessException;
import com.chatchat.service.AppUpdateService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@RestController
@RequestMapping("/admin")
public class AdminAppUpdateController extends ABaseController{
    @Resource
    private AppUpdateService appUpdateService;
    /**
     * 获取更新信息列表
     * @return
     */
    @RequestMapping("/loadUpdateList")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUpdateList(AppUpdateQuery query) {
        query.setOrderBy("id desc");
        PaginationResultVO resultVO = appUpdateService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }


    /**
     * 获取更新信息
     * @return
     */
    @RequestMapping("/loadUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO loadUpdate(AppUpdateQuery query) {
        query.setOrderBy("id desc");
        return getSuccessResponseVO(appUpdateService.findListByParam(query));
    }

    /**
     * 获取更新信息
     * @return
     */
    @RequestMapping("/saveUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveUpdate(Integer id, @NotEmpty String version,
                                 @NotEmpty String updateDesc,
                                 @NotEmpty Integer fileType,
                                 String outLink,
                                 MultipartFile file) throws IOException {
        AppUpdate appUpdate = new AppUpdate();
        appUpdate.setId(id);
        appUpdate.setVersion(version);
        appUpdate.setUpdateDesc(updateDesc);
        appUpdate.setFileType(fileType);
        appUpdate.setOuterLink(outLink);
        appUpdateService.saveUpdate(appUpdate,file);
        return getSuccessResponseVO(null);
    }


    /**
     * 删除更新信息
     * @return
     */
    @RequestMapping("/deleteUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO deleteUpdate(@NotNull Integer id){

        AppUpdate dbUpdate=this.appUpdateService.getAppUpdateById(id);
        if(!AppUpdateStatusEnum.INIT.getStatus().equals(dbUpdate.getStatus())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        appUpdateService.deleteAppUpdateById(id);
        return getSuccessResponseVO(null);
    }


    /**
     * 发布更新
     * @return
     */
    @RequestMapping("/postUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO postUpdate(@NotNull Integer id,@NotNull Integer status,String grayScaleUid){
        appUpdateService.postUpdate(id,status,grayScaleUid);
        return getSuccessResponseVO(null);
    }

}
