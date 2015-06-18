package com.opentools.common;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串处理工具类
 * 
 * @author Aaron
 * @date 2013-12-18 11:22
 */
public class StringUtil {

	/**
	 * 检查字符串是否为空
	 * 
	 * @param str
	 *            字符串
	 * @return
	 */
	public static boolean isEmpty(String src) {

		if (null != src && !"".equals(src) && !" ".equals(src)
				&& src.length() > 0 && !src.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * 检查字符串是否为空
	 * 
	 * @param str
	 *            字符串
	 * @return
	 */
	public static boolean isNotEmpty(String str) {
		if (str == null) {
			return false;
		} else if (str.length() == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 把字符串按分隔符转换为数组
	 * 
	 * @param str
	 *            字符串
	 * @param expr
	 *            分隔符
	 * @return
	 */
	public static String[] stringToArray(String str, String expr) {
		return str.split(expr);
	}

	/**
	 * 将数组按照给定的分隔转化成字符串
	 * 
	 * @param arr
	 * @param expr
	 * @return
	 */
	public static String arrayToString(String[] arr, String expr) {
		String strInfo = "";
		if (arr != null && arr.length > 0) {
			StringBuffer sf = new StringBuffer();
			for (String str : arr) {
				sf.append(str);
				sf.append(expr);
			}
			strInfo = sf.substring(0, sf.length() - 1);
		}
		return strInfo;
	}

	/**
	 * 将集合按照给定的分隔转化成字符串
	 * 
	 * @param arr
	 * @param expr
	 * @return
	 */
	public static String listToString(List<String> list, String expr) {
		String strInfo = "";
		if (list != null && list.size() > 0) {
			StringBuffer sf = new StringBuffer();
			for (String str : list) {
				sf.append(str);
				sf.append(expr);
			}
			strInfo = sf.substring(0, sf.length() - 1);
		}
		return strInfo;
	}

	/**
	 * 去除字符串中的空格，制表符，回车，换行符
	 * 
	 * @param src
	 * @return
	 */
	public static String replaceBlank(String src) {
		
		if (null != src)
		{
			Pattern p = Pattern.compile("\\s*|\t|\r|\n");
			Matcher m = p.matcher(src);
			src = m.replaceAll("");
		}

		return src;
	}

	
	private StringUtil() {}
}