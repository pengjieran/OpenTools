package com.opentools.tutorials;
/**
 * 交换两个数字的位置，不借助于第三方数字
 * @author 冯靖
 *
 */
public final class SwapPosition {
	
	public static void swap1(int a, int b) {
		
		a = a + b - (b = a);
		System.out.println("a=" + a + ",b=" + b);
	}
	
	public static void swap2(int a, int b) {
		
		b = a + (a = b) * 0;
		System.out.println("a=" + a + ",b=" + b);
	}
	
	public static void swap3(int a, int b) {
		
		a = a * b;
		b = a / b;
		a = a / b;
		System.out.println("a=" + a + ",b=" + b);
	}
	
	public static void swap4(int a, int b) {
		
		a = a + b;
		b = a - b;
		a = a - b;
		System.out.println("a=" + a + ",b=" + b);
	}
	
	public static void swap5(int a, int b) {
		
		a = a ^ b;
		b = a ^ b;
		a = a ^ b;
		System.out.println("a=" + a + ",b=" + b);
	}
	
	private SwapPosition() {}
}