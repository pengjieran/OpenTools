package com.opentools.common;

/**
 * 数字相关的工具类
 * 
 * @author Aaron
 * @since 2015年5月18日
 */
public class NumberUtils {

	private NumberUtils() {}

	/**
	 * 将一个数字等分成i等份，多余的加在最后一个数字
	 * @param i
	 * @param bisectNumber
	 * @return
	 */
	public static Integer[] formatNumber(Integer i, int bisectNumber) {

		Integer[] array = new Integer[bisectNumber];
		Double doubleValue = i.doubleValue();
		Double a = doubleValue / bisectNumber;
		Integer intValue = a.intValue();
		for (int index = 0; index < bisectNumber; index++) {
			if (index != (bisectNumber - 1)) {
				array[index] = intValue;
			} else {
				Integer inta = (intValue * bisectNumber);
				Integer aurplus = (i - inta);
				array[index] = (intValue + aurplus);
			}
		}
		return array;
	}
}