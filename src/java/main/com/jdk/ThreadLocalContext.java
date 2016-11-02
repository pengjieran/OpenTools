package com.jdk;

import java.util.HashMap;
import java.util.Map;

/**
 * 将数据存储于本地threadlocal中
 * @author aaron
 *
 */
public final class ThreadLocalContext {
	
	private static ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<Map<String, Object>>() {
		
		@Override
		protected Map<String, Object> initialValue() {
			
			return new HashMap<String, Object>();
		}
	};

	public static void put(String key, Object value) {
		
		threadLocal.get().put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T get(String key) {
		
		return ((T) threadLocal.get().get(key));
	}
	
	public static void reset() {
		
		threadLocal.get().clear();
	}
}