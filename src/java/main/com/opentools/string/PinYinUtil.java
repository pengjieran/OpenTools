package com.opentools.string;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Created by Aaron on 2016/7/20.
 */
public class PinYinUtil {

    /**
     * 将汉字转换为全大写拼音
     * @param src
     * @return
     * @throws Exception
     */
    public static String toPinYinUpperCase(String src) throws Exception {

        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        //全部大写
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        //不携带音标
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_U_AND_COLON);
        try {
            String s = PinyinHelper.toHanYuPinyinString(src, format, "", true);
            return s;
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            throw new Exception("无法转换拼音");
        }
    }

    /**
     * 将汉字转换为拼音
     * @param src
     * @param separate
     * @param format
     * @return
     * @throws Exception
     */
    public static String toPinYin(String src, String separate, HanyuPinyinOutputFormat format) throws Exception {

        try {
            String s = PinyinHelper.toHanYuPinyinString(src, format, separate, true);
            return s;
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            throw new Exception("无法转换拼音");
        }
    }

    /**
     *将汉字转换为拼音首字母大写，只保留首字母
     * @param src
     * @return
     * @throws Exception
     */
    public static String getFirstSpell(String src) throws Exception {

        StringBuilder strBuilder = new StringBuilder();
        char[] arr = src.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > 128) {
                try {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(arr[i], defaultFormat);
                    if (temp != null) {
                        strBuilder.append(temp[0].charAt(0));
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    e.printStackTrace();
                    throw new BadHanyuPinyinOutputFormatCombination("无法转换拼音");
                }
            } else {
                strBuilder.append(arr[i]);
            }
        }
        return strBuilder.toString().replaceAll("\\W", "").trim();
    }

    private PinYinUtil(){}
}