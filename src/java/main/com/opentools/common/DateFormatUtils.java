package com.opentools.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 时间日期格式化和解析工具类
 * @author Dean		pjr0206@163.com
 *
 */

public final class DateFormatUtils {
	
	/**
	 * 所有的formatter的存放map,预置为7个
	 */
	private static Map<String, SimpleDateFormat> formatters = new HashMap<String, SimpleDateFormat>(7);
	
	/**
	 * 解析日期格式
	 * @param date	待解析的日期，如2014-08-01
	 * @param format	日期格式，如yyyy-MM-dd
	 * @return	Date
	 * @throws ParseException
	 */
	public static Date parse(String date, String format) throws ParseException {
		
		return getInstance(format).parse(date);
	}
	
	/**
	 * 格式化日期
	 * @param date	需要格式化的日期
	 * @param format	日期格式yyyy-MM-dd
	 * @return	String	格式过的日期字符串
	 */
	public static String format(Date date, String format) {
		
		return getInstance(format).format(date);
	}
	
	/**
	 * 获得SimpleDateFormat的实例
	 * @param format	例如yyyy-MM-dd
	 * @return	SimpleDateFormat
	 */
	private static SimpleDateFormat getInstance(String format) {
		
		initDateIfNecessary();
		SimpleDateFormat formatter = formatters.get(format);
		
		if (formatter == null) {
			
			formatter = new SimpleDateFormat(format);
		}
		
		return formatter;
		
	}
	
	/**
	 * 初始化数据
	 */
	private static void initDateIfNecessary() {
		
		if (formatters.isEmpty()) {
			
			formatters.put("yyyy-MM-dd", new SimpleDateFormat("yyyy-MM-dd"));
			formatters.put("yyyy-MM-dd HH:mm", new SimpleDateFormat("yyyy-MM-dd HH:mm"));
			formatters.put("yyyy-MM-dd HH:mm:ss", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
			formatters.put("HH:mm", new SimpleDateFormat("HH:mm"));
			formatters.put("HH:mm:ss", new SimpleDateFormat("HH:mm:ss"));
			formatters.put("MM-dd-yyyy", new SimpleDateFormat("MM-dd-yyyy"));
			formatters.put("yyyy年MM月dd日", new SimpleDateFormat("yyyy年MM月dd日"));
		}
	}
	
	private DateFormatUtils() {}
}
