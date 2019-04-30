package org.shadowsocks.netty.client.manager;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.shadowsocks.netty.client.proxy.relay.DirectRelayClient;
import org.shadowsocks.netty.common.netty.RelayClient;

public class DirectRelayClientManager implements RelayClientManager {

    EventLoopGroup group;

    public DirectRelayClientManager(EventLoopGroup group) {
        this.group = group;
    }

    @Override
    public Promise<RelayClient> borrow(EventExecutor eventExecutor) {
        Promise<RelayClient> promise = eventExecutor.newPromise();
        DirectRelayClient directRelayClient = new DirectRelayClient(group);
        promise.setSuccess(directRelayClient);
        return promise;
    }

    @Override
    public void returnClient(RelayClient client) {
        DirectRelayClient c=(DirectRelayClient)client;
        c.close();
    }

}
