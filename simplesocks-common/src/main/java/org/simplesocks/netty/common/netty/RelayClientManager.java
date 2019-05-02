package org.simplesocks.netty.common.netty;

import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import org.simplesocks.netty.common.netty.RelayClient;

public interface RelayClientManager {


    Promise<RelayClient> borrow(EventExecutor eventExecutor, SocksCmdRequest socksCmdRequest);

    void returnClient(RelayClient client);

}
