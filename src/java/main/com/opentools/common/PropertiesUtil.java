package com.opentools.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 * 读取properties配置文件，写入配置信息
 * @author aaron
 *
 */
public class PropertiesUtil {

	/**
	 * 读取配置文件
	 * @param srcFile 文件绝对路径
	 * @return
	 */
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
	
	/**
	 * 向配置文件中写入数据
	 * @param srcFile 文件绝对路径
	 * @param key
	 * @param value
	 */
	public static void writeData(String srcFile, String key, String value) {
		
		Properties prop = loadProperties(srcFile);
		try {

			OutputStream fos = new FileOutputStream(srcFile);
			prop.setProperty(key, value);
			prop.store(fos, "Update '" + key + "' value");
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private PropertiesUtil() {}
}