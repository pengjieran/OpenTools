package com.opentools.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.opentools.common.SystemPropertyUtils;

/**
 * 文件操作相关类
 * @author Aaron
 * @since 2015年6月21日
 */
public class FileUtil {
	
	/**
	 * 在文件末尾追加一行
	 * @param file
	 * @param src
	 * @return
	 * @throws IOException
	 */
	public static String appendLine(File file, String src) throws IOException
	{
		String lineSeparator = SystemPropertyUtils.getLineSeparator();
		RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
		long length = randomAccessFile.length();
		randomAccessFile.seek(length);
		String str = (lineSeparator + src);
		randomAccessFile.write(str.getBytes());
		randomAccessFile.close();
		
		return src;
	}
	
	private FileUtil() {}
}