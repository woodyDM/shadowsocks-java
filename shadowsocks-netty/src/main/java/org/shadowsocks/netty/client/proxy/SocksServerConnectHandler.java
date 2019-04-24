package org.shadowsocks.netty.client.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.shadowsocks.netty.client.config.PacLoader;
import org.shadowsocks.netty.client.config.RemoteServer;
import org.shadowsocks.netty.client.encryption.CryptFactory;
import org.shadowsocks.netty.client.encryption.ICrypt;
import org.shadowsocks.netty.client.manager.RemoteServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

@ChannelHandler.Sharable
public final class SocksServerConnectHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {

	private static Logger logger = LoggerFactory.getLogger(SocksServerConnectHandler.class);

	private final Bootstrap b = new Bootstrap();
	private ICrypt _crypt;
	private RemoteServer remoteServer;
	private boolean isProxy = true;

	public SocksServerConnectHandler() {
		this.remoteServer = RemoteServerManager.getRemoteServer();
		this._crypt = CryptFactory.get(remoteServer.get_method(), remoteServer.get_password());
	}

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, final SocksCmdRequest request) throws Exception {
		Promise<Channel> promise = ctx.executor().newPromise();
		promise.addListener(new GenericFutureListener<Future<Channel>>() {
			@Override
			public void operationComplete(final Future<Channel> future) throws Exception {
				final Channel outboundChannel = future.getNow();
				if (future.isSuccess()) {
					final InRelayHandler inRelay = new InRelayHandler(ctx.channel(), SocksServerConnectHandler.this);
					final OutRelayHandler outRelay = new OutRelayHandler(outboundChannel,
							SocksServerConnectHandler.this);

					ctx.channel().writeAndFlush(getSuccessResponse(request)).addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture channelFuture) {
							try {
								if (isProxy) {
									sendConnectRemoteMessage(request, outboundChannel);
								}

								ctx.pipeline().remove(SocksServerConnectHandler.this);
								outboundChannel.pipeline().addLast(inRelay);
								ctx.pipeline().addLast(outRelay);
							} catch (Exception e) {
								logger.error("", e);
							}
						}
					});
				} else {
					ctx.channel().writeAndFlush(getFailureResponse(request));
					SocksServerUtils.closeOnFlush(ctx.channel());
				}
			}
		});

		final Channel inboundChannel = ctx.channel();
		b.group(inboundChannel.eventLoop()).channel(NioSocketChannel.class)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000).option(ChannelOption.SO_KEEPALIVE, true)
				.handler(new DirectClientHandler(promise));

		setProxy(request.host());

		logger.info("host = " + request.host() + ",port = " + request.port() + ",isProxy = " + isProxy);

		b.connect(getIpAddr(request), getPort(request)).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (!future.isSuccess()) {
					ctx.channel().writeAndFlush(getFailureResponse(request));
					SocksServerUtils.closeOnFlush(ctx.channel());
				}
			}
		});
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		SocksServerUtils.closeOnFlush(ctx.channel());
	}

	public void setProxy(String host) {
		if (PacLoader.is_global_mode()) {
			isProxy = true;
		} else {
			isProxy = PacLoader.isProxy(host);
		}
	}

	/**
	 * 获取远程ip地址
	 * 
	 * @param request
	 * @return
	 */
	private String getIpAddr(SocksCmdRequest request) {
		if (isProxy) {
			return remoteServer.get_ipAddr();
		} else {
			return request.host();
		}
	}

	/**
	 * 获取远程端口
	 * 
	 * @param request
	 * @return
	 */
	private int getPort(SocksCmdRequest request) {
		if (isProxy) {
			return remoteServer.get_port();
		} else {
			return request.port();
		}
	}

	private SocksCmdResponse getSuccessResponse(SocksCmdRequest request) {
		return new SocksCmdResponse(SocksCmdStatus.SUCCESS, SocksAddressType.IPv4);
	}

	private SocksCmdResponse getFailureResponse(SocksCmdRequest request) {
		return new SocksCmdResponse(SocksCmdStatus.FAILURE, SocksAddressType.IPv4);
	}

	/**
	 * localserver和remoteserver进行connect发送的数据
	 * 
	 * @param request
	 * @param outboundChannel
	 */
	private void sendConnectRemoteMessage(SocksCmdRequest request, Channel outboundChannel) {
		ByteBuf buff = Unpooled.buffer();
		request.encodeAsByteBuf(buff);
		if (buff.hasArray()) {
			int len = buff.readableBytes();
			byte[] arr = new byte[len];
			buff.getBytes(0, arr);
			byte[] data = remoteByte(arr);
			sendRemote(data, data.length, outboundChannel);
		}
	}

	/**
	 * localserver和remoteserver进行connect发送的数据
	 * 
	 * +-----+-----+-------+------+----------+----------+ | VER | CMD | RSV |
	 * ATYP | DST.ADDR | DST.PORT |
	 * +-----+-----+-------+------+----------+----------+ | 1 | 1 | X'00' | 1 |
	 * Variable | 2 | +-----+-----+-------+------+----------+----------+
	 * 
	 * 需要跳过前面3个字节
	 * 
	 * @param data
	 * @return
	 */
	private byte[] remoteByte(byte[] data) {
		int dataLength = data.length;
		dataLength -= 3;
		byte[] temp = new byte[dataLength];
		System.arraycopy(data, 3, temp, 0, dataLength);
		return temp;
	}

	/**
	 * 给remoteserver发送数据--需要进行加密处理
	 * 
	 * @param data
	 * @param length
	 * @param channel
	 */
	public void sendRemote(byte[] data, int length, Channel channel) {
		ByteArrayOutputStream _remoteOutStream = null;
		try {
			_remoteOutStream = new ByteArrayOutputStream();
			if (isProxy) {
				_crypt.encrypt(data, length, _remoteOutStream);
				data = _remoteOutStream.toByteArray();
			}
			channel.writeAndFlush(Unpooled.wrappedBuffer(data));
		} catch (Exception e) {
			logger.error("sendRemote error", e);
		} finally {
			if (_remoteOutStream != null) {
				try {
					_remoteOutStream.close();
				} catch (IOException e) {
				}
			}
		}
		logger.debug("sendRemote message:isProxy = " + isProxy + ",length = " + length + ",channel = " + channel);
	}

	/**
	 * 给本地客户端回复消息--需要进行解密处理
	 * 
	 * @param data
	 * @param length
	 * @param channel
	 */
	public void sendLocal(byte[] data, int length, Channel channel) {
		ByteArrayOutputStream _localOutStream = null;
		try {
			_localOutStream = new ByteArrayOutputStream();
			if (isProxy) {
				_crypt.decrypt(data, length, _localOutStream);
				data = _localOutStream.toByteArray();
			}
			channel.writeAndFlush(Unpooled.wrappedBuffer(data));
		} catch (Exception e) {
			logger.error("sendLocal error", e);
		} finally {
			if (_localOutStream != null) {
				try {
					_localOutStream.close();
				} catch (IOException e) {
				}
			}
		}
		logger.debug("sendLocal message:isProxy = " + isProxy + ",length = " + length + ",channel = " + channel);
	}

}
