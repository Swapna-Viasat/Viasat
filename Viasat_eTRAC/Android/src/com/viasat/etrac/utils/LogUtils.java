package com.viasat.etrac.utils;

import android.util.Log;


public class LogUtils 
{
	private static boolean isLog = false;
	public static void error(String key , String val)
	{
		if(isLog)
			Log.e(key, val);
	}
	public static void warn(String key , String val)
	{
		if(isLog)
			Log.w(key, val);
	}
	public static void verbose(String key , String val)
	{
		if(isLog)
			Log.v(key, val);
	}
	public static void info(String key , String val)
	{
		if(isLog)
			Log.i(key, val);
	}
	public static void debug(String key , String val)
	{
		if(isLog)
			Log.d(key, val);
	}
}
