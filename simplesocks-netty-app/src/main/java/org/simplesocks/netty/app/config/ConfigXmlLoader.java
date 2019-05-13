//package org.simplesocks.netty.app.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.w3c.dom.Document;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import java.io.IOException;
//import java.io.InputStream;
//
///**
// * 加载Config配置xml
// *
// * @author zhaohui
// *
// */
//@Slf4j
//public class ConfigXmlLoader {
//
//
//
//	public static AppConfiguration load(String file) throws Exception {
//		InputStream in = null;
//		try {
//			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//			in = ConfigXmlLoader.class.getClassLoader().getResourceAsStream(file);
//			Document doc = builder.parse(in);
//			NodeList list = doc.getElementsByTagName("conf");
//
//			AppConfiguration config = new AppConfiguration();
//			if (list.getLength() > 0) {
//				Node node = list.item(0);
//				NodeList childs = node.getChildNodes();
//
//				for (int j = 0; j < childs.getLength(); j++) {
//					if ("local_port".equals(childs.item(j).getNodeName())) {
//						config.setLocalPort(Integer.parseInt(childs.item(j).getTextContent()));
//					}
//				}
//
//				NodeList remoteList = doc.getElementsByTagName("remote");
//				if (remoteList.getLength() > 0) {
//					for (int j = 0; j < remoteList.getLength(); j++) {
//						Node remote = remoteList.item(j);
//						NodeList remoteChilds = remote.getChildNodes();
//						RemoteServer remoteConfig = new RemoteServer();
//						for (int k = 0; k < remoteChilds.getLength(); k++) {
//							if ("ip_addr".equals(remoteChilds.item(k).getNodeName())) {
//								remoteConfig.set_ipAddr(remoteChilds.item(k).getTextContent());
//							} else if ("port".equals(remoteChilds.item(k).getNodeName())) {
//								remoteConfig.set_port(Integer.parseInt(remoteChilds.item(k).getTextContent()));
//							} else if ("method".equals(remoteChilds.item(k).getNodeName())) {
//								remoteConfig.set_method(remoteChilds.item(k).getTextContent());
//							} else if ("password".equals(remoteChilds.item(k).getNodeName())) {
//								remoteConfig.set_password(remoteChilds.item(k).getTextContent());
//							}
//						}
//
//						config.addRemoteConfig(remoteConfig);
//					}
//				}
//			}
//			log.info("load config !");
//			return config;
//		} catch (Exception e) {
//			throw e;
//		} finally {
//			if (in != null) {
//				try {
//					in.close();
//				} catch (IOException e) {
//				}
//			}
//		}
//	}
//}
