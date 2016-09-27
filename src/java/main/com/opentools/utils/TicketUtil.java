package com.opentools.utils;

import com.opentools.http.HttpUtils;

/**
 * 查询火车票信息的工具类
 * Created by aaron on 2016/9/27.
 */
public class TicketUtil {

    public static void main(String[] args) {

        try {
            String s = HttpUtils.doSSLGet("https://kyfw.12306.cn/otn/resources/js/framework/station_name.js?station_version=1.8968");
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}