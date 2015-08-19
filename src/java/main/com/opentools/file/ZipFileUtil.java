package com.opentools.file;

import java.io.OutputStream;
import java.util.zip.ZipOutputStream;

/**
 * 压缩和解压缩文件相关的类
 * @author Peng
 *
 */
public class ZipFileUtil {

	public static ZipOutputStream zip(OutputStream outputStream)
	{
		ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
		
		return zipOutputStream;
	}
	
	private ZipFileUtil(){}
}