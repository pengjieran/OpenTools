package com.opentools.common;

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
	
	private TimeUtil(){}
}