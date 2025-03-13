package com.chatchat.controller;

import com.chatchat.annotation.GlobalInterceptor;
import com.chatchat.constants.Constants;
import com.chatchat.entity.config.AppConfig;
import com.chatchat.entity.enums.AppUpdateFileTypeEnum;
import com.chatchat.entity.enums.AppUpdateStatusEnum;
import com.chatchat.entity.enums.ResponseCodeEnum;
import com.chatchat.entity.po.AppUpdate;
import com.chatchat.entity.query.AppUpdateQuery;
import com.chatchat.entity.vo.AppUpdateVO;
import com.chatchat.entity.vo.PaginationResultVO;
import com.chatchat.entity.vo.ResponseVO;
import com.chatchat.exception.BusinessException;
import com.chatchat.service.AppUpdateService;
import com.chatchat.utils.CopyTools;
import com.chatchat.utils.StringTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/update")
public class AppUpdateController extends ABaseController{
    @Resource
    private AppUpdateService appUpdateService;
    @Autowired
    private AppConfig appConfig;

    /**
     * 获取更新信息列表
     * @return
     */
    @RequestMapping("/checkVersion")
    public ResponseVO checkVersion(String appVersion,String id) {
        if(StringTools.isEmpty(appVersion)){
            return getSuccessResponseVO(null);
        }
        AppUpdate appUpdate=appUpdateService.getLastUpdate(appVersion,id);
        if (appUpdate==null){
            return getSuccessResponseVO(null);
        }
        AppUpdateVO appUpdateVO= CopyTools.copy(appUpdate,AppUpdateVO.class);
        if (AppUpdateFileTypeEnum.LOCAL.getType().equals(appUpdate.getFileType())){
            File file=new File(appConfig.getProjectFolder()+ Constants.APP_UPDATE_FOLDER+appUpdate.getId()+Constants.APP_EXE_SUFFIX);
            appUpdateVO.setSize(file.length());
        }else {
            appUpdateVO.setSize(0L);
        }
        String filename=Constants.APP_NAME+Constants.APP_EXE_SUFFIX;
        appUpdateVO.setFileName(filename);

        return getSuccessResponseVO(appUpdateVO);
    }


}
