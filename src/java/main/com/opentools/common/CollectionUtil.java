package com.opentools.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 增加集合工具类，此处的一些方法可能都是自己项目中需要又没有现成的轮子时造出来的
 * @author aaron
 *
 */
public class CollectionUtil {
	
	/**
	 * 将map中的key处理成一个list,这部分代码不止一次用到，而其他轮子中好像又没有提供
	 * @param map
	 * @return
	 */
	public static List<Object> keyToList(Map<Object, Object> map) {
		
		Set<Object> keySet = map.keySet();
		List<Object> list = new ArrayList<>(keySet);
		return list;
	}
	
	/**
	 * 获取一个listmap中含有指定key值的map
	 * @param list
	 * @param key
	 * @return
	 */
	public static Map<Object, Object> get(List<Map<Object, Object>> list, Object key) {
		
		for (Map<Object, Object> map : list) {
			if (map.containsKey(key)) {
				
				return map;
			}
		}
		
		return null;
	}
	
	/**
	 * 更新一个listmap列表中的数据，
	 * @param list
	 * @param map 更新的数据
	 * @param add 没有时是否新增
	 * @return
	 */
	public static boolean updateListMap(List<Map<Object, Object>> list, Map<Object, Object> map, boolean add) {
		
		for (Map<Object, Object> destMap : list) {
			
			Set<Object> keySet = map.keySet();
			
			for (Object key : keySet) {
				
				Object object = destMap.get(key);
				if (null == object) {
					
					if (add) destMap.put(key, map.get(key));
					
				} else {
					
					destMap.replace(key, map.get(key));
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 过滤掉含有重复的指定key值的数据
	 * @param list
	 * 该方法危险，未经测试
	 */
	@Deprecated
	public static void filterList(List<Map<String, Object>> list, String key) {
		
		List<Object> keys = new ArrayList<>();
		for (Map<String, Object> map : list) {
			
			Object object = map.get(key);
			
			if (!keys.contains(object)) {
				
				keys.add(object);
			} else {
				
				list.remove(map);
			}
		}
	}
	
	private CollectionUtil(){}

}