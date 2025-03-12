package com.chatchat.entity.config;

import com.chatchat.utils.StringTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("appConfig")
public class AppConfig {
    @Value("${ws.port:}")
    private Integer wsPort;
    @Value("${project.folder:}")
    private String  projectFolder;
    @Value("${admin.emails:}")
    private String adminEmails;

    public String getAdminEmails() {
        return adminEmails;
    }

    public String getProjectFolder() {
        if (StringTools.isEmpty(this.projectFolder)||!projectFolder.endsWith("/")){
            projectFolder = projectFolder+"/";
        }
        return projectFolder;
    }

    public Integer getWsPort() {
        return wsPort;
    }
}
