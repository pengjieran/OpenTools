package com.test;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleTest {
	
	public static void main(String[] args) {
		/*try {
			QRUtils.encode("我的疯狂只为你而存在", "aa.png", 300, QRUtils.ENCODER_MODE_UTF8);
			
		} catch (WriterException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		try {
			
			Result result = QRUtils.decode("aa.png", QRUtils.ENCODER_MODE_GBK);
			System.out.println(result.getText());
		} catch (NotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}*/
		Date date = new Date(1433677228923L);
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		System.out.println(format.format(date));
	}

	@Test
	public void main() {

		System.out.print("");
	}

}