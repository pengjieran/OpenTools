package com.opentools.common;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @JsonIgnore 此注解用于属性上，作用是进行JSON操作时忽略该属性。
 * @JsonFormat 此注解用于属性上，作用是把Date类型直接转化为想要的格式，如@JsonFormat(pattern = "yyyy-MM-dd HH-mm-ss")。
 * @JsonProperty 此注解用于属性上，作用是把该属性的名称序列化为另外一个名称，如把trueName属性序列化为name，@JsonProperty("name")。
 * @author aaron
 *
 */
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
	
	/**
	 * ObjectMapper支持从byte[]、File、InputStream、字符串等数据的JSON反序列化
	 * @param objectMapper
	 * @param json
	 * @param classes
	 * @return
	 */
	public static <T> T parse(ObjectMapper objectMapper, String json, Class<T> classes) {
		
		 try {
			
			return objectMapper.readValue(json, classes);
		} catch (JsonParseException e) {
			e.printStackTrace();
			return null;
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
}