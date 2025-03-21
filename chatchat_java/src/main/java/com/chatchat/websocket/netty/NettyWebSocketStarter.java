package com.chatchat.websocket.netty;

import com.chatchat.constants.Constants;
import com.chatchat.entity.config.AppConfig;
import com.chatchat.utils.StringTools;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class NettyWebSocketStarter implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(NettyWebSocketStarter.class);
    private static EventLoopGroup bossGroup=new NioEventLoopGroup(1);
    private static EventLoopGroup workerGroup=new NioEventLoopGroup();
    @Resource
    private HandlerWebSocket handlerWebSocket;
    @Resource
    private AppConfig appConfig;
    @Autowired
    private HandlerHeartBeat handlerHeartBeat;

    @PreDestroy
    public void close(){
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public void runNetty(){
        try {
            ServerBootstrap serverBootstrap=new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG)).childHandler(
                            new ChannelInitializer() {
                                @Override
                                protected void initChannel(Channel channel) throws Exception {
                                    ChannelPipeline pipeline=channel.pipeline();
                                    // 设置几个重要的处理器
                                    // 对http协议的支持，使用http编码器、解码器
                                    pipeline.addLast(new HttpServerCodec());
                                    //聚合解码 httpRequest/httpContent/lastHttpContent到fullHttpRequest
                                    //保证收到的http请求的完整性
                                    pipeline.addLast(new HttpObjectAggregator(64*1024));
                                    //心跳 long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit
                                    //readerIdleTime 读超时时间 即测试端一定时间内未接受到被测试端消息
                                    //writerIdleTime 写超时时间 即测试端一点时间内未被测试端发送消息
                                    //allIdleTime 所有类型超时时间
                                    pipeline.addLast(new IdleStateHandler(Constants.REDIS_KEY_EXPIRES_HEART_BEAT, 0, 0, TimeUnit.SECONDS));
                                    pipeline.addLast(handlerHeartBeat);
                                    // 将http协议升级为ws协议，对websocket支持
                                    pipeline.addLast(new WebSocketServerProtocolHandler("/ws",null,true,
                                            64*1024,true,true,10000L));
                                    pipeline.addLast(handlerWebSocket);
                                }
                            }
                    );
            Integer wsPort=appConfig.getWsPort();
            String wsPortStr=System.getProperty("ws.port");
            if(!StringTools.isEmpty(wsPortStr)){
                wsPort=Integer.parseInt(wsPortStr);
            }
            ChannelFuture channelFuture=serverBootstrap.bind(wsPort).sync();
            log.info("netty服务启动成功，端口为：{}",wsPort);
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            log.error("启动netty失败",e);
        }
        finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void run() {
        runNetty();
    }
}
