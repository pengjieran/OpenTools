package com.opentools.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opentools.collection.CollectionUtil;

/**
 * 输出相关的工具类，主要用于各种格式的数据输出
 * @author aaron
 *
 */
public class PrintUtil {
	
	/**
	 * 将一个map转换为字符串输出，输出格式为{"key"：value}
	 * @param map
	 * @return
	 */
	public static StringBuilder printMap(Map<String, Object> map) {
		
		StringBuilder strBuilder = new StringBuilder();
		Set<String> keySet = map.keySet();
		List<String> keys = new ArrayList<>(keySet);
		
		strBuilder.append("{");
		for (String key : keys) {
			
			strBuilder.append("\"" + key + "\":");
			strBuilder.append(map.get(key).toString());
			
			if (!key.equals(keys.get(keys.size() - 1))) {
				
				strBuilder.append(",");
			}
		}
		
		strBuilder.append("}");
		
		return strBuilder;
	}
	
	/**
	 * 输出listshuju
	 * @auther aaron
	 * 
	 * @param list
	 * @return
	 */
	public static StringBuffer printList(List<String> list) {
		
		StringBuffer strBuffer = new StringBuffer();
		if (CollectionUtil.isNotEmpty(list)) {
			
			for (String str : list) {
				
				strBuffer.append(str);
				if (!str.equals(list.get(list.size() - 1))) {
					strBuffer.append(",");
				}
			}
		}
		return strBuffer;
	}

}