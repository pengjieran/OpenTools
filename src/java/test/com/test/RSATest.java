package com.test;

import com.opentools.common.RSA;

public class RSATest
{

	public static void main(String[] args)
	{
		
		RSA rsa = new RSA(5);
		String encrypt = rsa.encrypt("15");
		System.out.println(encrypt);
		String decrypt = rsa.decrypt(encrypt);
		System.out.println(decrypt);
		
	}

}
