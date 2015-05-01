package com.opentools.web;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ognl.Ognl;
import ognl.OgnlException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 通过反射的方式操作对象中的属性,无视访问修饰符，不经过getter/setter方法
 * @author Aaron email:pjr0206@163.com
 *
 */
public final class ReflectionUtils {
	
	/**
	 * 从src拷贝fields数组中指定的属性值到dest中，无视访问修饰符private/protected,不经过getter函数
	 * @param src
	 * @param dest
	 * @param fields
	 * @throws OgnlException 
	 */
	public static void copyFields(final Object src, final Object dest, String[] fields ) throws OgnlException {
		
		for (String field : fields) {
			
			Object value = Ognl.getValue(field, src);
			
			Ognl.setValue(field, dest, value);
		}
	}
	
	/**
	 * 直接读取对象属性值，无视private/protected,不经过getter
	 * @param object
	 * @param fieldName
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static Object getFieldValue(final Object object, final String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		Field field = getDeclaredField(object.getClass(), fieldName);
		
		makeAccessible(field);
		
		Object result = field.get(object);
		
		return result;
	}
	
	/**
	 * 直接设置对象属性值,无视private/protected,不经过setter
	 * @param object
	 * @param fieldName
	 * @param value
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static void setFieldValue(final Object object, final String fieldName, final Object value) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		Field field = getDeclaredField(object.getClass(), fieldName);
		
		makeAccessible(field);
		
		field.set(object, value);
	}
	
	/**
	 * 循环向上转型,获取对象的DeclaredField.
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	protected static Field getDeclaredField(final Object object, final String fieldName) throws NoSuchFieldException, SecurityException {
		
		return getDeclaredField(object.getClass(), fieldName);
	}
	
	/**
	 * 循环向上转型，获取类的DeclaredField
	 * @param clazz
	 * @param fieldName
	 * @return
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 */
	@SuppressWarnings({ "rawtypes", "unused" })
	protected static Field getDeclaredField(final Class clazz, final String fieldName) throws NoSuchFieldException, SecurityException {
		
		for (Class superClazz = clazz; superClazz != Object.class; superClazz = superClazz.getSuperclass()) {
			
			return superClazz.getDeclaredField(fieldName);
		}
		
		return null ;
	}
	
	/**
	 * 强制转换field为可访问
	 * @param field
	 */
	public static void makeAccessible(final Field field) {
		
		if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
			
			field.setAccessible(true);
		}
	}
	
	/**
	 * 通过反射获取定义Class时声明的父类的泛型参数的类型
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Class getSuperClassGenricType(final Class clazz) {
		
		return getSuperClassGenricType(clazz, 0);
	}
	
	/**
	 * 通过反射，获取定义Class时声明的父类的泛型参数的类型
	 * @param clazz
	 * @param index
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Class getSuperClassGenricType(final Class clazz, int index) {
		
		Type type = clazz.getGenericSuperclass();
		
		if (!(type instanceof ParameterizedType)) {
			
			return Object.class;
		}

		Type[] params = ((ParameterizedType) type).getActualTypeArguments();

		if (index >= params.length || index < 0) {
			
			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			
			return Object.class;
		}
		return (Class) params[index];
	}
	
	/**
	 * 提取集合中的对象的属性,组合成List.
	 * 
	 * @param collection 来源集合.
	 * @param propertityName 要提取的属性名.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List fetchElementPropertyToList(final Collection collection, final String propertyName) throws Exception {

		List list = new ArrayList();

		for (Object obj : collection) {
			
			list.add(PropertyUtils.getProperty(obj, propertyName));
		}

		return list;
	}
	
	/**
	 * 提取集合中的对象的属性,组合成由分割符分隔的字符串.
	 * 
	 * @param collection 来源集合.
	 * @param propertityName 要提取的属性名.
	 * @param separator 分隔符.
	 */
	@SuppressWarnings({ "rawtypes" })
	public static String fetchElementPropertyToString(final Collection collection, final String propertyName, final String separator) throws Exception {
		
		List list = fetchElementPropertyToList(collection, propertyName);
		
		return StringUtils.join(list, separator);
	}
	
	/**
	 * 过滤属性的类型
	 * @param entityClass
	 * @param propertyName
	 * @return
	 */
	public static Class<?> getFieldType(Class<?> entityClass, String propertyName) {

		Class<?> propertyType = null;
		try {
			if (StringUtils.contains(propertyName, ".")) {
				for (String str : propertyName.split("\\.")) {
					Field declaredField = getDeclaredField(entityClass, str);
					entityClass = declaredField.getType();
				}
				propertyType = entityClass;
			} else {
				propertyType = getDeclaredField(entityClass, propertyName)
						.getType();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return propertyType;
	}
	
	private ReflectionUtils() {}
}
