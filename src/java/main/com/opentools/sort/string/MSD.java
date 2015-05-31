package com.opentools.sort.string;

/**
 * 高位优先的字符串排序
 * 
 * 高位优先排序算法用于更通用的情形(字符串不定长), 首先用 键索引计数法 对所有字符串首字母排序, 然后在 递归的
 * 将每个首字母所对应的子数组排序(忽略首字母) 当指定位置超出字符串的末尾应该返回 -1 . 越界则输出-1.
 * 这种转换意味着字符串中的每个字符都可能产生R+1中不同的值，同时键索引计数法本来就需要一个额外的位置，所以使用代码int count[] = new
 * int[R + 2];
 * 
 * 在高位优先排序中, 每次递归过程都会创建一个count数组并转换成索引, 当数据量过大, 会造成很大的 空间代价 , 所以当进行小数组排序时, 切换为使用
 * 插入排序算法 , 避免重复检查已知相同字符带来的成本
 * 
 * 算法缺点: 1.高位优先的字符串排序使用了两个辅助数组（aux[]和count[]） 2.高位优先的字符串排序的最坏情况就是所有的键均相同
 * 
 * @author Aaron
 * @since 2015年5月31日
 */
public class MSD {

	private static int R = 256; // 字符数

	private static final int M = 15; // 小数组的切换阈值

	private static String[] aux; // 辅助数组, 存储中间排序结果

	private static int charAt(String s, int d) {

		if (d < s.length())

			return s.charAt(d);

		else

			return -1;

	}

	public static void sort(String[] a) { // 排序的外部接口

		int N = a.length;

		aux = new String[N];

		sort(a, 0, N - 1, 0);

	}

	// 排序的内部实现, 以第d个字符为键将a[lo]至a[hi]排序, 递归过程

	private static void sort(String[] a, int lo, int hi, int d) {

		if (hi <= lo + M) {

			Insertion.sort(a, lo, hi, d); // 大于阈值则使用插入排序

			return;

		}

		int[] count = new int[R + 2]; // 计算频率

		for (int i = lo; i <= hi; i++) {

			count[charAt(a[i], d) + 2]++; // 由左到右同此每个字符的频率

		}

		for (int r = 0; r < R + 1; r++) {

			count[r + 1] += count[r]; // 频率转换为索引

		}

		// 数据分类

		for (int i = lo; i <= hi; i++) {

			aux[count[charAt(a[i], d) + 1]++] = a[i];

		}

		// 回写

		for (int r = 0; r < R; r++) {

			sort(a, lo + count[r], lo + count[r + 1] - 1, d + 1);

		}

	}

	public static class Insertion {

		public static void sort(String[] a, int lo, int hi, int d) { // 从第d个字符开始对a[lo]到a[hi]排序

			for (int i = lo; i <= hi; i++) {

				for (int j = i; j > lo && less(a[j], a[j - 1], d); j--)

					exch(a, j, j - 1);

			}

		}

		private static boolean less(String v, String w, int d) {

			return v.substring(d).compareTo(w.substring(d)) < 0;

		}

		private static void exch(String[] a, int i, int j) {

			String temp = a[i];

			a[i] = a[j];
			;

			a[j] = temp;

		}

	}

}