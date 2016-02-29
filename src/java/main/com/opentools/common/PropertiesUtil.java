package com.opentools.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * 读取properties配置文件，写入配置信息
 * @author aaron
 *
 */
public class PropertiesUtil {

	public static Properties loadProperties(String srcFile) {
		
		Properties properties = new Properties();
		FileInputStream inStream;
		try {
			inStream = new FileInputStream(new File(srcFile));
			properties.load(inStream);
			inStream.close();
			return properties;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private PropertiesUtil() {}
}