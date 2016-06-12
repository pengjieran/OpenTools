package com.opentools.file;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author aaron
 * @version 0.1
 * @Description 文件名相关工具类
 */
public class FileNameUtil {

    /**
     * 获取文件扩展名
     * @param filePath
     * @return 不带.的扩展名，例如jpg
     */
    public String getExtension(String filePath) {

        return FilenameUtils.getExtension(filePath);
    }

    public static String getFullPathNoEndSeparator(String file) {

        return FilenameUtils.getFullPathNoEndSeparator(file);
    }

    public static String removeExtension(String fileName) {

        return FilenameUtils.removeExtension(fileName);
    }

    @Test
    public void test() {

        File file = new File("aaa.jpg");
        try {

            file.createNewFile();
            String fullPathNoEndSeparator = getFullPathNoEndSeparator("aaa.jpg");
            System.out.println(fullPathNoEndSeparator);
            String s = removeExtension("aaa.jpg");
            System.out.println(s);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}