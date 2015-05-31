package com.opentools.sort.string;

/**
 * 低位优先的字符串排序 低位优先排序算法是通过建索引计数法完成的, 从每个字符串的最右侧开始, 以每个位置的字符作为key键(相当于组号), 用
 * 键索引计数法 对字符串排序W遍(假设所有字符串长度为W) 由于键索引计数法的排序是稳定的, 则可以推出低位优先的字符串排序算法能够稳定的排序定长字符串
 * 算法缺点: 只能用于处于等长字符串排序 需要额外的count[]和aux[]数组空间
 * 
 * @author Aaron
 *
 */
public class LSD {

	public static void sort(String[] a, int W) {

		// 通过后W个字符将a[]排序, 从低位开始
		int N = a.length; // 字符串数组长度

		int R = 256; // radix; 最多表示256的字符extern-ASCII

		String[] aux = new String[N]; // 存储排序后的字符串数组

		for (int d = W - 1; d >= 0; d--) { // 低位排序从这里体现

			int[] count = new int[R + 1];

			// 计算出现频率
			for (int i = 0; i < N; i++) {

				count[a[i].charAt(d) + 1]++; // count每一位统计对应的字符的出现次数

			}

			for (int r = 0; r < R; r++) {

				count[r + 1] += count[r]; // 第一个字符换索引为0

			}

			for (int i = 0; i < N; i++) {

				aux[count[a[i].charAt(d)]++] = a[i]; // 将字符串应对的d位的数, 放在当前索引下,然后索引加1

			}

			// 写回a[], 重复W词, 因为按照W个字符排序
			for (int i = 0; i < N; i++) {

				a[i] = aux[i];

			}

		}

	}

}