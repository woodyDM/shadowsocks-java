package org.simplesocks.netty.server.proxy.relay;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import org.simplesocks.netty.common.protocol.ProxyDataRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 *
 * target server data to local server
 * @author
 *
 */
public class TargetServerDataHandler extends ChannelInboundHandlerAdapter {

	private static Logger log = LoggerFactory.getLogger(TargetServerDataHandler.class);
	private Channel toLocalServerChannel;
	public TargetServerDataHandler(Channel remoteServerChannel) {
		this.toLocalServerChannel = remoteServerChannel;
	}



	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf bytes = (ByteBuf) msg;
		log.info("write target server data to local server. len {}",bytes.readableBytes());
		try{
			int len = bytes.readableBytes();
			byte[] bytes1 = new byte[len];
			bytes.readBytes(bytes1);
            ProxyDataRequest request = new ProxyDataRequest(bytes1);
            toLocalServerChannel.writeAndFlush(request);
		}finally {
			ReferenceCountUtil.release(bytes);
		}
	}


	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.close();
		log.debug("TargetServerDataHandler channelInactive close {}",ctx.channel().remoteAddress());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
		log.error("exception ",cause);
	}

}