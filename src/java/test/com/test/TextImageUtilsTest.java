package test;

import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.opentools.tutorials.TextImageUtils;

public class TextImageUtilsTest {
	
	public static void main(String[] args) {
		
		try {
			//TextImageUtils.makeImage("123456789", new FileOutputStream(new File("aaa.png")), Font.BOLD);
			TextImageUtils.makeImage("pjr0206@163.com", new FileOutputStream(new File("4521.png")), Font.PLAIN);
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

}