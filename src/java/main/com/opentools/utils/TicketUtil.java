package com.opentools.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
	public static HttpResponse search(String startStation, String endStation, String time) {
		
		Map<String, String> city = getCity();
		String startKey = city.get(startStation);
		String endKey = city.get(endStation);
		
		String logUrl = "https://kyfw.12306.cn/otn/leftTicket/log";
		String url = "https://kyfw.12306.cn/otn/leftTicket/queryX";
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("leftTicketDTO.train_date", time);
		params.put("leftTicketDTO.from_station", startKey);
		params.put("leftTicketDTO.to_station", endKey);
		params.put("purpose_codes", "ADULT");
		
		Map<String, String> headers = new HashMap<>();
		headers.put("t", "6c602f1ec3dcab1658b4c7acad87bbc5");
		headers.put("Referer", "https://kyfw.12306.cn/otn/leftTicket/init");
		try {
			
			HttpUtils.sendGet(logUrl, params, headers);
			HttpResponse result = HttpUtils.sendGet(url, params, headers);
			return result;
		} catch (ClientProtocolException e) {
			
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			
			e.printStackTrace();
			return null;
		}
	}
	
	@Test
	public void Test() {
		
		HttpResponse response = search("信阳", "北京", "2016-10-30");
		try {
			
			InputStream stream = response.getEntity().getContent();
			String string = IOUtils.toString(stream);
			System.out.println(string);
		} catch (UnsupportedOperationException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

}