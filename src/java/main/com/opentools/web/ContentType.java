package com.opentools.web;

/**
 * 表明发送请求时contentType部分的key和value值
 *
 * @author Aaron
 */
public interface ContentType {

    String CONTENTTYPE_KEY = "Content-type";

    String CONTENTTYPE_OCTET_VALUE = "application/octet-stream";//文件扩展名.*,二进制流，不知道下载文件类型

    String CONTENTTYPE_VIDEO_AVI_VALUE = "video/avi";//文件扩展名.avi,视频文件

    String CONTENTTYPE_HTML_VALUE = "text/html";//网页文件，包含html或者jsp等文件

    String CONTENTTYPE_JSON_VALUE = "application/json";//返回的是json数据

    String CONTENTTYPE_JSON_UTF8_VALUE = "application/json,charset=utf8";//utf8编码的json数据

}