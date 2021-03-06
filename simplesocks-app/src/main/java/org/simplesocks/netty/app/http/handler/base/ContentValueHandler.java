package org.simplesocks.netty.app.http.handler.base;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.simplesocks.netty.app.http.HttpHandler;

import java.nio.charset.StandardCharsets;

public abstract class ContentValueHandler implements HttpHandler {




    protected void returnOkContent(String contentType, String content, ChannelHandlerContext ctx, FullHttpRequest msg){
        returnContent(contentType, content, ctx, HttpResponseStatus.OK, msg);
    }

    protected void returnContent(String contentType, byte[] bytes, ChannelHandlerContext ctx, HttpResponseStatus status, FullHttpRequest msg){
        DefaultFullHttpResponse response = generateHttpResponse0(contentType, bytes, status, msg);
        ctx.write(response);
        returnResponse(ctx, HttpUtil.isKeepAlive(msg));
    }

    protected void returnContent(String contentType, String content, ChannelHandlerContext ctx, HttpResponseStatus status, FullHttpRequest msg){
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        returnContent(contentType, bytes, ctx, status, msg);
    }



    private DefaultFullHttpResponse generateHttpResponse0(String contentType, byte[] bytes, HttpResponseStatus status, FullHttpRequest msg){
        boolean keepAlive = HttpUtil.isKeepAlive(msg);
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        if(keepAlive)
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        response.content().writeBytes(bytes, 0 , bytes.length);
        return response;
    }

    private void returnResponse(ChannelHandlerContext ctx, boolean keepAlive){
        ChannelFuture channelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if(!keepAlive){
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
