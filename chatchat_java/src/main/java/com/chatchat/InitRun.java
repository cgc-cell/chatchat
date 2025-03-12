package com.chatchat;

import com.chatchat.redis.RedisUtils;
import org.apache.ibatis.session.SqlSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;


@Component("initRun")
public class InitRun implements ApplicationRunner {
    public  static final Logger logger = LoggerFactory.getLogger(InitRun.class);

    @Resource
    private DataSource dataSource;;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            dataSource.getConnection();
            redisUtils.get("test");
            logger.info("服务启动成功，可以继续进行开发啦!!!");
        }catch (SQLException e){
            logger.error("数据库配置错误，请检测数据库配置");
        }catch (Exception e){
            logger.error("服务启动一场，错误信息：{}", e.getMessage());
        }
    }
}
