package com.opentools.web;

/**
 * 表明发送请求时contentType部分的key和value值
 * @author Aaron
 *
 */
public class ContentType {
	
	public static final String CONTENTTYPE_KEY = "Content-type";
	
	public static final String CONTENTTYPE_OCTET_VALUE = "application/octet-stream";//文件扩展名.*,二进制流，不知道下载文件类型
	
	public static final String CONTENTTYPE_VIDEO_AVI_VALUE = "video/avi";//文件扩展名.avi,视频文件
	
	public static final String CONTENTTYPE_HTML_VALUE = "text/html";//网页文件，包含html或者jsp等文件
	
	private ContentType() {}

}