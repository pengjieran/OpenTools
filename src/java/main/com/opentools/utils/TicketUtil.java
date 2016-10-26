package com.opentools.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
		System.out.println(s);
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
	
	@Test
	public void Test() {
		
		Map<String, String> city = getCity();
		
		Set<String> set = city.keySet();
		System.out.println(set.size());
		for (String key : set) {
			
			System.out.println(key + "========" + city.get(key));
		}
		
		String string = city.get("信阳");
		System.out.println(string);
	}

}