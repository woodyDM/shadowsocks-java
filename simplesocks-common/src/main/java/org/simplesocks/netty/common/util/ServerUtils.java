package org.simplesocks.netty.common.util;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import org.slf4j.Logger;

import java.io.IOException;

public final class ServerUtils {

	static final String PAGE = "https://github.com/woodyDM/simplesocks-java";

	/**
	 * Closes the specified channel after all queued write requests are flushed.
	 */
	public static void closeOnFlush(Channel ch) {
		if (ch.isActive()) {
			ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(
					ChannelFutureListener.CLOSE);
		}
	}

	public static void closeEventLoopGroup(EventLoopGroup group){
		if(group!=null){
			group.shutdownGracefully();
		}
	}

	public static void logException(Logger log, Throwable t){
		if(t instanceof IOException){
		    Throwable t2 = t.getCause()==null? t : t.getCause();
		    //usually caused by closed client channel or server.
			log.debug("IOException, {}",t2.getMessage());
		}else if(t.getClass().getName().equals("io.netty.channel.ExtendedClosedChannelException")){
		    log.warn("ExtendedClosedChannelException, channel may close when flushing.");
		}else {
            log.error("Exception happened :", t);
        }
	}


	public static void drawServerStartup(Logger log){
		log.info("_______________________________________________________");
		log.info("|                                                      |");
		log.info("|                SimpleSocksServer v0.0.2              |");
		log.info("|                                                      |");
		log.info("|      https://github.com/woodyDM/simplesocks-java     |");
		log.info("|                                                      |");
		log.info("-------------------------------------------------------");
	}

	public static void drawClientStartup(Logger log){
		log.info("_______________________________________________________");
		log.info("|                                                      |");
		log.info("|                SimpleSocksClient v0.0.2              |");
		log.info("|                                                      |");
		log.info("|      https://github.com/woodyDM/simplesocks-java     |");
		log.info("|                                                      |");
		log.info("-------------------------------------------------------");
	}

	private ServerUtils() {
	}
}
