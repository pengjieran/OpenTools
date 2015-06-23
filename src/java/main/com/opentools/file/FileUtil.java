package com.opentools.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.FileNameMap;
import java.net.URLConnection;

import com.opentools.common.SystemPropertyUtils;

/**
 * 文件操作相关类
 * @author Aaron
 * @since 2015年6月21日
 */
public class FileUtil {
	
	/**
	 * 在文件末尾追加一行,以默认编码方式写入
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
	
	/**
	 * 在文件末尾追加一行，以指定编码方式追加
	 * @param file
	 * @param str
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String appendLine(File file, String str, String encoding) throws IOException
	{
		String lineSeparator = SystemPropertyUtils.getLineSeparator();
		RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
		long length = randomAccessFile.length();
		randomAccessFile.seek(length);
		String src = (lineSeparator + str);
		randomAccessFile.write(src.getBytes(encoding));
		randomAccessFile.close();
		
		return str;
	}
	
	/**
	 * 获取文件的mime类型
	 * @param file
	 * @return
	 */
	public static String getMimeType(String file)
	{
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String type = fileNameMap.getContentTypeFor(file);
		return type;
	}
	
	private FileUtil() {}
}