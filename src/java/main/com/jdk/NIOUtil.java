package com.jdk;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.channels.Channel;

/**
 * 处理与nio相关的工具类
 * @author Aaron
 *
 */
public class NIOUtil {

	@SuppressWarnings("resource")
	public static Channel fileToChannel(String filePath) throws FileNotFoundException {
		
		RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");
		
		return randomAccessFile.getChannel();
		
	}
}