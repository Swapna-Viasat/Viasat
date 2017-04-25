package com.viasat.etrac.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CalenderUtils 
{
	private static String DATE_TIME = "yyyy-MM-dd HH:mm:ss z";
	public static String getCurrentTimeStamp()
	{
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME);
        String currentTimeStamp = dateFormat.format(new Date());
        return currentTimeStamp;
	}
}
