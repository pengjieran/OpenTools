package com.opentools.sort.string;

/**
 * 
 * 三项字符串快速排序
 * 
 * 
 * 根据高位优先, 改进为快速排序策略, 这种方法是前两种方法的结合
 * 
 * 根据字符串数组的首字母进行三向切分, 然后 递归的 将得到的三个子数组排序, 一个含有所有首字母小于切分字符的字符串子数组,
 * 一个含有所有首字母等于切分字符的子数组, 一个含有所有首字母大于气氛字符串的子数组
 * 
 * 三项字符串快速排序能够很好地处理等值键、有较长公共前缀的键、取值范围较小的键和小数组，且只需要递归所需的隐式栈的额外空间
 * 
 * 
 * @author Aaron
 * @since 2015年5月31日
 */
public class Quick3String {

	private static int charAt(String s, int d) {

		if (d < s.length())

			return s.charAt(d);

		else

			return -1;

	}

	public static void sort(String[] a) {

		sort(a, 0, a.length - 1, 0);

	}

	private static void sort(String[] a, int lo, int hi, int d) {

		if (hi <= lo)

			return;

		int lt = lo, gt = hi;

		int v = charAt(a[lo], d); // povit

		int i = lo + 1;

		while (i <= gt) {

			int t = charAt(a[i], d);

			if (t < v)

				exch(a, lt++, i++);

			else if (t > v)

				exch(a, i, gt--);

			else

				i++;

		}

		sort(a, lo, lt - 1, d);

		if (v >= 0)

			sort(a, lt, gt, d + 1);

		sort(a, gt + 1, hi, d);

	}

	private static void exch(String[] a, int i, int j) {

		String temp = a[i];

		a[i] = a[j];
		;

		a[j] = temp;

	}

}