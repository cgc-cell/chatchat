package com.chatchat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.chatchat"})
@MapperScan(basePackages = {"com.chatchat.mappers"})
@EnableTransactionManagement
@EnableScheduling
public class ChatChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatChatApplication.class,args);
    }
}
