package com.chatchat.controller;

import com.chatchat.annotation.GlobalInterceptor;
import com.chatchat.constants.Constants;
import com.chatchat.entity.config.AppConfig;
import com.chatchat.entity.dto.SysSettingDto;
import com.chatchat.entity.enums.ResponseCodeEnum;
import com.chatchat.entity.po.GroupInfo;
import com.chatchat.entity.query.GroupInfoQuery;
import com.chatchat.entity.vo.PaginationResultVO;
import com.chatchat.entity.vo.ResponseVO;
import com.chatchat.exception.BusinessException;
import com.chatchat.redis.RedisComponent;
import com.chatchat.service.GroupInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/admin")
public class AdminSettingController extends ABaseController{
    @Resource
    private RedisComponent redisComponent;
    @Autowired
    private AppConfig appConfig;


    /**
     * 获取系统设置
     * @return
     */
    @RequestMapping("/getSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO getSysSetting() {

        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        return getSuccessResponseVO(sysSettingDto);
    }

    /**
     * 更改系统设置
     * @return
     */
    @RequestMapping("/saveSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveSysSetting(SysSettingDto sysSettingDto,
                                     MultipartFile robotFile,
                                     MultipartFile robotCover) throws IOException {

        if(robotFile!=null){
            String basePath = appConfig.getProjectFolder()+ Constants.FILE_FOLDER_FILE;
            File targetFolder = new File(basePath+Constants.FILE_FOLDER_AVATAR);
            if(!targetFolder.exists()){
                targetFolder.mkdirs();
            }
            String fileName = targetFolder.getPath()+"/"+Constants.ROBOT_UID+Constants.IMAGE_SUFFIX;
            robotFile.transferTo(new File(fileName));
            robotCover.transferTo(new File(fileName+Constants.COVER_IMAGE_SUFFIX));
        }
        redisComponent.saveSysSetting(sysSettingDto);
        return getSuccessResponseVO(null);
    }

}
