package com.opentools.common;


/**
 * 获取系统参数的工具类
 * 
 * @author Aaron 
 *         java.version Java 运行时环境版本
 * 
 *         java.vendor Java 运行时环境供应商
 * 
 *         java.vendor.url Java 供应商的 URL
 * 
 *         java.home Java 安装目录
 * 
 *         java.vm.specification.version Java 虚拟机规范版本
 * 
 *         java.vm.specification.vendor Java 虚拟机规范供应商
 * 
 *         java.vm.specification.name Java 虚拟机规范名称
 * 
 *         java.vm.version Java 虚拟机实现版本
 * 
 *         java.vm.vendor Java 虚拟机实现供应商
 * 
 *         java.vm.name Java 虚拟机实现名称
 * 
 *         java.specification.version Java 运行时环境规范版本
 * 
 *         java.specification.vendor Java 运行时环境规范供应商
 * 
 *         java.specification.name Java 运行时环境规范名称
 * 
 *         java.class.version Java 类格式版本号
 * 
 *         java.class.path Java 类路径
 * 
 *         java.library.path 加载库时搜索的路径列表
 * 
 *         java.io.tmpdir 默认的临时文件路径
 * 
 *         java.compiler 要使用的 JIT 编译器的名称
 * 
 *         java.ext.dirs 一个或多个扩展目录的路径
 * 
 *         os.name 操作系统的名称
 * 
 *         os.arch 操作系统的架构
 * 
 *         os.version 操作系统的版本
 * 
 *         file.separator 文件分隔符（在 UNIX 系统中是“/”）
 * 
 *         path.separator 路径分隔符（在 UNIX 系统中是“:”）
 * 
 *         line.separator 行分隔符（在 UNIX 系统中是“/n”）
 * 
 *         user.name 用户的账户名称
 * 
 *         user.home 用户的主目录
 * 
 *         user.dir 用户的当前工作目录
 *         
 *         catalina.base  tomcat服务器安装路径，例：/usr/local/tomcat
 */
public class SystemPropertyUtils {

	/**
	 * 获取行分隔符，行分隔符在windows 下是 \r\n，在Linux下面是 \n， 在Mac下是 \r
	 * 
	 * @return
	 */
	public static String getLineSeparator() {
		return System.getProperty("line.separator");
	}

	/**
	 * 获取路径分隔符，路径分隔符在windows下是;，在LInux下是 :
	 * 
	 * @return
	 */
	public static String getPathSeparator() {
		return System.getProperty("path.separator");
	}

	/**
	 * 获取路径分隔符，路径分隔符在windows下是 \ ，在LInux下是 /
	 * 
	 * @return
	 */
	public static String getFileSeparator() {
		return System.getProperty("file.separator");
	}
	
	public static String getOSName() {
		return System.getProperty("os.name");
	}

	private SystemPropertyUtils() {
	}
}