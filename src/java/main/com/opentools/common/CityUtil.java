package com.opentools.common;

import org.apache.http.NameValuePair;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用于获取世界主要城市的数据
 * Created by Aaron on 2016/5/21.
 */
public class CityUtil {

    public static String getAreas() {

        String timestamp = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");



        List<NameValuePair> nameValuePairList = Form.form().add("method", "taobao.areas.get").add("app_key", "23370620").add("timestamp", timestamp).add("format", "json").add("v", "2.0").add("sign_method","md5").build();

        try {

            String s = Request.Post("http://gw.api.taobao.com/router/rest").bodyForm(nameValuePairList).execute().returnContent().asString();
            System.out.println(s);
            return s;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String mapToString(Map<String, String> map) {

        StringBuilder stringBuilder = new StringBuilder();
        if (null != map && !map.isEmpty()) {


            List<String> lst = new ArrayList<>(map.keySet());
            for (String key : lst) {

                stringBuilder.append(key + "=" + map.get(key));
                if (!key.equals(lst.get(lst.size() - 1))) {

                    stringBuilder.append("&");
                }
            }
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {

        getAreas();

    }
}