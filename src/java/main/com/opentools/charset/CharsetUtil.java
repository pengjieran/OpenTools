package com.opentools.charset;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * 字符编码相关的工具类
 * @author Aaron
 * @since 2015年6月15日
 */
public class CharsetUtil
{
	
	/**
     * 7位ASCII字符，也叫作ISO646-US、Unicode字符集的基本拉丁块
     */
    public static final String US_ASCII = "US-ASCII";

    /**
     * ISO 拉丁字母表 No.1，也叫作 ISO-LATIN-1
     */
    public static final String ISO_8859_1 = "ISO-8859-1";

    /**
     * 8 位 UCS 转换格式
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * 16 位 UCS 转换格式，Big Endian（最低地址存放高位字节）字节顺序
     */
    public static final String UTF_16BE = "UTF-16BE";

    /**
     * 16 位 UCS 转换格式，Little-endian（最高地址存放低位字节）字节顺序
     */
    public static final String UTF_16LE = "UTF-16LE";

    /**
     * 16 位 UCS 转换格式，字节顺序由可选的字节顺序标记来标识
     */
    public static final String UTF_16 = "UTF-16";

    /**
     * 中文超大字符集
     */
    public static final String GBK = "GBK";
    
    /**
     * 将默认的编码方式转换为目标编码方式
     * @param srcStr
     * @param newCharSet
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String changeCharSet(String srcStr, String newCharSet) throws UnsupportedEncodingException
    {
    	if (null != srcStr)
    	{
    		byte[] bs = srcStr.getBytes();
    		return new String(bs, newCharSet);
    	}
    	
    	return null;
    }
    
    /**
     * 获取默认的字符集
     * @return
     */
    public static String getDefaultCharSet()
    {
    	OutputStreamWriter writer = new OutputStreamWriter(new ByteArrayOutputStream());
    	String encoding = writer.getEncoding();
    	return encoding;
    }

    /**
     * 
     * @param srcStr
     * @param oldCharSet
     * @param newCharSet
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String changeCharSet(String srcStr, String oldCharSet, String newCharSet) throws UnsupportedEncodingException
    {
    	
    	if (null != srcStr)
    	{
    		byte[] src = srcStr.getBytes(oldCharSet);
    		return new String(src, newCharSet);
    	}
    	
    	return null;
    }
}