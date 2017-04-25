package com.viasat.etrac.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.viasat.etrac.common.ShareConstants;
import com.viasat.etrac.dataobj.RouterDo;

public class eTracUtils extends Application
{
	protected static eTracUtils Instance = null;
	//private String ulSpeed,dlSpeed,latency,webPageLoad,videoPlayBack,bandWidth;
//	private String serverUrl = "http://speedtest.via-satellite.net/etrac/requests.php";
	//private String serverUrl = "https://etrac.exede.viasat.io/mobile/requests.php";
//	private String iperfUrl = "http://speedtest.via-satellite.net/etrac/content/iperf.json";
	private String speedTestUrl = "http://speedtest.via-satellite.net/etrac/speedof.html";
	private String videoLoadTestUrl = "http://speedtest.via-satellite.net/etrac/youtube.html";
	//private String webLoadTestUrl = "http://speedtest.via-satellite.net/etrac/content/webPageTimer.json";
	private int intWifiState = -1;
	private int runAllTestWait = 1500;
	private int TIME_1_SEC = 1000;
	private int TIME_1_MIN = 60 * TIME_1_SEC;
	private int VL_TimeOut = 1*TIME_1_MIN;
	private int SP_TimeOut = 2*TIME_1_MIN;	
	private int WP_TimeOut = 2*TIME_1_MIN ;
	private int TP_TimeOut = 2*TIME_1_MIN ;
	private RouterDo routerInfo;
	private SharedPreferences prefs;
	
	
	//SWAPNA ADDED SMS API
		private String versionUrl = "https://sms.viasat.io/qa/etrac/?name=version";
		private String iperfUrl = "https://sms.viasat.io/qa/etrac/?name=iperf";
		private String webLoadTestUrl = "https://sms.viasat.io/qa/etrac/?name=webPageTimer";
		private String tosAgreementUrl= "https://sms.viasat.io/qa/etrac/?name=tos";
		private String helpUrl= "https://sms.viasat.io/qa/etrac/?name=help";
		private String tokenUrl ="https://sms.viasat.io/qa/tokens";
		private String serverUrl ="https://sms.viasat.io/qa/etrac/?cmd=";
		
		public String getServerUrl() {
			return serverUrl;
		}
		public void setServerUrl(String serverUrl) {
			this.serverUrl = serverUrl;
		}
		public String getVersionUrl() {
			return versionUrl;
		}
		public void setVersionUrl(String versionUrl) {
			this.versionUrl = versionUrl;
		}
		public String getTosAgreementUrl() {
			return tosAgreementUrl;
		}
		public void setTosAgreementUrl(String tosAgreementUrl) {
			this.tosAgreementUrl = tosAgreementUrl;
		}
		public String getHelpUrl() {
			return helpUrl;
		}
		public void setHelpUrl(String helpUrl) {
			this.helpUrl = helpUrl;
		}

		public String getTokenUrl() {
			return tokenUrl;
		}
		public void setTokenUrl(String tokenUrl) {
			this.tokenUrl = tokenUrl;
		}

		
	
	// Set Methods
	public void setULSpeed(String val) 
	{
		if(val != null)
			saveInPrefs(ShareConstants.SP_UL_SPEED, val);
	}
	public void setDLSpeed(String val) 
	{
		if(val != null)
			saveInPrefs(ShareConstants.SP_DL_SPEED, val);
	}
	public void setLatency(String val) 
	{
		if(val != null)
			saveInPrefs(ShareConstants.SP_LATENCY, val);
	}
	public void setJitter(String val) 
	{
		if(val != null)
			saveInPrefs(ShareConstants.SP_JITTER, val);
	}
	public void setWebPageLoad(String val) 
	{
		if(val != null)
			saveInPrefs(ShareConstants.SP_WEB_PAGE_LOAD_AVG, val);
	}
	public void setWebPagesCount(String val) 
	{
		if(val != null)
			saveInPrefs(ShareConstants.SP_WEB_PAGE_COUNT, val);
	}
	
	public void setWebPageTotTime(String val) 
	{
		if(val != null)
			saveInPrefs(ShareConstants.SP_WEB_PAGE_LOAD_TOTAL, val);
	}
	
	public void setVideoPlayBack(String val) 
	{
		if(val != null)
			saveInPrefs(ShareConstants.SP_VIDEO_PLAY_BACK, val);
	}
	public void setBandWidth(String val)
	{
		if(val != null)
			saveInPrefs(ShareConstants.SP_BAND_WIDTH, val);
	}
	
