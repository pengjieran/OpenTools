package com.opentools.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.opentools.common.StringUtil;
import com.opentools.http.HttpUtils;

/**
 * 查询火车票信息的工具类
 * Created by aaron on 2016/9/27.
 */
public class TicketUtil {
	
	/**
	 * 获取城市对应的拼音简写
	 * 运行之前先要确定jdk的ca库里已经导入了12306的CA证书
	 * @auther aaron
	 * 
	 * @return
	 */
	public static Map<String, String> getCity() {
		
		String s = HttpUtils.sendGet("https://kyfw.12306.cn/otn/resources/js/framework/station_name.js?station_version=1.8971", null);
		//获取数据
		String[] strings = s.split("=");
		String str = strings[1];
		str = str.substring(1, str.length() - 1);
		//分割地址信息
		String[] split = str.split("@");
		Map<String, String> result = new HashMap<>();
		for (String st : split) {
			
			if (StringUtil.isNotEmpty(st)) {
				
				String[] strings2 = StringUtils.split(st, "|");
				result.put(strings2[1], strings2[2]);
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @auther aaron
	 * 
	 * @param startStation
	 * @param endStation
	 * @param time
	 * @return
	 */
	public static String search(String startStation, String endStation, String time) {
		
		Map<String, String> city = getCity();
		String startKey = city.get(startStation);
		String endKey = city.get(endStation);
		System.out.println(startKey + "===" + endKey + "===" + time);
		return null;
	}
	
	@Test
	public void Test() {
		
		search("信阳", "北京", "2016-10-30");
	}

}