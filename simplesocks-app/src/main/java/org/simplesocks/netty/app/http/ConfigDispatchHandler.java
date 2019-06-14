package org.simplesocks.netty.app.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.http.handler.*;

@ChannelHandler.Sharable
@Slf4j
public class ConfigDispatchHandler extends SimpleChannelInboundHandler<FullHttpRequest> {



    private final static Dispatcher dispatcher ;
    static {

        dispatcher = new Dispatcher();
        dispatcher.register("/api/setting", "GET", new SettingInfoHandler());
        dispatcher.register("/api/setting", "POST", new SettingHandler());
        dispatcher.register("/api/statistic", "GET", new StatisticHandler());
        dispatcher.register("/api/statistic/reset", "POST", new StatisticResetHandler());

        dispatcher.register("/api/domain", "GET", new SettingInfoHandler());    //TODO
        dispatcher.register("/api/domain/pac", "GET", new SettingInfoHandler());    //TODO
        dispatcher.register("/api/domain", "POST", new SettingInfoHandler());       //TODO
        dispatcher.register("/api/info", "GET", new GeneralInfoHandler());
        dispatcher.register("/static/*","GET", new StaticResourceHandler());
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        String uri = msg.uri();
        String method = msg.method().name();
        HttpHandler httpHandler = dispatcher.get(uri, method);
        httpHandler.handle(ctx, msg);
    }
}