	public void setRecByte(String val) 
	{
		if(val != null)
			saveInPrefs(ShareConstants.SP_REC_BYTE, val);
	}
	public void setSendByte(String val)
	{
		if(val != null)
			saveInPrefs(ShareConstants.SP_SEND_BYTE, val);
	}
	
	public void setRouterDetails(RouterDo routerInfo)
	{
		if(routerInfo != null)
			this.routerInfo = routerInfo;
	}
	
	public String getServerName()
	{
		return serverUrl;
	}
	
	public String getIperfUrl()
	{
		return iperfUrl;
	}
	
	public String getSpeedTestUrl()
	{
		return speedTestUrl;
	}
	
	public String getVideoLoadTestUrl()
	{
		return videoLoadTestUrl;
	}
	
	public String getWebLoadTestUrl()
	{
		return webLoadTestUrl;
	}
	
	// get Methods
	public String getULSpeed() 
	{
		return getFromPrefs(ShareConstants.SP_UL_SPEED);
	}

	public String getDLSpeed() 
	{
		return getFromPrefs(ShareConstants.SP_DL_SPEED);
	}
	public String getLatency() 
	{
		return getFromPrefs(ShareConstants.SP_LATENCY);
	}
	public String getJitter() 
	{
		return getFromPrefs(ShareConstants.SP_JITTER);
	}
	public String getWebPageLoad() 
	{
		return getFromPrefs(ShareConstants.SP_WEB_PAGE_LOAD_AVG);
	}
	public String getWebPageCount() 
	{
		return getFromPrefs(ShareConstants.SP_WEB_PAGE_COUNT);
	}
	public String getWebPageTotTime() 
	{
		return getFromPrefs(ShareConstants.SP_WEB_PAGE_LOAD_TOTAL);
	}
	public String getVideoPlayBack() 
	{
		return getFromPrefs(ShareConstants.SP_VIDEO_PLAY_BACK);
	}
	public String getBandWidth()
	{
		String val = getFromPrefs(ShareConstants.SP_BAND_WIDTH);
		if(val.equalsIgnoreCase("0"))
			val =  "0 Kbits/sec";
		return val;
	}
	public String getRecByte() 
	{
		return getFromPrefs(ShareConstants.SP_REC_BYTE);
	}
	public String getSendByte() 
	{
		return getFromPrefs(ShareConstants.SP_SEND_BYTE);
	}
	
	public RouterDo getRouterDetails()
	{
		if(routerInfo != null)
			return routerInfo;
		else return new RouterDo();
	}
	
	// Clear Values
	
	public void clearTestValues() 
	{
		String val = "0";
		saveInPrefs(ShareConstants.SP_UL_SPEED, val);
		saveInPrefs(ShareConstants.SP_DL_SPEED, val);
		saveInPrefs(ShareConstants.SP_LATENCY, val);
		saveInPrefs(ShareConstants.SP_WEB_PAGE_LOAD_AVG, val);
		saveInPrefs(ShareConstants.SP_WEB_PAGE_COUNT, val);
		saveInPrefs(ShareConstants.SP_WEB_PAGE_LOAD_TOTAL, val);
		saveInPrefs(ShareConstants.SP_VIDEO_PLAY_BACK, val);
		saveInPrefs(ShareConstants.SP_BAND_WIDTH, val);
		saveInPrefs(ShareConstants.SP_JITTER, val);
		saveInPrefs(ShareConstants.SP_REC_BYTE, val);
		saveInPrefs(ShareConstants.SP_SEND_BYTE, val);
	}
	
	public void onCreate() {
		super.onCreate();
		Instance = this;
		Instance.initializeInstance();
	}

	public static eTracUtils instance() {
		return Instance;
	}

	private void initializeInstance()
	{
	}
	
	public void setWifiState(int val)
	{
		intWifiState = val;
	}
	
	public int getWifiState()
	{
		return intWifiState;
	}
	
	public int getRunAllTestWait()
	{
		return runAllTestWait;
	}
	
	public int getSPTimeOut()
	{
		return SP_TimeOut;
	}
	
	public int getVLTimeOut()
	{
		return VL_TimeOut;
	}
	
	public int getWPTimeOut()
	{
		return WP_TimeOut;
	}
	public int getTPTimeOut()
	{
		return TP_TimeOut;
	}
	
	private void saveInPrefs(String key, String val)
	{
		if(prefs == null)
			prefs = this.getSharedPreferences(ShareConstants.mainPrefsName, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
//		editor.remove(key);
		editor.putString(key, val);
	    editor.commit();
	}
	
	private String getFromPrefs(String key)
	{
		if(prefs == null)
			prefs = this.getSharedPreferences(ShareConstants.mainPrefsName, Context.MODE_PRIVATE);
		return prefs.getString(key, "0");
	}
}
