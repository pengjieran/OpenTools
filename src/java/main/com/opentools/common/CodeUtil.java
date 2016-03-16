package com.opentools.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 * 使用jdk原生的编解码方式对某个指定数据加密解密
 * @author Aaron
 *
 */
public class CodeUtil {

	/**
	 * 对byte做base64编码
	 */
	public static String encodeBase64(byte[] data) {
		
		Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(data);
	}
	
	/**
	 * 对字符串做base64解码
	 * @param src
	 * @return
	 */
	public static byte[] decodeBase64(String src) {
		
		Decoder decoder = Base64.getDecoder();
		return decoder.decode(src.getBytes());
	}
	
	/**
	 * 对字符串做加解密操作
	 * @param src
	 * @param deskey
	 * @param passkey
	 * @param mode 加密：Cipher.ENCRYPT_MODE 解密：Cipher.DECRYPT_MODE
	 * @return
	 */
	public static String codeDES(String src, String deskey, String passkey, int mode) {
		
		try {
			SecureRandom sr = new SecureRandom();
			DESKeySpec dks = new DESKeySpec(deskey.getBytes());
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(dks);
			IvParameterSpec iv = new IvParameterSpec(passkey.getBytes());
			Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			cipher.init(mode, securekey, iv, sr);
			CipherInputStream cis = new CipherInputStream(new ByteArrayInputStream(src.getBytes()) , cipher);
			return IOUtils.toString(cis);
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidAlgorithmParameterException | IOException e) {
			
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 利用commons-codec做一次md5处理
	 * @param src
	 * @return
	 */
	public static String Md5(String src) {
		
		return DigestUtils.md5Hex(src);
		
	}
	
	/**
	 * 使用jdk原生的方法计算md5值
	 * @param src
	 * @return
	 */
	public static String MD5JDK(String src) {
		
		// 用来将字节转换成 16 进制表示的字符
		char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(src.getBytes());
			//计算结果是一个128位的长整数
			byte[] bs = md.digest();
			// 每个字节用 16 进制表示的话，使用两个字符
			char str[] = new char[16 * 2];
			
			int k = 0; // 表示转换结果中对应的字符位置
			for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节
				// 转换成 16 进制字符的转换
				byte byte0 = bs[i]; // 取第 i 个字节
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
				// >>> 为逻辑右移，将符号位一起右移
				str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
			}
			String s = new String(str); // 转换后的结果转换为字符串
			return s;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String SHA512encode(String src) {
		
		return DigestUtils.sha512Hex(src.getBytes());
				
	}
	
	@Test
	public void test() {
		
		/*String md5 = CodeUtil.Md5("123456");
		System.out.println(md5);
		
		String md5jdk = CodeUtil.MD5JDK("123456");
		System.out.println(md5jdk);*/
		
		String string = CodeUtil.SHA512encode("123456");
		System.out.println(string);
	}
}