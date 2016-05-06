package com.opentools.common;

import java.util.Random;

/**
 * 随机生成工具类
 * Created by aaron on 2016/5/6.
 */
public class RandomUtil {

    private static final String CHARACTER = "qwertyuiopasdfghjklzxcvbnm";

    private static final String NUMBER = "0123456789";

    /**
     * 随机生成指定长度的用户名
     * @param characterLength
     * @param numberLength
     * @return
     */
    public static String randomName(int characterLength, int numberLength) {

        Random random = new Random();
        StringBuilder nameBuilder = new StringBuilder();
        if (characterLength > 0) {

            for (int i = 0; i < characterLength; i++) {

                nameBuilder.append(CHARACTER.charAt(random.nextInt(25)));
            }
        }

        if (numberLength > 0) {

            for (int i = 0; i < characterLength; i++) {

                nameBuilder.append(NUMBER.charAt(random.nextInt(9)));
            }
        }

        return nameBuilder.toString();
    }
}