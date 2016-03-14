package com.opentools.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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

import org.apache.commons.io.IOUtils;

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
}