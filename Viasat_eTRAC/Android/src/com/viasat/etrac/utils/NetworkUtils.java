package com.viasat.etrac.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.viasat.etrac.dataobj.RouterDo;



public class NetworkUtils 
{
	static int test = 0;

	public static String	deviceIpAddress;
	public static String	routerIpAddress; 
	public static String	ssid;
	public static String	macAdd;
	public static String	linkSpeed;

	//--------------
	static DhcpInfo d;
	static WifiManager wifiManager;
	//--------------
	
	private static String ipUrl = "http://icanhazip.com/";
	private static String ipDetailsUrl = "http://xml.utrace.de/?query=";
	private static String ipinfoUrl = "http://ipinfo.io/json";
	private static String ipApiUrl = "http://ip-api.com/json";
	private static String ipTelizeUrl = "http://www.telize.com/geoip";

	public static int isNetworkAvailable(Context context) {
		/*ConnectivityManager cm	= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
		if(activeNetworkInfo != null)
		{			
			//Network inteface available
			return activeNetworkInfo != null && activeNetworkInfo.isConnected();
		}
		else
		{
			//Network inteface is not available
			return false;
		}*/

		//-------------------------------------------
		//Real Device for WiFi with Internet
		try {
			return isNetworkAvailablewithInternet(context);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 1;
		//-------------------------------------------
	}

	public static int isWiFiAvailable(Context context) {
		try {
			return isWiFiConnected(context);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 1;
	}


	public static int isNetworkAvailablewithInternet(Context context) throws IOException 
	{
		boolean ret = false;
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivity != null) {
			//NetworkInfo[] info = connectivity.getAllNetworkInfo();
			NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (info != null) {	                
				if (info.getState() == NetworkInfo.State.CONNECTED) {	
					//Check for Internet Connectivity
					ret = hasActiveInternetConnection(context);

					if(ret == true) {						
						return 0; //Internet Available
					}
					else {						
						return 2; //Internet not available
					}
				}
			}
		}
		return 1; //Not connected to WiFi router
	}

	public static int isWiFiConnected(Context context) throws IOException 
	{
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivity != null) {
			NetworkInfo info = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

			if (info != null) {	                
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					return 0; ///Connected to WiFi router
				}
			}
		}
		return 1; //Not connected to WiFi router
	}


	public static boolean hasActiveInternetConnection(Context context) {	
		String url_1 = "http://www.cmcltd.com"; //"http://stackoverflow.com";
		String url_0 = "http://google.com";
		HttpURLConnection urlc = null;
		
		try {
			if(test == 0) {
				urlc = (HttpURLConnection) (new URL(url_0).openConnection());
				test = 1;
			}
			else {
				urlc = (HttpURLConnection) (new URL(url_1).openConnection());
				test = 0;
			}

			urlc.setRequestProperty("User-Agent", "Test");
			urlc.setRequestProperty("Connection", "close");
			urlc.setConnectTimeout(1500); 
			urlc.connect();
			return (urlc.getResponseCode() == 200);
		} catch (IOException e) 
		{
			LogUtils.error("NetworkUtils","Test IOException: WebServer is not responding");
		}

		return false;
	}

	public static boolean pingIP(Context contex, String hostIP){
		//boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win"); 

		ProcessBuilder processBuilder = new ProcessBuilder("ping", "-c", "1", hostIP); //("ping", isWindows? "-n" : "-c", "1", host);
		Process proc = null;
		try {
			proc = processBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		int returnVal = 0;
		try {
			returnVal = proc.waitFor();
		} catch (InterruptedException e) {
			return false;
		}
		return returnVal == 0;
	}

	public static boolean ping(Context context) {
		try {
			SocketAddress addr = new InetSocketAddress("www.google.com", 80); // Set IP/Host and Port
			Socket socket = new Socket();
			//Connect socket to address, and set a time-out to 3 sec
			socket.connect(addr, 3000);
			//If network isn't conecctet then throw a IOException else socket is connected successfully
			socket.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}	

	@SuppressLint("DefaultLocale")
	public static RouterDo routerInfo(Context context)
	{
		RouterDo routerDo = null;
		wifiManager= (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		if(wifiManager != null)
		{
			routerDo = new RouterDo();

			//-----------------------------------------------------
			d=wifiManager.getDhcpInfo();

			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			//------------------------------
			//--Connected WiFi Router Name--
			//------------------------------
			ssid = wifiInfo.getSSID();

			//------------------------------
			//--Android Device MAC Address--
			//------------------------------
			macAdd = wifiInfo.getMacAddress(); 

			//-----------------------------
			//--Android Device IP Address--
			//-----------------------------
			int dIP = d.ipAddress;
			deviceIpAddress=String.valueOf(String.format("%d.%d.%d.%d",
					(dIP & 0xff),
					(dIP >> 8 & 0xff),
					(dIP >> 16 & 0xff),
					(dIP >> 24 & 0xff))); 

			//-----------------------------
			//---WiFi Router IP Address---
			//-----------------------------
			int rIP = d.gateway;
			routerIpAddress= String.valueOf(String.format("%d.%d.%d.%d",
					(rIP & 0xff),
					(rIP >> 8 & 0xff),
					(rIP >> 16 & 0xff),
					(rIP >> 24 & 0xff))); 
			
			//-----------------------------
			//---WiFi Router Link Speed----
			//-----------------------------
			linkSpeed = String.valueOf(wifiInfo.getLinkSpeed());

			routerDo.ssid = ssid.replace("\"", "");
			routerDo.macAdd = macAdd.toUpperCase();
			routerDo.routerIpAddress = routerIpAddress;
			routerDo.linkSpeed = linkSpeed;
			
			if(hasActiveInternetConnection(context) == true)
				getISPFromIpApi(routerDo);
		}
		return routerDo;
	}
	
	private static void getISPFromIpApi(RouterDo routerDo)
	{
		DefaultHttpClient client = new DefaultHttpClient();
		final HttpGet getRequest = new HttpGet(ipApiUrl);
		try 
		{
			HttpResponse response = client.execute(getRequest);

			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK)
			{
				HttpEntity entity = response.getEntity();
				InputStream inputStream = null;
				if (entity != null)
				{
					try 
					{
						inputStream = entity.getContent();
						if(inputStream != null)
						{
							String data = convertInputStreamToString(inputStream);
							JSONObject jsonObject = new JSONObject(data);
							routerDo.serIp = jsonObject.getString("query");
							routerDo.serHostname = jsonObject.getString("as");
							routerDo.serRegion = jsonObject.getString("region");
							routerDo.serCountryCode = jsonObject.getString("countryCode");
							routerDo.serOrg = jsonObject.getString("org");
							routerDo.serISP = jsonObject.getString("isp");
							routerDo.serLati = jsonObject.getString("lat");
							routerDo.serLong = jsonObject.getString("lon");
						}
						else
						{
							getISPFromTelize(routerDo);
						}
					} 
					finally 
					{
						inputStream.close();
						entity.consumeContent();
					}
				}
			}
			else
			{
				getISPFromTelize(routerDo);
			}
		} 
		catch (Exception e) 
		{
			getRequest.abort();
			getISPFromTelize(routerDo);
		} 
	}
	
	private static void getISPFromTelize(RouterDo routerDo)
	{
		DefaultHttpClient client = new DefaultHttpClient();
		final HttpGet getRequest = new HttpGet(ipTelizeUrl);
		try 
		{
			HttpResponse response = client.execute(getRequest);

			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK)
			{
				HttpEntity entity = response.getEntity();
				InputStream inputStream = null;
				if (entity != null)
				{
					try 
					{
						inputStream = entity.getContent();
						if(inputStream != null)
						{
							String data = convertInputStreamToString(inputStream);
							JSONObject jsonObject = new JSONObject(data);
							routerDo.serIp = jsonObject.getString("ip");
							routerDo.serHostname = jsonObject.getString("asn");
							routerDo.serRegion = jsonObject.getString("country");
							routerDo.serCountryCode = jsonObject.getString("country_code");
							routerDo.serOrg = jsonObject.getString("isp");
							routerDo.serISP = jsonObject.getString("isp");
							routerDo.serLati = jsonObject.getString("latitude");
							routerDo.serLong = jsonObject.getString("longitude");
						}
						else
						{
							getISPFromIpinfo(routerDo);
						}
					} 
					finally 
					{
						inputStream.close();
						entity.consumeContent();
					}
				}
			}
			else
			{
				getISPFromIpinfo(routerDo);
			}
		} 
		catch (Exception e) 
		{
			getRequest.abort();
			getISPFromIpinfo(routerDo);
		} 
	}
	
	private static void getISPFromIpinfo(RouterDo routerDo)
	{
		DefaultHttpClient client = new DefaultHttpClient();
		final HttpGet getRequest = new HttpGet(ipinfoUrl);
		try 
		{
			HttpResponse response = client.execute(getRequest);

			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK)
			{
				HttpEntity entity = response.getEntity();
				InputStream inputStream = null;
				if (entity != null)
				{
					try 
					{
						inputStream = entity.getContent();
						if(inputStream != null)
						{
							String data = convertInputStreamToString(inputStream);
							JSONObject jsonObject = new JSONObject(data);
							if(jsonObject != null && jsonObject.has("loc"))
							{
								routerDo.serIp = jsonObject.getString("ip");
								routerDo.serHostname = jsonObject.getString("hostname");
								routerDo.serRegion = jsonObject.getString("region");
								routerDo.serCountryCode = jsonObject.getString("country");
								String org = jsonObject.getString("org");
								org = org.substring(org.indexOf(" ")+1, org.length());
								routerDo.serOrg = org;
							}
							else
							{
								getISP(routerDo);
							}
						}
					} 
					finally 
					{
						inputStream.close();
						entity.consumeContent();
					}
				}
			}
			else
			{
				getISP(routerDo);
			}
		} 
		catch (Exception e) 
		{
			getRequest.abort();
			getISP(routerDo);
		} 
	}
	
	private static void getISP(RouterDo routerDo)
	{
		DefaultHttpClient client = new DefaultHttpClient();
		final HttpGet getRequest = new HttpGet(ipUrl);
		try 
		{
			HttpResponse response = client.execute(getRequest);

			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK)
			{
				HttpEntity entity = response.getEntity();
				InputStream inputStream = null;
				if (entity != null)
				{
					try 
					{
						inputStream = entity.getContent();
						if(inputStream != null)
						{
							routerDo.serIp = convertInputStreamToString(inputStream);
							getISPDetails(routerDo);
						}
					} 
					finally 
					{
						inputStream.close();
						entity.consumeContent();
					}
				}
			}
		} 
		catch (Exception e) 
		{
			getRequest.abort();
		} 
	}
	
	private static void getISPDetails(RouterDo routerDo)
	{
		DefaultHttpClient client = new DefaultHttpClient();
		final HttpGet getRequest = new HttpGet(ipDetailsUrl+routerDo.serIp);
		try 
		{
			HttpResponse response = client.execute(getRequest);

			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK)
			{
				HttpEntity entity = response.getEntity();
				InputStream inputStream = null;
				if (entity != null)
				{
					try 
					{
						inputStream = entity.getContent();
						if(inputStream != null)
						{
							String data = convertInputStreamToString(inputStream);
							if(data.contains("results") && !data.contains("Sorry!"))
								getISPInfo(data,routerDo);
						}
					} 
					finally 
					{
						inputStream.close();
						entity.consumeContent();
					}
				}
			}
		} 
		catch (Exception e) 
		{
			getRequest.abort();
		} 
	}
	
	private static String convertInputStreamToString(InputStream inputStream) throws IOException
	{
		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while((line = bufferedReader.readLine()) != null)
			result += line;
		return result;
	}
	
	private static void getISPInfo(String res, RouterDo routerDo)
	{
		routerDo.serIp = getSubPart(res, "</ip>");
		routerDo.serHostname = getSubPart(res, "</host>");
		routerDo.serOrg = getSubPart(res, "</org>");
		routerDo.serCountryCode = getSubPart(res, "</countrycode>");
		routerDo.serRegion = getSubPart(res, "</region>");
		routerDo.serISP = getSubPart(res, "</isp>");
		routerDo.serQueries = getSubPart(res, "</queries>");
		routerDo.serLati = getSubPart(res, "</latitude>");
		routerDo.serLong = getSubPart(res, "</longitude>");
	}
	
	private static String getSubPart(String str , String sub)
	{
		return str.substring(str.indexOf(sub.replace("/", "")), str.indexOf(sub)).replace(sub.replace("/", ""), "");
	}
}
