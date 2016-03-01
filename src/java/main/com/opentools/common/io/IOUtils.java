package com.opentools.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 
 * @author Aaron
 *
 */
public class IOUtils {

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
	
	/**
	 * 将输出流中的数据读出为byte数组
	 * @param is 输出流
	 * @param closed 是否关闭输出流
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray(InputStream is, boolean closed) throws IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		byte[] data = new byte[1024];
		while(-1 != is.read(data)) {
			
			baos.write(data);
		}
		
		byte[] array = baos.toByteArray();
		if (closed) is.close();
		
		baos.flush();
		baos.close();
		return array;
	}
	
	private IOUtils() {}
}