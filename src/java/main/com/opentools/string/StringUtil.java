package com.opentools.string;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.WordUtils;

/**
 * the string tools
 * 
 * @author Aaron
 * 
 */
public class StringUtil {

	/**
	 * 将字符串依据驼峰法转化.注意,该方法只会将delimiter后的字符给大写,不在delimiter后的字符即便是大写也可能被
	 * 转换成小写,比如productType_MODEL会被转换为product_type_model
	 */
	public static String toCamelcase(String str, char[] delimiters) {

		String result = WordUtils.capitalizeFully(str, delimiters);
		for (char c : delimiters) {

			result = result.replace(String.valueOf(c), "");
		}
		int len = result.length();
		result = result.substring(0, 1).toLowerCase() + result.substring(1, len);

		return result;
	}

	/**
	 * 分割字符串(忽略指定区块内的内容)
	 * 如input("foo,bar,c;qual=\"baz,blurb\",d;junk=\"quux,syzygy\"", ",",
	 * "\\\"", , "\\\"")
	 * ouput["foo","bar","c;qual=\"baz,blurb\"","d;junk=\"quux,syzygy\""]
	 * 
	 * @param str
	 *            待分割字符串
	 * @param splitter
	 *            分割符
	 * @param ignoreBlockLeftDelimiter
	 *            忽略区块左定界符
	 * @param ignoreBlockLeftDelimiter
	 *            忽略区块右定界符
	 * 
	 * @return String[]
	 */
	public static String[] split(String str, String splitter, String ignoreBlockLeftDelimiter,
			String ignoreBlockRightDelimiter) {

		// String regEx = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
		// String regEx = splitter + "(?=([^" + ignoreBlockLeftDelimiter + "]*"
		// + ignoreBlockRightDelimiter
		// + "[^" + ignoreBlockLeftDelimiter + "]*" + ignoreBlockRightDelimiter
		// + ")*[^" + ignoreBlockRightDelimiter + "]*$)";

		String escapedIgnoreBlockLeftDelimiter = escapeForRegEx(ignoreBlockLeftDelimiter);
		String escapedIgnoreBlockRightDelimiter = escapeForRegEx(ignoreBlockRightDelimiter);

		StringBuilder regEx = new StringBuilder();
		regEx.append(escapeForRegEx(splitter));
		regEx.append("(?=([^");
		regEx.append(escapedIgnoreBlockLeftDelimiter);
		regEx.append("]*");
		regEx.append(escapedIgnoreBlockRightDelimiter);
		regEx.append("[^");
		regEx.append(escapedIgnoreBlockLeftDelimiter);
		regEx.append("]*");
		regEx.append(escapedIgnoreBlockRightDelimiter);
		regEx.append(")*[^");
		regEx.append(escapedIgnoreBlockRightDelimiter);
		regEx.append("]*$)");

		return str.split(regEx.toString());
	}

	/**
	 * 仿sql语句中的like语法
	 * 
	 * @param str
	 *            字符串
	 * @param expr
	 *            匹配表达式
	 * @param ignoreCase
	 *            是否忽略大小写
	 * 
	 * @return boolean
	 */
	public static boolean like(String str, String expr, boolean ignoreCase) {

		if (ignoreCase) {

			expr = expr.toLowerCase(); // ignoring locale for now
			str = str.toLowerCase();
		}

		// expr = Pattern.quote(expr);
		expr = expr.replace(".", "\\."); // "\\" is escaped to "\"
		// ... escape any other potentially problematic characters here
		expr = expr.replace("?", ".");
		expr = expr.replace("%", ".*");
		// expr = Pattern.quote(expr);
		// expr = expr.replace("(", "\\u0028");
		// expr = expr.replace(")", "\\u0029");
		// TODO,这里转义圆括号实际上是针对项目的特殊情况,将来应该重构成更通用的,如Pattern.quote
		expr = expr.replace("(", "\\(");
		expr = expr.replace(")", "\\)");

		return str.matches(expr);
	}

	/**
	 * 转换富文本内容,使适合于html页面显示
	 * 
	 * @param sor
	 * @return
	 */
	public static String change4html(String sor) {
		if (sor != null) {
			String dst = sor.replaceAll("\"", "'");// 目前只进行"转换为'
			return dst;
		} else {
			return sor;
		}

	}

