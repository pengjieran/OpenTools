package com.opentools.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 检查用户请求设备是不是模拟器
 * 
 * @author Aaron
 * @since 2015年6月5日
 */
public class EmulatorUtil
{
	
	private static final String BOARD = "borad";
	
	private static final String BOOTLOADER = "bootloader";
	
	private static final String BRAND = "brand";
	
	private static final String DEVICE = "device";
	
	private static final String PRODUCT = "product";
	
	private static final String HARDWARE = "hardware";
	
	private static final String MODEL = "model";
	
	private static final String IMSI = "imsi";
	
	private static final String OSVERSION = "osversion";

	public static boolean isEmulator(Map<String, String[]> headers, boolean flag)
	{
		
		List<String> imsis = new ArrayList<String>();
		imsis.add("310260000000000");

		
		List<String> versions = new ArrayList<String>();
		versions.add("qemu");
		versions.add("hd");
		
		List<String> deviceids = new ArrayList<String>();
		deviceids.add("000000000000000");
		
		String board = getValue(headers.get(BOARD));
		String bootloader = getValue(headers.get(BOOTLOADER));
		String device = getValue(headers.get(DEVICE));
		String product = getValue(headers.get(PRODUCT));
		String brand = getValue(headers.get(BRAND));
		String hardware = getValue(headers.get(HARDWARE));
		String model = getValue(headers.get(MODEL));
		String imsi = getValue(headers.get(IMSI));
		String osversion = getValue(headers.get(OSVERSION));
		
		if ("unknown".equals(board)
                || "generic".equals(brand) || "android".equals(brand)
                || "generic".equals(device) || (device != null && device.startsWith("vbox"))
                || "sdk".equals(model) || "google_sdk".equals(model) || ( model != null && model.toLowerCase().contains("droid4x"))||( model!=null&&model.toLowerCase().contains("- API "))
                || "sdk".equals(product) ||(product!=null&&product.startsWith("vbox"))
                || "goldfish".equals(hardware) || "unknown".equals(hardware) || (hardware!=null&&hardware.startsWith("vbox"))
            )
		{
			return true;
		}
		
		if (null == osversion)
		{
			return true;
		}
		
		if (osversion.contains("qemu") || osversion.contains("hd"))
		{
			return true;
		}

		if (imsis.contains(imsi))
		{
			return true;
		}
		
		if (null == imsi)
		{
			return true;
		}
		
		if (imsi.contains("unknown"))
		{
			return true;
		}
		
		if (board.equals("BOARD") && flag)
		{
			return true;
		}
		
		if (versions.contains(osversion))
		{
			return true;
		}

		return false;
	}
	
	private static String getValue(String[] values)
	{
		if (null != values && values.length > 0)
		{
			StringBuilder stringBuilder = new StringBuilder();
			for (String str : values)
			{
				stringBuilder.append(str);
			}
			
			return stringBuilder.toString();
		}
		else
		{
			return null;
		}
	}

	private EmulatorUtil()
	{
	}

}