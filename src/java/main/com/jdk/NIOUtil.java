package com.jdk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;

import org.apache.commons.io.IOUtils;

/**
 * 处理与nio相关的工具类
 * @author Aaron
 *
 */
public class NIOUtil {

	/**
	 * 将文件读入channel
	 * @param filePath
	 * @return
	 * @throws FileNotFoundException
     */
	@SuppressWarnings("resource")
	public static Channel fileToChannel(String filePath) throws FileNotFoundException {
		
		RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");
		
		return randomAccessFile.getChannel();
	}
	
	public static void readFromChannel(Channel channel, String filePath) throws FileNotFoundException {
		
		OutputStream out = new FileOutputStream(new File(filePath));
		FileChannel fileChannel = (FileChannel) channel;
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
		try {
			int read = fileChannel.read(byteBuffer);
			while (read != -1) {
				
				byteBuffer.flip();
				while (byteBuffer.hasRemaining()) {
					
					byte b = byteBuffer.get();
					byte[] bytes = new byte[]{b};
					IOUtils.write(bytes, out);
				}
				
				byteBuffer.clear();
				read = fileChannel.read(byteBuffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}