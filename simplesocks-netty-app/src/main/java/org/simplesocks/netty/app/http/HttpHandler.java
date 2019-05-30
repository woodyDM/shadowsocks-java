package org.simplesocks.netty.app.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import org.simplesocks.netty.app.config.AppConfiguration;

public interface HttpHandler {


    String pathSupport();

    HttpMethod methodSupport();

    HttpResponse handle(ChannelHandlerContext ctx, FullHttpRequest msg, AppConfiguration configuration);

}