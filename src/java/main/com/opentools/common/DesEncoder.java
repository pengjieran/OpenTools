package com.opentools.common;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * DES 加密、解密
 * 
 * @author Aaron
 */
public final class DesEncoder {

	private static final String ALGORITHM = "DES";
	private static final String KEY = "k#Sr1SjD";
	private SecretKey deskey;
	private Cipher cipher;
	private byte[] encryptorData;
	private byte[] decryptorData;

	private static DesEncoder instance = new DesEncoder();

	public static DesEncoder getInstance() {
		return instance;
	}

	/**
	 * DES 加密
	 * 
	 * @param plainTxt
	 * @return
	 * @throws  
	 * @throws Exception
	 */
	public static String encode(String plainTxt) {
		byte[] encByte = instance.createEncryptor(plainTxt);
		return instance.byteToString(encByte);
	}

	/**
	 * DES 解密
	 * 
	 * @param encTxt
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 */
	public static String decode(String encTxt) throws IOException {
		byte[] decByte = instance
				.createDecryptor(instance.stringToByte(encTxt));
		return new String(decByte);
	}

	/**
	 * 初始化 DES 实例
	 */
	private DesEncoder() {
		init();
	}

	/**
	 * 初始化 DES 加密算法的一些参数
	 */
	private void init() {
		Security.addProvider(new com.sun.crypto.provider.SunJCE());
		try {
			
			byte key[] = KEY.getBytes();
			deskey = new SecretKeySpec(key, ALGORITHM);
			
			cipher = Cipher.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		} catch (NoSuchPaddingException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 对 byte[] 进行加密
	 * 
	 * @param datasource
	 *            要加密的数据
	 * @return 返回加密后的 byte 数组
	 */
	private byte[] createEncryptor(byte[] datasource) {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, deskey);
			encryptorData = cipher.doFinal(datasource);
		} catch (java.security.InvalidKeyException ex) {
			ex.printStackTrace();
		} catch (javax.crypto.BadPaddingException ex) {
			ex.printStackTrace();
		} catch (javax.crypto.IllegalBlockSizeException ex) {
			ex.printStackTrace();
		}
		return encryptorData;
	}

	/**
	 * 将字符串加密
	 * 
	 * @param datasource
	 * @return
	 * @throws Exception
	 */
	private byte[] createEncryptor(String datasource) {
		return createEncryptor(datasource.getBytes());
	}

	/**
	 * 对 datasource 数组进行解密
	 * 
	 * @param datasource
	 *            要解密的数据
	 * @return 返回加密后的 byte[]
	 */
	private byte[] createDecryptor(byte[] datasource) {
		try {
			cipher.init(Cipher.DECRYPT_MODE, deskey);
			decryptorData = cipher.doFinal(datasource);
		} catch (java.security.InvalidKeyException ex) {
			ex.printStackTrace();
		} catch (javax.crypto.BadPaddingException ex) {
			ex.printStackTrace();
		} catch (javax.crypto.IllegalBlockSizeException ex) {
			ex.printStackTrace();
		}
		return decryptorData;
	}

	/**
	 * 
	 * 将 DES 加密过的 byte数组转换为字符串
	 * 
	 * @param dataByte
	 * @return
	 */
	private String byteToString(byte[] dataByte) {
		String returnStr = null;
		byte[] bs = Base64.encodeBase64(dataByte);
		returnStr = new String(bs);
		return returnStr;
	}

	/**
	 * 
	 * 将字符串转换为DES算法可以解密的byte数组
	 * 
	 * @param dataByte
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 */
	private byte[] stringToByte(String datasource) throws IOException {
		return Base64.decodeBase64(datasource.getBytes());
	}
}