package com.test;

import java.io.IOException;

import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.opentools.web.QRUtils;

public class ConsoleTest {
	
	public static void main(String[] args) {
		try {
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
		}
	}

}