	/**
	 * 将byte数组转换为表示16进制值的字符串， 如：byte[]{8,18}转换为：0813， 和public static byte[]
	 * hexStr2ByteArr(String strIn) 互为可逆的转换过程
	 * 
	 * @param arrB
	 *            需要转换的byte数组
	 * @return 转换后的字符串
	 * @throws Exception
	 *             本方法不处理任何异常，所有异常全部抛出
	 */
	public static String byteArr2HexStr(byte[] arrB) throws Exception {
		int iLen = arrB.length;
		// 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍
		StringBuffer sb = new StringBuffer(iLen * 2);
		for (int i = 0; i < iLen; i++) {
			int intTmp = arrB[i];
			// 把负数转换为正数
			while (intTmp < 0) {
				intTmp = intTmp + 256;
			}
			// 小于0F的数需要在前面补0
			if (intTmp < 16) {
				sb.append("0");
			}
			sb.append(Integer.toString(intTmp, 16));
		}
		return sb.toString();
	}

	/**
	 * 将二进制转换为字符串
	 * 
	 * @param b
	 * @return
	 */
	public static String byte2hex(byte[] b) {
		StringBuffer sb = new StringBuffer();
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0XFF);
			if (stmp.length() == 1) {
				sb.append("0" + stmp);
			} else {
				sb.append(stmp);
			}

		}
		return sb.toString();
	}

	/**
	 * 将字符串转换为二进制
	 * 
	 * @param str
	 * @return
	 */
	public static byte[] hex2byte(String str) {

		if (str == null)
			return null;
		str = str.trim();
		int len = str.length();
		if (len == 0 || len % 2 == 1)
			return null;
		byte[] b = new byte[len / 2];
		try {
			for (int i = 0; i < str.length(); i += 2) {
				b[i / 2] = (byte) Integer.decode("0X" + str.substring(i, i + 2)).intValue();
			}
			return b;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 将带有格式的字符串转换成HTML方式
	 * 
	 * @param origine
	 * @return
	 */
	public static String convert2Html(String origine) {
		String outStr = null;
		if (origine != null) {
			String tmp = StringUtil.replace(origine, ">", "&gt;");
			String tmp2 = StringUtil.replace(tmp, "<", "&lt;");
			// String tmp3 = StringUtils.replace(tmp2," ","&nbsp;");

			String tmp4 = StringUtil.replace(tmp2, "\r\n", "<br>");

			String tmp5 = StringUtil.replace(tmp4, "\n", "<br>");

			String tmp6 = StringUtil.replace(tmp5, "&lt;br&gt;", "<br>");

			outStr = tmp6;
		} else {
			outStr = "";
		}
		return outStr;
	}

	public static String convert2Html2(String origine) {
		String outStr = null;
		if (origine != null) {
			String tmp = StringUtil.replace(origine, ">", "&gt;");
			String tmp2 = StringUtil.replace(tmp, "<", "&lt;");
			outStr = tmp2;
		} else {
			outStr = "";
		}
		return outStr;
	}

	public static String convert2Text(String origine) {
		String outStr = null;
		if (origine != null) {
			String tmp = StringUtil.replace(origine, "&gt;", ">");
			String tmp2 = StringUtil.replace(tmp, "&lt;", "<");
			outStr = tmp2;
		} else {
			outStr = "";
		}
		return outStr;
	}

	/**
	 * 去掉HTML格式
	 * 
	 * @param origine
	 * @return
	 */
	public static String removeHtml(String origine) {
		String outStr = null;
		if (origine != null) {
			outStr = origine.replaceAll("<[^<^>]*>|&nbsp;", "").replaceAll(" ", "");
		} else {
			outStr = "";
		}
		return outStr;
	}

	/**
	 * Compares two Strings, returns true if their values are the same.
	 * 
	 * @param s1
	 *            The first string.
	 * @param s2
	 *            The second string.
	 * @return True if the values of both strings are the same.
	 */
	public static boolean equals(String s1, String s2) {
		if (s1 == null)
			return (s2 == null);
		else if (s2 == null)
			// s1 is not null
			return false;
		else
			return s1.equals(s2);
	}

	/**
	 * Makes the first letter caps and leaves the rest as is.
	 */
	public static String firstLetterCaps(String data) {
		StringBuffer sbuf = new StringBuffer(data.length());
		sbuf.append(data.substring(0, 1).toUpperCase());
		sbuf.append(data.substring(1));
		return sbuf.toString();
	}

	/**
	 * 将表示16进制值的字符串转换为byte数组， 和public static String byteArr2HexStr(byte[] arrB)
	 * 互为可逆的转换过程
	 * 
	 * @param strIn
	 *            需要转换的字符串
	 * @return 转换后的byte数组
	 * @throws Exception
	 *             本方法不处理任何异常，所有异常全部抛出
	 */
	public static byte[] hexStr2ByteArr(String strIn) throws Exception {
		byte[] arrB = strIn.getBytes();
		int iLen = arrB.length;
		// 两个字符表示一个字节，所以字节数组长度是字符串长度除以2
		byte[] arrOut = new byte[iLen / 2];
		for (int i = 0; i < iLen; i = i + 2) {
			String strTmp = new String(arrB, i, 2);
			arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
		}
		return arrOut;
	}

	/**
	 * 检查字符串是否是空的
	 * @auther aaron
	 * 
	 * @param foo
	 * @return
	 */
	public static final boolean isEmpty(String foo) {
		
		return (foo == null || foo.trim().length() == 0);
	}

	/**
	 * 检查字符串是否不为空
	 * @auther aaron
	 * 
	 * @param src
	 * @return
	 */
	public static final boolean isNotEmpty(String src) {
		
		return !isEmpty(src);
	}
	
	/**
	 * convert the ISO char encoding to GBK
	 * 
	 * @param str
	 *            the ISO encoding string
	 * @return the GBK encoding string
	 */
	public static String ISOtoGBK(String str) {

		byte[] by = null;
		try {
			by = str.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			return str;
		}
		try {
			String a = new String(by, "GBK");
			return a;
		} catch (UnsupportedEncodingException ex1) {
			ex1.printStackTrace();
			return str;
		}
	}

	/**
	 * Joins the elements of the provided array into a single string containing
	 * a list of CSV elements.
	 * 
	 * @param list
	 *            The list of values to join together.
	 * @param separator
	 *            The separator character.
	 * @return The CSV text.
	 */
	public static String join(String[] list, String separator) {
		StringBuffer csv = new StringBuffer();
		for (int i = 0; i < list.length; i++) {
			if (i > 0) {
				csv.append(separator);
			}
			csv.append(list[i]);
		}
		return csv.toString();
	}

	/**
	 * Remove Underscores from a string and replaces first Letters with
	 * Capitals. foo_bar becomes FooBar
	 */
	public static String removeUnderScores(String data) {
		String temp = null;
		StringBuffer out = new StringBuffer();
		temp = data;

		StringTokenizer st = new StringTokenizer(temp, "_");
		while (st.hasMoreTokens()) {
			String element = (String) st.nextElement();
			out.append(firstLetterCaps(element));
		}
		return out.toString();
	}

	/**
	 * replace a old substring with rep in str
	 * 
	 * @param str
	 *            the string need to be replaced
	 * @param old
	 *            the string need to be removed
	 * @param rep
	 *            the string to be inserted
	 * @return string replaced
	 */
	public static String replace(String str, String old, String rep) {
		if ((str == null) || (old == null) || (rep == null))
			// null return
			// ""
			return "";
		int index = str.indexOf(old);
		if ((index < 0) || old.equals(""))
			// nothing to replace,return the
			// origin
			return str;
		StringBuffer strBuf = new StringBuffer(str);
		while (index >= 0) { // found old part
			strBuf.delete(index, index + old.length());
			strBuf.insert(index, rep);
			index = strBuf.toString().indexOf(old);
		}
		return strBuf.toString();
	}

	public static String convertURIToURLInHTML(String html, String pattern, String domain) {
		if (html == null)
			return "";
		String http = "http://";
		String dolar = "$";
		int index = html.indexOf(pattern);
		// int realIndex = html.indexOf(pattern);
		int index1 = html.indexOf(pattern + http);
		int index2 = html.indexOf(pattern + dolar);
		StringBuffer strBuf = new StringBuffer(html);
		while (index >= 0) {
			if (index != index1 && index != index2) {
				strBuf.insert(index + pattern.length(), domain);
				int preIndex = index + pattern.length() + domain.length();
				index = strBuf.indexOf(pattern, preIndex);
				// realIndex = strBuf.indexOf(pattern,preIndex);
				index1 = strBuf.indexOf(pattern + http, preIndex);
				index2 = strBuf.indexOf(pattern + dolar, preIndex);
			} else {
				int preIndex = index + pattern.length();
				index = strBuf.indexOf(pattern, preIndex);
				// realIndex = strBuf.indexOf(pattern,preIndex);
				index1 = strBuf.indexOf(pattern + http, preIndex);
				index2 = strBuf.indexOf(pattern + dolar, preIndex);
			}
		}
		return strBuf.toString();
	}

	/**
	 * replace a old substring with rep in str
	 * 
	 * @param str
	 *            the string need to be replaced
	 * @param old
	 *            the string need to be removed
	 * @param rep
	 *            the string to be inserted
	 * @return string replaced
	 */
	public static String replaceOnlyOnce(String str, String old, String rep) {
		if ((old == null) || old.equals(""))
			// return the original string
			return str;
		if ((str == null) || str.equals(""))
			// return the original string
			return str;
		int leftIndex = str.indexOf(old);
		if (leftIndex < 0)
			// replace,return the origin
			return str;
		String leftStr = str.substring(0, leftIndex);
		String rightStr = str.substring(leftIndex + old.length());
		return leftStr + rep + rightStr;
	}

	/**
	 * Returns the output of printStackTrace as a String.
	 * 
	 * @param e
	 *            A Throwable.
	 * @return A String.
	 */
	public static final String stackTrace(Throwable e) {
		String foo = null;
		try {
			// And show the Error Screen.
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			e.printStackTrace(new PrintWriter(buf, true));
			foo = buf.toString();
		} catch (Exception f) {
			// Do nothing.
		}
		return foo;
	}

	/**
	 * Returns the output of printStackTrace as a String.
	 * 
	 * @param e
	 *            A Throwable.
	 * @param addPre
	 *            a boolean to add HTML
	 * 
	 *            <pre>
	 *            tags around the stacktrace
	 * @return A String.
	 * 
	 */
	public static final String stackTrace(Throwable e, boolean addPre) {
		if (addPre)
			return "<pre>" + stackTrace(e) + "</pre>";
		else
			return stackTrace(e);
	}

	/**
	 * Wraps a single line of text. Called by wrapText(). I can't think of any
	 * good reason for exposing this to the public, since wrapText should always
	 * be used AFAIK.
	 * 
	 * @param line
	 *            A line which is in need of word-wrapping.
	 * @param newline
	 *            The characters that define a newline.
	 * @param wrapColumn
	 *            The column to wrap the words at.
	 * @return A line with newlines inserted.
	 */

	protected static String wrapLine(String line, String newline, int wrapColumn) {
		StringBuffer wrappedLine = new StringBuffer();

		while (line.length() > wrapColumn) {
			int spaceToWrapAt = line.lastIndexOf(' ', wrapColumn);

			if (spaceToWrapAt >= 0) {
				wrappedLine.append(line.substring(0, spaceToWrapAt));
				wrappedLine.append(newline);
				line = line.substring(spaceToWrapAt + 1);
			}

			// This must be a really long word or URL. Pass it
			// through unchanged even though it's longer than the
			// wrapColumn would allow. This behavior could be
			// dependent on a parameter for those situations when
			// someone wants long words broken at line length.
			else {
				spaceToWrapAt = line.indexOf(' ', wrapColumn);

				if (spaceToWrapAt >= 0) {
					wrappedLine.append(line.substring(0, spaceToWrapAt));
					wrappedLine.append(newline);
					line = line.substring(spaceToWrapAt + 1);
				} else {
					wrappedLine.append(line);
					line = "";
				}
			}
		}

		// Whatever is left in line is short enough to just pass through,
		// just like a small small kidney stone
		wrappedLine.append(line);

		return wrappedLine.toString();
	}

	/**
	 * Takes a block of text which might have long lines in it and wraps the
	 * long lines based on the supplied wrapColumn parameter. It was initially
	 * implemented for use by VelocityEmail. If there are tabs in inString, you
	 * are going to get results that are a bit strange, since tabs are a single
	 * character but are displayed as 4 or 8 spaces. Remove the tabs.
	 * 
	 * @param inString
	 *            Text which is in need of word-wrapping.
	 * @param newline
	 *            The characters that define a newline.
	 * @param wrapColumn
	 *            The column to wrap the words at.
	 * @return The text with all the long lines word-wrapped.
	 */
	public static String wrapText(String inString, String newline, int wrapColumn) {
		
		StringTokenizer lineTokenizer = new StringTokenizer(inString, newline, true);
		StringBuffer stringBuffer = new StringBuffer();

		while (lineTokenizer.hasMoreTokens()) {
			try {
				String nextLine = lineTokenizer.nextToken();

				if (nextLine.length() > wrapColumn) {
					// This line is long enough to be wrapped.
					nextLine = wrapLine(nextLine, newline, wrapColumn);
				}

				stringBuffer.append(nextLine);
			} catch (NoSuchElementException nsee) {
				// thrown by nextToken(), but I don't know why it would
				break;
			}
		}

		return (stringBuffer.toString());
	}

	/**
	 * 过滤字符串中的特殊字符
	 * 
	 * @param src
	 * @return
	 */
	public static String filter(String src) {

		String regEx = "()（）[`~!@#$%^&*+=|{}':;',\\[\\].<>/?~！@#￥%……&*——+|{}【】‘；：”“’。，、？]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(src);
		return m.replaceAll("").trim();
	}

	/**
	 * 为正则表达式用到的字符串转义
	 * 
	 * @param str
	 *            字符串
	 * 
	 * @return 转义结果
	 */
	private static String escapeForRegEx(String str) {

		// TODO,这里现在用的是if else的分支,可能存在现成的工具类,将来可以重构
		if ("\"".equals(str)) {

			return "\\\"";
		} else if ("[".equals(str)) {

			return "\\[";
		} else if ("]".equals(str)) {

			return "\\]";
		} else if (".".equals(str)) {

			return "\\.";
		} else {

			return str;
		}
	}
}