package com.opentools.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by aaron on 2016/9/23.
 */
public class PropertyUtil {

    /**
     * 首先，Java中的getResourceAsStream有以下几种：
     1. Class.getResourceAsStream(String path) ： path 不以’/'开头时默认是从此类所在的包下取资源，以’/'开头则是从ClassPath根下获取。其只是通过path构造一个绝对路径，最终还是由ClassLoader获取资源。

     2. Class.getClassLoader.getResourceAsStream(String path) ：默认则是从ClassPath根下获取，path不能以’/'开头，最终是由ClassLoader获取资源。

     3. ServletContext. getResourceAsStream(String path)：默认从WebAPP根目录下取资源，Tomcat下path是否以’/'开头无所谓，当然这和具体的容器实现有关。

     4. Jsp下的application内置对象就是上面的ServletContext的一种实现。

     其次，getResourceAsStream 用法大致有以下几种：

     第一： 要加载的文件和.class文件在同一目录下，例如：com.x.y 下有类me.class ,同时有资源文件myfile.xml

     那么，应该有如下代码：

     me.class.getResourceAsStream("myfile.xml");

     第二：在me.class目录的子目录下，例如：com.x.y 下有类me.class ,同时在 com.x.y.file 目录下有资源文件myfile.xml

     那么，应该有如下代码：

     me.class.getResourceAsStream("file/myfile.xml");

     第三：不在me.class目录下，也不在子目录下，例如：com.x.y 下有类me.class ,同时在 com.x.file 目录下有资源文件myfile.xml

     那么，应该有如下代码：

     me.class.getResourceAsStream("/com/x/file/myfile.xml");

     总结一下，可能只是两种写法

     第一：前面有 “   / ”

     “ / ”代表了工程的根目录，例如工程名叫做myproject，“ / ”代表了myproject

     me.class.getResourceAsStream("/com/x/file/myfile.xml");

     第二：前面没有 “   / ”

     代表当前类的目录

     me.class.getResourceAsStream("myfile.xml");

     me.class.getResourceAsStream("file/myfile.xml");

     最后，自己的理解：
     getResourceAsStream读取的文件路径只局限与工程的源文件夹中，包括在工程src根目录下，以及类包里面任何位置，但是如果配置文件路径是在除了源文件夹之外的其他文件夹中时，该方法是用不了的。
     * 从classpath根路径下加载properties配置文件
     * @param filePath
     * @return
     * @throws IOException
     */
    public static Properties loadProperties(String filePath) throws IOException {

        InputStream inputStream = ClassLoader.getSystemResourceAsStream(filePath);

        Properties properties = new Properties();
        properties.load(inputStream);

        return properties;
    }

    public static void main(String[] args) {

        try {
            Properties properties = loadProperties("common.properties");
            System.out.println(properties.getProperty("key"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PropertyUtil() {}
}
