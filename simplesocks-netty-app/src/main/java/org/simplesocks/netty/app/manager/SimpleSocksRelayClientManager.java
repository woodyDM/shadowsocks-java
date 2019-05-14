package org.simplesocks.netty.app.manager;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.simplesocks.netty.app.proxy.relay.ssocks.SimpleSocksRelayClientAdapter;
import org.simplesocks.netty.client.SimpleSocksProtocolClient;
import org.simplesocks.netty.common.encrypt.EncType;
import org.simplesocks.netty.common.encrypt.EncrypterFactory;
import org.simplesocks.netty.common.exception.BaseSystemException;
import org.simplesocks.netty.common.netty.RelayClient;
import org.simplesocks.netty.common.netty.RelayClientManager;


@Slf4j
public class SimpleSocksRelayClientManager implements RelayClientManager {

    private String host;
    private int port;
    private String auth;
    private EventLoopGroup loopGroup;
    private String encType;
    private EncrypterFactory encrypterFactory;

    public SimpleSocksRelayClientManager(String host, int port, String auth, EventLoopGroup loopGroup,String encType, EncrypterFactory encrypterFactory) {
        this.host = host;
        this.port = port;
        this.auth = auth;
        this.loopGroup = loopGroup;
        this.encrypterFactory = encrypterFactory;
        this.encType = encType;
    }

    @Override
    public Promise<RelayClient> borrow(EventExecutor eventExecutor, SocksCmdRequest socksCmdRequest) {
        SimpleSocksProtocolClient client = new SimpleSocksProtocolClient(auth, encType, host, port, loopGroup, encrypterFactory);
        SimpleSocksRelayClientAdapter adapter = new SimpleSocksRelayClientAdapter(client);
        Promise<RelayClient> promise = eventExecutor.newPromise();
        client.init().addListener(future -> {
            if(future.isSuccess()){
                promise.setSuccess(adapter);
            }else{
                promise.setFailure(new BaseSystemException("Failed connect to server "+host+"with auth "+auth));
            }
        }) ;
        return promise;
    }

    @Override
    public void returnClient(RelayClient client) {
        SimpleSocksRelayClientAdapter adapter = (SimpleSocksRelayClientAdapter)client;
        adapter.getClient().close();
    }
}
