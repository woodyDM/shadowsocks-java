package org.simplesocks.netty.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 加载pac配置xml
 * 
 * @author zhaohui
 * 
 */
public class PacLoader {

	private static Logger log = LoggerFactory.getLogger(PacLoader.class);

	private static List<String> domainList = new ArrayList<String>();
	private static List<String> tempList = new ArrayList<String>();
	/**是否是全局代理模式**/
	private static boolean _global_mode;

	/** 重加载的间隔时间 **/
	private static int reloadTime = 5;

	private static long lastModify;

	public static void load(final String filePath) throws Exception {

		loadFile(filePath);
		log.info("load pac at start.");
		Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				try {
					load(filePath);
				} catch (Exception e) {
					log.error("load pac error", e);
				}
			}
		}, reloadTime, reloadTime, TimeUnit.SECONDS);
	}

	private synchronized static void loadFile(String file) throws Exception {
		tempList.clear();
		InputStream in = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			in = PacLoader.class.getClassLoader().getResourceAsStream(file);

			Document doc = builder.parse(in);
			NodeList list = doc.getElementsByTagName("domain");

			if (list.getLength() > 0) {
				for (int j = 0; j < list.getLength(); j++) {
					tempList.add(list.item(j).getTextContent());
				}
			}
			setDomainList(tempList);

			NodeList globalList = doc.getElementsByTagName("global_mode");
			if (globalList.getLength() > 0) {
				String global = globalList.item(0).getTextContent();
				if ("true".equalsIgnoreCase(global)) {
					set_global_mode(true);
				} else {
					set_global_mode(false);
				}
			}

		} catch (Exception e) {
			throw e;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private synchronized static void setDomainList(List<String> tempList) {
		domainList.clear();
		domainList.addAll(tempList);
	}

	/**
	 * 指定的host是否需要代理
	 * 
	 * @param host
	 * @return
	 */
	public synchronized static boolean isProxy(String host) {
		String suffix;
		int pos = host.lastIndexOf('.');
		pos = host.lastIndexOf('.', pos - 1);
		while (true) {
			if (pos <= 0) {
				if (domainList.contains(host)) {
					return true;
				} else {
					return false;
				}
			}
			suffix = host.substring(pos + 1);
			if (domainList.contains(suffix)) {
				return true;
			}
			pos = host.lastIndexOf('.', pos - 1);
		}
	}

	public static boolean is_global_mode() {
		return _global_mode;
	}

	public static void set_global_mode(boolean _global_mode) {
		PacLoader._global_mode = _global_mode;
	}

}
