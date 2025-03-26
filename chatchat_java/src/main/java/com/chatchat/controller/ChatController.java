package com.chatchat.controller;

import com.chatchat.constants.Constants;
import com.chatchat.entity.config.AppConfig;
import com.chatchat.entity.dto.MessageSendDto;
import com.chatchat.entity.dto.TokenUserInfoDto;
import com.chatchat.entity.enums.MessageTypeEnum;
import com.chatchat.entity.enums.ResponseCodeEnum;
import com.chatchat.entity.po.ChatMessage;
import com.chatchat.entity.vo.ResponseVO;
import com.chatchat.exception.BusinessException;
import com.chatchat.service.ChatMessageService;
import com.chatchat.service.ChatSessionService;
import com.chatchat.service.ChatSessionUserService;
import com.chatchat.utils.StringTools;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.el.parser.BooleanNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

@RestController
@RequestMapping("/chat")
public class ChatController extends ABaseController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private AppConfig appConfig;

    @RequestMapping("/sendMessage")
    public ResponseVO sendMessage(HttpServletRequest request,
                                  @NotEmpty String contactId,
                                  @NotEmpty @Max(500) String messageContent,
                                  @NotNull Integer messageType,
                                  Long fileSize,
                                  String fileName,
                                  Integer fileType) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        ChatMessage chatMessage=new ChatMessage();
        chatMessage.setContactId(contactId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setMessageType(messageType);
        chatMessage.setFileSize(fileSize);
        chatMessage.setFileName(fileName);
        chatMessage.setFileType(fileType);
        chatMessage.setSendUserId(tokenUserInfoDto.getUserId());
        chatMessage.setSendUserNickName(tokenUserInfoDto.getNickName());

        MessageSendDto messageSendDto=chatMessageService.saveMessage(chatMessage,tokenUserInfoDto);
        return getSuccessResponseVO(messageSendDto);
    }

    @RequestMapping("/uploadFile")
    public ResponseVO uploadFile(HttpServletRequest request,
                                  @NotNull Long messageId,
                                  @NotNull MultipartFile file,
                                  MultipartFile cover) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        chatMessageService.saveMessageFile(tokenUserInfoDto.getUserId(),messageId,file,cover);
        return getSuccessResponseVO(null);
    }


    @RequestMapping("/downloadFile")
    public void downloadFile(HttpServletRequest request,
                                 HttpServletResponse response,
                                 @NotEmpty String fileId,
                                 @NotNull Boolean showCover) {
        TokenUserInfoDto tokenUserInfoDto=getTokenUserInfoDto(request);
        OutputStream out=null;
        FileInputStream in =null;
        try{
            File file = null;
            if (!StringTools.isNumber(fileId)){
                String avatarFolder= Constants.FILE_FOLDER_FILE+Constants.FILE_FOLDER_AVATAR;
                String avatarPath= appConfig.getProjectFolder()+avatarFolder+fileId+Constants.IMAGE_SUFFIX;
                if(showCover){
                    avatarPath=avatarPath+Constants.COVER_IMAGE_SUFFIX;
                }
                file=new File(avatarPath);
            }else{
                file=chatMessageService.downloadFile(tokenUserInfoDto,Long.parseLong(fileId),showCover);
            }
            if(!file.exists()){
                throw new BusinessException(ResponseCodeEnum.CODE_602);
            }
            response.setContentType("application/x-msdownload;charset=utf-8");
            response.setHeader("Content-Disposition","attachment;");
            response.setContentLength((int) file.length());
            in=new FileInputStream(file);
            byte[] buffer=new byte[1024];
            out=response.getOutputStream();
            int len=0;
            while((len=in.read(buffer))!=-1){
                out.write(buffer,0,len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("下载文件失败",e);
            throw new BusinessException(ResponseCodeEnum.CODE_602);
        }
        finally {
            if(out!=null){
                try {
                    out.close();
                } catch (Exception e) {
                    log.error("io异常",e);
                }
            }
            if(in!=null){
                try {
                    in.close();
                } catch (Exception e) {
                    log.error("io异常",e);
                }
            }
        }
    }
}
