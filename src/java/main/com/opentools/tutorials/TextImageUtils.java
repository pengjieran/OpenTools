package com.opentools.tutorials;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

/**
 * 将文字生成图片
 * 电子邮件的font 为Font.PLAIN
 * 电话号码和产品关键特征的font为Font.BOLD
 * @author Aaron
 * @since 2015年5月12日
 *
 */
public final class TextImageUtils {
	
	private static final IndexColorModel ICM = createIndexColorModel();
	
	/**
	 * 用一串文字生成一张图片
	 * @param string	需要生成图片的文字
	 * @param out	图片
	 * @param font	文字字体大小
	 * @throws IOException 
	 */
	public static void makeImage(String string,OutputStream out, int fontSize) throws IOException {
		
		int height = 22;
		BufferedImage bi = new BufferedImage(255, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = (Graphics2D) bi.getGraphics();
		Font font = new Font("Verdana", fontSize, 20);
		g2d.setFont(font);
		g2d.drawString(string, 2, 19);
		FontMetrics fm = g2d.getFontMetrics();
		
		int new_width = fm.charsWidth(string.toCharArray(), 0, string.length()) + 4;
		int new_height = fm.getHeight();
		BufferedImage nbi = new BufferedImage(new_width, new_height, BufferedImage.TYPE_BYTE_INDEXED, ICM);
		Graphics2D g = (Graphics2D) nbi.getGraphics();
		g.setColor(new Color(0, 0, 0, 0));
		g.fillRect(0, 0, new_width, new_height);
		g.setFont(font);
		g.setColor(new Color(200, 0, 0));
		g.drawString(string, 2, new_height - 4);
		ImageIO.write(nbi, "png", out);
		
	}
	
	private static IndexColorModel createIndexColorModel() {
		
		BufferedImage bfi = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_INDEXED);
		IndexColorModel icm = (IndexColorModel) bfi.getColorModel();
		int size = 256;
		byte[] r = new byte[size];
		byte[] g = new byte[size];
		byte[] b = new byte[size];
		byte[] a = new byte[size];
		icm.getReds(r);
		icm.getGreens(g);
		icm.getBlues(b);
		java.util.Arrays.fill(a, (byte)255);
		r[0] = g[0] = b[0] = a[0] = 0;
		return new IndexColorModel(8, size, r, g, b);
		
	}
			
	private TextImageUtils() {}
}
