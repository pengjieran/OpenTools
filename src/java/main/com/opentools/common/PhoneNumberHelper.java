package com.opentools.common;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public final class PhoneNumberHelper {

	public static PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

	public static class ParsedPhoneNumber {
		private boolean mobile;
		private boolean numberValid;
		private String alpha2Code;
		private String countryCode;
		private String areaCode;
		private String localNumber;

		public boolean isMobile() {
			return mobile;
		}

		public void setMobile(boolean mobile) {
			this.mobile = mobile;
		}

		public boolean isNumberValid() {
			return numberValid;
		}

		public void setNumberValid(boolean numberValid) {
			this.numberValid = numberValid;
		}

		public String getAlpha2Code() {
			return alpha2Code;
		}

		public void setAlpha2Code(String alpha2Code) {
			this.alpha2Code = alpha2Code;
		}

		public String getCountryCode() {
			return countryCode;
		}

		public void setCountryCode(String countryCode) {
			this.countryCode = countryCode;
		}

		public String getLocalNumber() {
			return localNumber;
		}

		public void setLocalNumber(String localNumber) {
			this.localNumber = localNumber;
		}

		public String getAreaCode() {
			return areaCode;
		}

		public void setAreaCode(String areaCode) {
			this.areaCode = areaCode;
		}
	}

	public static ParsedPhoneNumber parse(String aNumber)
			throws NumberParseException {
		PhoneNumber number = null;

		number = parse1(aNumber);
		ParsedPhoneNumber parsedPhoneNumber = new ParsedPhoneNumber();
		parsedPhoneNumber.setNumberValid(phoneUtil.isValidNumber(number));
		PhoneNumberType type = phoneUtil.getNumberType(number);
		parsedPhoneNumber.setMobile(PhoneNumberType.MOBILE.equals(type)
				|| PhoneNumberType.FIXED_LINE_OR_MOBILE.equals(type));
		parsedPhoneNumber
				.setCountryCode(String.valueOf(number.getCountryCode()));
		parsedPhoneNumber.setAlpha2Code(phoneUtil
				.getRegionCodeForNumber(number));

		String nationalSignificantNumber = phoneUtil
				.getNationalSignificantNumber(number);
		int geographicalAreaCodeLength = phoneUtil
				.getLengthOfGeographicalAreaCode(number);
		int nationalDestinationCodeLength = phoneUtil
				.getLengthOfNationalDestinationCode(number);
		String geographicalAreaCode = null;
		String localNumber = nationalSignificantNumber;
		if (geographicalAreaCodeLength > 0) {
			geographicalAreaCode = nationalSignificantNumber.substring(0,
					geographicalAreaCodeLength);
			localNumber = nationalSignificantNumber
					.substring(geographicalAreaCodeLength
							+ nationalDestinationCodeLength);
		}

		parsedPhoneNumber.setAreaCode(geographicalAreaCode);
		parsedPhoneNumber.setLocalNumber(localNumber);

		return parsedPhoneNumber;

	}

	/**
	 * 
	 * @param numberToParse
	 *            - number that we are attempting to parse. This can contain
	 *            formatting such as +, ( and -, as well as a phone number
	 *            extension.
	 * @param defaultRegion
	 *            - the ISO 3166-1 two-letter region code that denotes the
	 *            region that we are expecting the number to be from. This is
	 *            only used if the number being parsed is not written in
	 *            international format. The country calling code for the number
	 *            in this case would be stored as that of the default region
	 *            supplied.
	 * @return a phone number proto buffer filled with the parsed number
	 * @throws NumberParseException
	 *             - if the string is not considered to be a viable phone number
	 *             or if no default region was supplied
	 */
	private static PhoneNumber parse2(String numberToParse, String defaultRegion)
			throws NumberParseException {
		return phoneUtil.parse(numberToParse, defaultRegion);
	}

	private static PhoneNumber parse1(String numberToParse)
			throws NumberParseException {
		return parse2(numberToParse, "CN");
	}

	public static boolean isValidPhoneNumber(String number) {
		boolean result = false;
		try {
			PhoneNumber phoneNumber = parse1(number);
			result = phoneUtil.isValidNumber(phoneNumber);
		} catch (NumberParseException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean isMobileNumber(String numberToParse) {
		boolean result = false;
		try {
			PhoneNumber phoneNumber = parse1(numberToParse);
			PhoneNumberType type = phoneUtil.getNumberType(phoneNumber);
			if (PhoneNumberType.MOBILE.equals(type)
					|| PhoneNumberType.FIXED_LINE_OR_MOBILE.equals(type)) {
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String format2InternationalNumber(String n)
			throws NumberParseException {
		PhoneNumber p = parse1(n);
		return phoneUtil.format(p,
				PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
	}

	public static String format2E164(String n) throws NumberParseException {
		PhoneNumber p = PhoneNumberHelper.parse1(n);
		return phoneUtil.format(p, PhoneNumberUtil.PhoneNumberFormat.E164);
	}

	public static void main(String[] args) throws NumberParseException {
		
		PhoneNumber number = phoneUtil.parseAndKeepRawInput("+86045157970420", "CN");
		
		String format = phoneUtil.format(number,PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
		
		System.out.println(format);
		String[] strings = format.split(" ");
		//inspectNumber("8601082745223");
		System.out.println(strings[0]);
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 1; i < strings.length; i++)
		{		
			stringBuilder.append(strings[i]);
		}
		System.out.println(stringBuilder.toString());
	}

	@SuppressWarnings({ "incomplete-switch", "unused" })
	private static void inspectNumber(String numberStr)
			throws NumberParseException {
		PhoneNumber number = phoneUtil.parseAndKeepRawInput(numberStr, "CN");
		boolean isPossible = phoneUtil.isPossibleNumber(number);
		System.out.println(isPossible);
		boolean isNumberValid = phoneUtil.isValidNumber(number);
		System.out.println(isNumberValid);
		if (!isPossible) {
			System.out.println("\nResult from isPossibleNumberWithReason(): ");
			PhoneNumberUtil.ValidationResult validationResult = phoneUtil.isPossibleNumberWithReason(number);
			switch (validationResult) {
			case INVALID_COUNTRY_CODE:
				System.out.println("INVALID_COUNTRY_CODE");
				break;
			case TOO_SHORT:
				System.out.println("TOO_SHORT");
				break;
			case TOO_LONG:
				System.out.println("TOO_LONG");
				break;
			}
		}
		String nationalSignificantNumber = phoneUtil.getNationalSignificantNumber(number);
		int nationalDestinationCodeLength = phoneUtil.getLengthOfNationalDestinationCode(number);
		int geographicalAreaCodeLength = phoneUtil.getLengthOfGeographicalAreaCode(number);
		String nationalDestinationCode = "";
		if (nationalDestinationCodeLength > 0) {
			
			nationalDestinationCode = nationalSignificantNumber.substring(0, nationalDestinationCodeLength);
		}
		String geographicalAreaCode = "";
		if (geographicalAreaCodeLength > 0) {
			geographicalAreaCode = nationalSignificantNumber.substring(0, geographicalAreaCodeLength);
		}
		System.out.println(numberStr
				+ " nationalSignificantNumber:"
				+ nationalSignificantNumber
				+ " geographicalAreaCodeLength:"
				+ geographicalAreaCodeLength
				+ " nationalDestinationCodeLength:"
				+ nationalDestinationCodeLength
				+ " nationalDestinationCode: "
				+ nationalDestinationCode
				+ " International format:"
				+ phoneUtil.format(number,PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
				+ " National format:"
				+ phoneUtil.format(number,
						PhoneNumberUtil.PhoneNumberFormat.NATIONAL));
	}
}