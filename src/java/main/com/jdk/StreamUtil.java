package com.jdk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
	
	/**
	 * 将Object对象转换为byte数组
	 * @param object
	 * @return
	 * @throws IOException
	 */
	public static byte[] toBytes(Object object) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(object);
		oos.flush();
		byte[] bytes = bos.toByteArray();
		oos.close();
		bos.close();
		return bytes;
	}
	
	/**
	 * 将byte数组转换为对象
	 * @param bytes
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Object object = ois.readObject();
		bis.close();
		ois.close();
		return object;
	}
}