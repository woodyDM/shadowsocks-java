package org.shadowsocks.netty.client;

/**
 * socksserver启动类
 * 
 * @author zhaohui
 * 
 */
public class Start {

	public static void main(String[] args) {
		SocksServer.getInstance().start();
	}
}
