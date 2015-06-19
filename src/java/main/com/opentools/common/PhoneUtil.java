package com.opentools.common;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * 
 * @author Aaron
 *识别手机号，电话号码
 */
public class PhoneUtil {
	
	public static final String IS_VALID_NUMBER = "isValidNumber";
	
	public static final String COUNTRY_CODE = "countryCode";
	
	public static final String NATIONAL_NUMBER = "national_number";
	
	public static final String E164_NUMBER = "E164_number";

	private static PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
	
	/**
	 * 解析手机号和电话号码
	 * @param phone	需要解析的号码
	 * @param defaultRegion 默认为CN
	 * @return
	 * @throws NumberParseException
	 */
	public static Map<String, String> getPhoneNumber(String phone, String defaultRegion) throws NumberParseException
	{
		Map<String, String> phones = new LinkedHashMap<String, String>();
		
		if (null == defaultRegion)
		{
			defaultRegion = "CN";
		}
		
		PhoneNumber phoneNumber = phoneNumberUtil.parse(phone, defaultRegion);
		boolean isvalidNumber = phoneNumberUtil.isValidNumber(phoneNumber);		
		
		phones.put(IS_VALID_NUMBER, String.valueOf(isvalidNumber));
		phones.put(COUNTRY_CODE, String.valueOf(phoneNumber.getCountryCode()));
		phones.put(NATIONAL_NUMBER, String.valueOf(phoneNumber.getNationalNumber()));
		phones.put(E164_NUMBER, isvalidNumber ? phoneNumberUtil.format(phoneNumber, PhoneNumberFormat.E164) : "invalid");
		
		return phones;
	}
	private PhoneUtil(){}
}