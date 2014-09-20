package cc.aaron67.fetch.leetcode.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Config {
	private static Logger logger = Logger.getLogger(Config.class);

	private static Properties properties = new Properties();

	static {
		try {
			String configFilePath = System.getProperty("user.dir")
					+ "/init.properties";
			PropertyConfigurator.configure(configFilePath);
			properties.load(new BufferedInputStream(new FileInputStream(
					configFilePath)));
		} catch (IOException e) {
			logger.error("读配置文件出错\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public static String get(String key) {
		return properties.getProperty(key);
	}
}
