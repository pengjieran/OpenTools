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

    @Test
    public void test() {

        File file = new File("aaa.jpg");
        try {

            file.createNewFile();
            String path = file.getAbsolutePath();
            String extension = getExtension(path);
            System.out.println(extension);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}