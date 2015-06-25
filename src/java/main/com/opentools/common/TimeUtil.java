package com.opentools.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {

	/**
	 * 距离当前时间七天之内和七天之外的日期
	 * 有一定差距，1000毫秒
	 * @param date
	 * @param type 0：当天，1:7天之内，2:7天之外
	 * @return
	 */
	public static boolean isFromToday(Date date, int type)
	{
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		Date today = calendar.getTime();
		
		long diff = today.getTime() - date.getTime();
		if (diff < 0) diff = 0;
		long days = diff/(1000*60*60*24);
		
		if (type == 0 && 0 == days) return true;
		if (1 == type && days > 0 && days <= 7) return true;
		if (2 == type && days > 7) return true;
		
		return false;
	}
	
	/**
	 * 获取今天的开始时间和明天的开始时间
	 * @param type，1：获取今天0时0分0秒的时间，2：获取明天0时0分0秒的时间
	 * @return
	 */
	public static Date getDate(int type)
	{
		LocalDate localDate = LocalDate.now();
		LocalDateTime localDateTime = localDate.atTime(0, 0, 0);
		
		if (1 == type)
		{
			return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
		}
		else
		{
			LocalDate date = localDate.plusDays(1);
			LocalDateTime time = date.atTime(0, 0, 0);
			return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
		}
		
	}
	
	private TimeUtil(){}
}