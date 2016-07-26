package com.opentools.http;

/**
 * Created by aaron on 2016/7/25.
 *  存放http返回结果时常用的返回格式
 */
public interface ContentType {

    String KEY = "Content-Type";

    String doc = "application/msword";
    String docx = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    String docm = "application/vnd.ms-word.document.macroEnabled.12";
    String dot = "application/msword";
    String dotx = "application/vnd.openxmlformats-officedocument.wordprocessingml.template";
    String dotm = "application/vnd.ms-word.template.macroEnabled.12";
    String xls = "application/vnd.ms-excel";
    String xlsx = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    String xlsm = "application/vnd.ms-excel.sheet.macroEnabled.12";
    String xlt = "application/vnd.ms-excel";
    String xltx = "application/vnd.openxmlformats-officedocument.spreadsheetml.template";
    String xltm = "application/vnd.ms-excel.template.macroEnabled.12";
    String ppt = "application/vnd.ms-powerpoint";
    String pptx = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    String pptm = "application/vnd.ms-powerpoint.presentation.macroEnabled.12";
    String pot = "application/vnd.ms-powerpoint";
    String potx = "application/vnd.openxmlformats-officedocument.presentationml.template";
    String potm = "application/vnd.ms-powerpoint.presentation.macroEnabled.12";
    String pps = "application/vnd.ms-powerpoint";
    String ppsx = "application/vnd.openxmlformats-officedocument.presentationml.slideshow";
    String ppsm = "application/vnd.ms-powerpoint.slideshow.macroEnabled.12";
    String pdf = "application/pdf";
    String swf = "application/x-shockwave-flash";
    String js = "application/x-javascript";
    String dll = "application/octet-stream";
    String tar = "application/x-tar";
    String zip = "application/zip";
    String exe = "application/x-msdownload";
    String json = "application/json;charset=UTF-8";

    String img = "application/x-img";
    String bmp = "image/bmp";
    String gif = "image/gif";
    String jpeg = "image/jpeg";
    String jpg = "image/jpeg";
    String jpe = "image/jpeg";
    String png = "image/png";

    String htm = "text/html";
    String html = "text/html";
    String xml = "text/xml";
    String css = "text/css";
    String txt = "text/plain;charset=UTF-8";

    String mpeg = "video/mpeg";
    String mpg = "video/mpeg";
    String mpe = "video/mpeg";
    String mov = "video/quicktime";
    String avi = "video/avi";
    String movie = "video/x-sgi-movie";
    String mp4 = "video/mpeg4";
    String wmv = "video/x-ms-wmv";
    String wm = "video/x-ms-wm";
}
