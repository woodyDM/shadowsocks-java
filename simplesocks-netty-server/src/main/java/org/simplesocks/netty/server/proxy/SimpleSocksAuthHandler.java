package org.simplesocks.netty.server.proxy;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.common.encrypt.factory.EncrypterFactory;
import org.simplesocks.netty.common.protocol.*;
import org.simplesocks.netty.server.auth.AuthProvider;
import org.simplesocks.netty.server.proxy.relay.RelayProxyDataHandler;

@Slf4j
public class SimpleSocksAuthHandler extends SimpleChannelInboundHandler<SimpleSocksMessage> {

    private AuthProvider authProvider;
    private EncrypterFactory encrypterFactory;

    public SimpleSocksAuthHandler(AuthProvider authProvider,EncrypterFactory encrypterFactory) {
        this.authProvider = authProvider;
        this.encrypterFactory = encrypterFactory;
    }

    /**
     * read
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SimpleSocksMessage msg) throws Exception {
        DataType type = msg.getType();
        switch (type){
            case CONNECT:{
                ConnectionMessage request = (ConnectionMessage)msg;
                boolean ok = authProvider.tryAuthenticate(request.getAuth(), ctx.channel() );
                if(ok){
                    RelayProxyDataHandler relayProxyDataHandler = new RelayProxyDataHandler(request, authProvider, encrypterFactory);
                    ctx.pipeline().addLast(relayProxyDataHandler);
                    relayProxyDataHandler.tryToConnectToTarget(ctx.channel());
                }else{
                    ctx.channel().writeAndFlush(ConnectionResponse.fail(request.getEncryptType()));
                }
                break;
            }
            case PROXY_DATA:{

                boolean authenticated = authProvider.authenticated(ctx.channel());
                if(authenticated){
                    ctx.fireChannelRead(msg);
                }else{
                    ProxyDataMessage request = (ProxyDataMessage)msg;
                    ctx.channel().writeAndFlush(new ProxyDataResponse(ServerResponseMessage.Code.FAIL, request.getId()));
                }
                break;
            }
            default:
                throw new IllegalStateException("client send data illegal! "+type);
        }
    }

}
