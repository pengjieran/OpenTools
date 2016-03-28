package com.jdk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 流处理工具类
 * @author aaron
 * @since 2016年3月23日
 * @version 0.0.1
 */
public class StreamUtil {
	
	final static int BUFFER_SIZE = 4096;
	
	public static String InputStreamTOString(InputStream inputStream) throws IOException {
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] data = new byte[BUFFER_SIZE];
		String string = null;
		int count = 0;
		while ((count = inputStream.read(data, 0, BUFFER_SIZE)) != -1) {
			
			byteArrayOutputStream.write(data, 0, count);
			
		}
		
		string = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
		return string;
	}
	
	/**
	 * 将inputStream转换为指定字符编码的字符串
	 * @param in
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static String InputStreamToString(InputStream in, String charset) throws IOException {
		
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] data = new byte[BUFFER_SIZE];
		String string = null;
		int count = 0;
		while ((count = in.read(data, 0, BUFFER_SIZE)) != -1) {
			
			byteArrayOutputStream.write(data, 0, count);
			
		}
		
		string = new String(byteArrayOutputStream.toByteArray(), charset);
		return string;
	}

}