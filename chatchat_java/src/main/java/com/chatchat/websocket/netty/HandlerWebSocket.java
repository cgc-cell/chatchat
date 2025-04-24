package com.chatchat.websocket.netty;

import com.chatchat.entity.dto.TokenUserInfoDto;
import com.chatchat.redis.RedisComponent;
import com.chatchat.utils.StringTools;
import com.chatchat.websocket.ChannelContextUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ChannelHandler.Sharable
public class HandlerWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static final Logger log = LoggerFactory.getLogger(HandlerWebSocket.class);

    @Resource
    private RedisComponent redisComponent;
    @Resource
    private ChannelContextUtils channelContextUtils;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的连接接入");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        Channel channel = ctx.channel();
        Attribute<String> attribute=channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId=attribute.get();
//        log.info("收到 {} 的消息:{}",userId, textWebSocketFrame.text());
        redisComponent.saveUserHeartBeat(userId);

//        channelContextUtils.send2Group(textWebSocketFrame.text());

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channelContextUtils.removeContext(ctx.channel());
        log.info("有连接断开");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
            WebSocketServerProtocolHandler.HandshakeComplete handshakeComplete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            String url = handshakeComplete.requestUri();
            String token=getToken(url);
            if (token==null){
                ctx.channel().close();
                return;
            }
            log.info("url:{}", url);
            TokenUserInfoDto tokenUserInfoDto=redisComponent.getTokenUserInfoDto(token);
            if (tokenUserInfoDto==null){
                ctx.channel().close();
                return;
            }
            channelContextUtils.addContext(tokenUserInfoDto.getUserId(), ctx.channel());

        }
    }
    private String getToken(String url){
        if(StringTools.isEmpty(url)){
            return null;
        }
        String[] query = url.split("\\?");
        if (query.length != 2) {
            return null;
        }
        String[] params = query[1].split("=");
        if (params.length != 2) {
            return null;
        }
        return params[1];
    }
}
