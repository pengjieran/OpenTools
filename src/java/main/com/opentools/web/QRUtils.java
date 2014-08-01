package com.opentools.web;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

/**
 * 生成和解析二维码相关的辅助工具类
 * @author Dean		pjr0206@163.com
 *
 */
public final class QRUtils {
	
	public static final String ENCODER_MODE_UTF8 = "UTF8";
	
	public static final String ENCODER_MODE_GBK = "GBK";
	
	public static final String ENCODER_MODE_gb2312 = "GB2312";
	
	public static final String ENCODER_MODE_ISO8859_1 = "ISO8859-1";
	
	/**
	 * 生成二维码图片
	 * @param content	二维码中包含的字符串信息
	 * @param path		生成的二维码图片保存路径
	 * @param length	生成的图片长宽
	 * @param encodee_mode	编码方式
	 * @throws WriterException 
	 * @throws IOException 
	 */
	public static void encode(String content, String path, Integer length, String encodee_mode) throws WriterException, IOException {
		
		if (length == null) {
			
			length = 300;
		}
		
		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
		hints.put(EncodeHintType.CHARACTER_SET, encodee_mode);
		hints.put(EncodeHintType.MARGIN, 1);
		BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, length, length, hints);
		MatrixToImageWriter.writeToFile(matrix, "png", new File(path));
	}
	
	/**
	 * 解析二维码图片的工具类
	 * @param path		二维码图片的路径
	 * @param unicode	读取出来的内容的编码方式
	 * @return Result	读取结果，获取结果时可以使用result.getText()方式
	 * @throws IOException
	 * @throws NotFoundException
	 */
	public static Result decode(String path, String encodee_mode) throws IOException, NotFoundException {
		
		BufferedImage bufferedImage = ImageIO.read(new File(path));
		LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, encodee_mode);
		
		return new MultiFormatReader().decode(bitmap, hints);
	}
	
	private QRUtils() {}

}
