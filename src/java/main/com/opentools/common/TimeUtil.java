package com.opentools.common;

import java.time.Instant;
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
	
	/**
	 * 判断给定时间是不是今天，无误差，但需要jdk8以上的版本支持
	 * @param date
	 * @return
	 */
	public static boolean isToday(Date date)
	{
		Instant instant = date.toInstant();
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		LocalDate localDate = localDateTime.toLocalDate();
		
		LocalDate now = LocalDate.now();
		
		return now.equals(localDate);
	}
	
	/**
	 * 获取精确的毫秒数，因为在windows下的时间粒度大概为15-16毫秒，而在linux下为1毫秒，有时候会造成时间不准的情况
	 * @return
	 */
	public static long getMSTime()
	{
		return 0;
		//return (System.nanoTime() / 1000000L);
	}
	
	public static void main(String[] args) {
		
		for (int i = 0; i < 100; i++)
		{
			System.out.println(System.currentTimeMillis());
			System.out.println(getMSTime());
		}
	}
	
	private TimeUtil(){}
}