package com.opentools.common;

/**
 * 获取系统参数的工具类
 * @author Aaron
 *
 */
public class SystemPropertyUtils {

	/**
	 * 获取行分隔符，行分隔符在windows 下是 \r\n，在Linux下面是 \n， 在Mac下是 \r
	 * @return
	 */
	public static String getLineSeparator()
	{
		return System.getProperty("line.separator");
	}

	/**
	 * 获取路径分隔符，路径分隔符在windows下是;，在LInux下是 :
	 * @return
	 */
	public static String getPathSeparator()
	{
		return System.getProperty("path.separator");
	}
	
	/**
	 * 获取路径分隔符，路径分隔符在windows下是 \ ，在LInux下是 /
	 * @return
	 */
	public static String getFileSeparator()
	{
		return System.getProperty("file.separator");
	}
	
	private SystemPropertyUtils(){}
}