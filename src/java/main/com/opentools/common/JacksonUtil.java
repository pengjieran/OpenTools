package com.opentools.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JacksonUtil {

	/**
	 * 获取一个ObjectMapper
	 * @param serializationFeature
	 * @param deserializationFeature
	 * @return
	 */
	public static ObjectMapper getObjectMapper(
			SerializationFeature serializationFeature,
			DeserializationFeature deserializationFeature) {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(serializationFeature, true);
		objectMapper.configure(deserializationFeature, true);
		return objectMapper;
	}

	/**
	 * 获取默认配置的映射器
	 * 
	 * @return
	 */
	public static ObjectMapper getDefaultMapper() {

		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper;
	}

	/**
	 * ObjectMapper是JSON操作的核心，Jackson的所有JSON操作都是在ObjectMapper中实现。
	 * ObjectMapper有多个JSON序列化的方法，可以把JSON字符串保存File、OutputStream等不同的介质中。
	 * writeValue(File arg0, Object arg1)把arg1转成json序列，并保存到arg0文件中。
	 * writeValue(OutputStream arg0, Object arg1)把arg1转成json序列，并保存到arg0输出流中。
	 * writeValueAsBytes(Object arg0)把arg0转成json序列，并把结果输出成字节数组。
	 * writeValueAsString(Object arg0)把arg0转成json序列，并把结果输出成字符串。
	 */
	public static String toString(ObjectMapper objectMapper, Object object) {

		try {

			return objectMapper.writeValueAsString(object);
			
		} catch (JsonProcessingException e) {

			e.printStackTrace();
			return null;
		}
	}
}