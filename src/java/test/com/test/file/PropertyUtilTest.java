package com.test.file;

import com.opentools.file.PropertyUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Aaron on 2016/9/25.
 */
public class PropertyUtilTest {

    @Test
    public void testLoad() {

        try {
            Properties properties = PropertyUtil.loadProperties("common.properties");
            Assert.assertEquals(properties.getProperty("key"), "value");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWriteData() {

        Map<String, String> map = new HashMap<>();
        map.put("userId","123456");
        try {
            PropertyUtil.writeProperties(map, "common.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}