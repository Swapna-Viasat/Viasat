package com.viasat.etrac;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.viasat.etrac.adapters.UrlLoadAdapter;
import com.viasat.etrac.common.ShareConstants;
import com.viasat.etrac.controls.CustomDialog;
import com.viasat.etrac.dataobj.LinkDataDo;
import com.viasat.etrac.dataobj.RouterDo;
import com.viasat.etrac.dataobj.WebLoadDo;
import com.viasat.etrac.listeners.DataListener;
import com.viasat.etrac.services.GetWebLoadData;
import com.viasat.etrac.services.ServerAccess;
import com.viasat.etrac.utils.CalenderUtils;
import com.viasat.etrac.utils.NetworkUtils;
import com.viasat.etrac.utils.WebServiceManager;
import com.viasat.etrac.utils.eTracUtils;
import com.viasat.etrac.utils.WebServiceManager.SMSAPIConnection;

public class WebPageLoadTimerActivity extends Activity implements DataListener
{
	private WebView webview;
	private ListView lv;
	private UrlLoadAdapter adapter;
	private long sTime , eTime;
	private boolean isError = false;
	boolean flag = false;
	private CustomDialog customDialog;
	private TextView tvTitle,tvTeststatusTimer;
	private eTracUtils myData;
	private boolean netflag;
	private int count;
	private Thread work, timer;
	private String curPage = "",prePage = "";	
	private ImageView ivBack;
	private ScheduledExecutorService executor;
	long startTime = 0, endTime = 0, diffTime=0;
	private String msg = "WebPage Load Timer Test Time Out.";
	private boolean blinkFlag = false;
	int startTimeCount = 0;

	@SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webload);
		blinkFlag = false;

		myData = (eTracUtils) getApplication();

		tvTitle      = (TextView)findViewById(R.id.tvTitle);
		tvTeststatusTimer= (TextView)findViewById(R.id.tvTeststatusTimer);
		webview = (WebView)findViewById(R.id.webview);
		lv		= (ListView)findViewById(R.id.lv);

		ivBack	   = (ImageView)findViewById(R.id.ivBack);
		tvTeststatusTimer.setVisibility(View.INVISIBLE);
		if(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false))
			ivBack.setVisibility(View.VISIBLE);

		ivBack.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				webview.loadUrl("about:blank");
				blinkFlag = false;
				netflag = false;
				if(executor != null && !executor.isShutdown())
					executor.shutdown();
				finish();
			}
		});

		tvTitle.setText(R.string.webload);
		webview.setWebViewClient(new MyWebViewClient());
		webview.getSettings().setJavaScriptEnabled(true);
		if(android.os.Build.VERSION.SDK_INT < 17)
		{	
			webview.getSettings().setPluginState(PluginState.ON);
			webview.getSettings().setUserAgentString("Mobile");
		}
		else
		{
			String ua=webview.getSettings().getUserAgentString();
			webview.getSettings().setUserAgentString(ua);
			webview.getSettings().setMediaPlaybackRequiresUserGesture(false);
		}

		webview.getSettings().setDomStorageEnabled(true);
		webview.setDrawingCacheEnabled(true);
		webview.clearCache(true);

		//Start for Thread For networkcheck
		startWifiCheck();

		//statrTimer();  //Having issue with Nexus 5 device
		startCountDown();
		SharedPreferences prefs = this.getSharedPreferences(ShareConstants.mainPrefsName, Context.MODE_PRIVATE);
		String pref_token = prefs.getString(ShareConstants.SP_TOKEN, "");
		GetWebLoadData getServiceData = new GetWebLoadData(this,pref_token);
		getServiceData.execute(myData.getWebLoadTestUrl());
	}


	class MyWebViewClient extends WebViewClient
	{
		private int running = 0;
		boolean reload = false;
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon)
		{
			super.onPageStarted(view, url, favicon);
			sTime = System.currentTimeMillis();
			curPage = url;
			running = Math.max(running, 1);
		}


		@Override
		public void onPageFinished(WebView view, String url)
		{
			--running;
			if(running == 0)
				if(curPage.contentEquals(url) || prePage.contentEquals(url))
				{
					if(isError)
						adapter.refreshView((Integer) view.getTag(),0);
					else if(sTime != 0)
					{
						eTime = System.currentTimeMillis();
						long loadTime = eTime-sTime;
						adapter.refreshView((Integer) view.getTag(),loadTime);
					}
				}
		}


		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) 
		{
			prePage= url;
			return super.shouldOverrideUrlLoading(view, url);	
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,String description, String failingUrl)
		{
			Log.e("Netflag", netflag+"");
			super.onReceivedError(view, errorCode, description, failingUrl);
			isError = true;
		}
	}

	@Override
	public void dataDownloaded(Object object)
	{
		WebLoadDo webLoadDo = (WebLoadDo) object;
		adapter = new UrlLoadAdapter(WebPageLoadTimerActivity.this, webLoadDo.vecLinkDataDos);
		lv.setAdapter(adapter);
		adapter.loadViews(0);
	}

	public void loadingCompleted(Vector<LinkDataDo> vecLinkDataDos)
	{

		if(vecLinkDataDos != null && vecLinkDataDos.size() > 0 && netflag)
		{
			int totalCount = 0;
			double totalloadTime = 0;
			for (LinkDataDo linkDataDo : vecLinkDataDos) 
			{
				if(linkDataDo != null && linkDataDo.isLoaded)
				{
					totalloadTime = totalloadTime + linkDataDo.loadTime;
					totalCount++;
				}
			}

			if(executor != null && !executor.isShutdown())
				executor.shutdown();

			DecimalFormat decimalFormat = new DecimalFormat("0.00");
			String avgRuntime = decimalFormat.format(totalloadTime/totalCount);
			String totalRuntime = decimalFormat.format(totalloadTime);
			myData.setWebPageLoad(avgRuntime);
			myData.setWebPagesCount(String.valueOf(totalCount));
			updateWebLoadTimerServerDB(totalCount,totalRuntime,avgRuntime);

			if(netflag == false)
				return;

			OnClickListener clickListener = new OnClickListener() 
			{						
				@Override
				public void onClick(View v) 
				{
					customDialog.dismiss();
					finish();
				}
			};
			Log.e("loadingCompleted", "Done");
			String msg = "Webpages loaded: "+totalCount+"\nTotal Load Time: "+totalRuntime+" seconds\nAvg. Load Time: "+avgRuntime+" seconds";
			runOnUiThread(new Runnable() {
				@Override
				public void run() {	
					tvTeststatusTimer.setVisibility(View.INVISIBLE);
				}
			});
			customDialog = new CustomDialog(WebPageLoadTimerActivity.this, msg, R.string.webload,clickListener,false);
			customDialog.setCancelable(false);
			runOnUiThread(new Runnable() {

				@Override
				public void run()
				{
					customDialog.show();
				}
			});

			checkIsRunAllTest();
		}
	}

	public void loadWebView(int pos , String url)
	{
		if(netflag)
		{
			curPage= url;
			lv.smoothScrollToPosition(pos,10);
			eTime = 0;
			sTime = 0;
			isError = false;
			webview.setTag(pos);
			webview.loadUrl(url);	
			webview.setFocusable(true);
		}
	}

	private void startWifiCheck()
	{
		count = 0;
		Runnable runnable = new Runnable() 
		{

			@Override
			public void run() 
			{
				netflag = true;
				while(netflag)
				{					
					if(netflag == false)
					{
						count = 0;
						break;
					}
					int state = NetworkUtils.isNetworkAvailable(WebPageLoadTimerActivity.this);
					if(state != 0)
					{
						count ++;
						if(count == 4)
						{
							netflag = false;
							count = 0;
							runOnUiThread(new Runnable() {

								@Override
								public void run() 
								{
									webview.loadUrl("about:blank");
								}
							});
							myData.setWifiState(state);
							if(customDialog != null && customDialog.isShowing())
							{
								customDialog.dismiss();
								customDialog = null;
							}
							finish();
							break;
						}
					}
					else
						count = 0;
					//--------Timer count down-----------
					if(blinkFlag == true)
					{
						runOnUiThread(new Runnable() {
							@Override
							public void run() {	           
								long timeDisplay = (myData.getWPTimeOut() - (diffTime))/1000;								
								tvTeststatusTimer.setText(getString(R.string.totalTime)+ " " +timeDisplay + " sec");
							}
						});
					}
					//-------------------
					if(state == 0)
					{
						//wait for 500ms
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}

				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {	
						tvTeststatusTimer.setVisibility(View.INVISIBLE);
					}
				});
			}
		};

		work = new Thread(runnable);
		work.start();
	}

	private void updateWebLoadTimerServerDB(int numURLs,String totalRuntime,String avgRuntime) 
	{
		String token  	 = "0";
		String ssid   	 = "";
		//----------------------------------
		RouterDo routerInfo = NetworkUtils.routerInfo(WebPageLoadTimerActivity.this);
		if(routerInfo != null)
		{
			//macAdd  = routerInfo.macAdd;
			ssid 	= routerInfo.ssid;
		}
		SharedPreferences prefs = this.getSharedPreferences(ShareConstants.mainPrefsName, Context.MODE_PRIVATE);
		String email    = prefs.getString(ShareConstants.SP_EMAIL, "");
		String flightid = prefs.getString(ShareConstants.SP_FLIGHTID, "");
		String provider = prefs.getString(ShareConstants.SP_PROVIDER, "");
		String service  = prefs.getString(ShareConstants.SP_SERVICE, "");
		String latitude = prefs.getString(ShareConstants.SP_LATI, "");
		String longitude= prefs.getString(ShareConstants.SP_LONG, "");

		//----------------------------------
		String comand 	 = "timerResults";
		String macAdd 	 = "";
		String device  = "Android";
		//----------------------------------
	
		WebServiceManager webServiceManager = WebServiceManager.getInstance();
		String pref_token = prefs.getString(ShareConstants.SP_TOKEN, "");
		String url;
		try {
			url = "https://sms.viasat.io/qa/etrac/timerResults?"+"&flightID="+URLEncoder.encode(flightid, "UTF-8")+"&email="+URLEncoder.encode(email, "UTF-8")+"&serviceName="+URLEncoder.encode(service, "UTF-8")+"&mac="+URLEncoder.encode(macAdd, "UTF-8")+"&device="+URLEncoder.encode(device, "UTF-8")+"&token="+URLEncoder.encode(pref_token, "UTF-8")+"&providerName="+URLEncoder.encode(provider, "UTF-8")+"&ssid="+URLEncoder.encode(ssid, "UTF-8")+"&timeStamp="+URLEncoder.encode(CalenderUtils.getCurrentTimeStamp(), "UTF-8")+
					"&latitude="+URLEncoder.encode(latitude, "UTF-8")+"&longitude="+URLEncoder.encode(longitude, "UTF-8")+"&numURLs="+URLEncoder.encode(String.valueOf(numURLs), "UTF-8")+"&avgRuntime="+URLEncoder.encode(avgRuntime, "UTF-8")+"&totalRuntime="+URLEncoder.encode(totalRuntime, "UTF-8")+"&url0=www.google.com"+"&time0=35.0"+"cmd=timerResults";
	
			//url_prev = myData.getServerUrl()+URLEncoder.encode(comand, "UTF-8")+"&flightID="+URLEncoder.encode(flightid, "UTF-8")+"&email="+URLEncoder.encode(email, "UTF-8")+"&serviceName="+URLEncoder.encode(service, "UTF-8")+"&mac="+URLEncoder.encode(macAdd, "UTF-8")+"&token="+URLEncoder.encode(pref_token, "UTF-8")+"&providerName="+URLEncoder.encode(provider, "UTF-8")+"&ssid="+URLEncoder.encode(ssid, "UTF-8")+"&timeStamp="+URLEncoder.encode(CalenderUtils.getCurrentTimeStamp(), "UTF-8")+"&latitude="+URLEncoder.encode(latitude, "UTF-8")+"&longitude="+URLEncoder.encode(longitude, "UTF-8")+"&numURLs="+URLEncoder.encode(String.valueOf(numURLs), "UTF-8")+"&avgRuntime"+URLEncoder.encode(avgRuntime, "UTF-8")+"&totalRuntime"+URLEncoder.encode(totalRuntime, "UTF-8");
				
			webServiceManager.doHttpPost(pref_token, url);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*List<NameValuePair> pairs = new ArrayList<NameValuePair>(15);
		pairs.add(new BasicNameValuePair("cmd", comand));
		//----------------------------------
		pairs.add(new BasicNameValuePair("email", email));
		pairs.add(new BasicNameValuePair("flightID", flightid));
		pairs.add(new BasicNameValuePair("providerName", provider));
		pairs.add(new BasicNameValuePair("serviceName", service));
		pairs.add(new BasicNameValuePair("token", token));
		pairs.add(new BasicNameValuePair("ssid", ssid));
		//----------------------------------
		pairs.add(new BasicNameValuePair("numURLs", String.valueOf(numURLs)));
		pairs.add(new BasicNameValuePair("avgRuntime", avgRuntime));
		pairs.add(new BasicNameValuePair("totalRuntime", totalRuntime));
		pairs.add(new BasicNameValuePair("timeStamp", CalenderUtils.getCurrentTimeStamp()));
		ServerAccess serverAceess = new ServerAccess(pairs,myData.getServerName());
		serverAceess.execute();*/
		
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	public void onBackPressed() 
	{
		if(!getIntent().hasExtra("isRunAllTest") || ! getIntent().getBooleanExtra("isRunAllTest", false))
		{
			super.onBackPressed();
			if(executor != null && !executor.isShutdown())
				executor.shutdown();
			blinkFlag = false;
			netflag = false;
		}
	}

	private void checkIsRunAllTest()
	{
		if(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false) && netflag)
		{
			customDialog.okButton.setEnabled(false);

			new Handler().postDelayed(new Runnable()
			{

				@Override
				public void run() 
				{
					if(customDialog != null && customDialog.isShowing())
						customDialog.dismiss();					

					if(netflag)
					{
						blinkFlag = false;
						netflag = false;
						//Intent intent = new Intent(WebPageLoadTimerActivity.this, ThroughputActivity.class);
						Intent intent = new Intent(WebPageLoadTimerActivity.this, SendByteCountActivity.class);
						intent.putExtra("isRunAllTest", true);
						startActivity(intent);
						finish();
					}
				}
			}, myData.getRunAllTestWait());
		}
		else{
			blinkFlag = false;
			netflag = false;
		}
	}

	/*private void statrTimer()
	{
		new Handler().postDelayed(new Runnable() {			
			@Override
			public void run()
			{
				if(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false) && netflag)
				{
					setTestFailResult();
					customDialog = new CustomDialog(WebPageLoadTimerActivity.this, "WebPage Load Timer Test Time Out.", R.string.dialog_title);
					customDialog.show();
					new Handler().postDelayed(new Runnable()
					{
						@Override
						public void run() 
						{
							if(customDialog != null && customDialog.isShowing())
							{
								customDialog.dismiss();
								customDialog = null;
							}
							if(netflag)
							{
								netflag = false;
								Intent intent = new Intent(WebPageLoadTimerActivity.this, SendByteCountActivity.class);
								intent.putExtra("isRunAllTest", true);
								startActivity(intent);
								finish();
							}
						}
					}, myData.getRunAllTestWait());
				}
				else if(netflag)
				{
					setTestFailResult();
					myData.setWifiState(6);
					netflag = false;
					if(customDialog != null && customDialog.isShowing())
					{
						customDialog.dismiss();
						customDialog = null;
					}
					finish();
				}
			}
		}, myData.getWPTimeOut());
	}*/

	private void startCountDown()
	{
		blinkFlag = true;
		startTime = System.currentTimeMillis();
		tvTeststatusTimer.setVisibility(View.VISIBLE);
		tvTeststatusTimer.setText(getString(R.string.totalTime)+ " " + myData.getWPTimeOut()/1000 + " sec");
		Runnable runnable = new Runnable() 
		{
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				while(true)
				{
					endTime = System.currentTimeMillis();					
					diffTime = endTime - startTime;

					if(diffTime >= myData.getWPTimeOut())
					{
						blinkFlag = false;
						doWebPageLoadFinish();
						break;
					}

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if(!netflag)
						break;

				}		
			}			
		};

		timer = new Thread(runnable);
		timer.start();
	}


	private void doWebPageLoadFinish()
	{
		int totalCount = 0;
		double totalloadTime = 0;
		String avgRuntime = "";
		String totalRuntime = "";
		if(adapter != null)
		{
			Vector<LinkDataDo> linkDataDos = adapter.getLoadData();
			if(linkDataDos != null && linkDataDos.size() > 0)
			{				
				for (LinkDataDo linkDataDo : linkDataDos) 
				{
					if(linkDataDo != null && linkDataDo.isLoaded)
					{
						totalloadTime = totalloadTime + linkDataDo.loadTime;
						totalCount++;
					}
				}

				DecimalFormat decimalFormat = new DecimalFormat("0.00");
				totalRuntime = decimalFormat.format(totalloadTime);
				avgRuntime = decimalFormat.format(totalloadTime/totalCount);
				myData.setWebPageLoad(avgRuntime);
				myData.setWebPageTotTime(totalRuntime);
				myData.setWebPagesCount(String.valueOf(totalCount));
			}
		}

		if(totalCount < 1)
			setTestFailResult();
		else
		{
			msg  = msg +"\n\nTotal Load Time: "+totalRuntime+" seconds\nAvg. Load Time: "+avgRuntime+" seconds" + "\nWebpages loaded: "+totalCount;
			updateWebLoadTimerServerDB(totalCount,totalRuntime,avgRuntime);
		}

		if(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false) && netflag)
		{
			runOnUiThread(new Runnable(){
				public void run() {
					if(netflag)
					{
						runOnUiThread(new Runnable() {
							@Override
							public void run() {	
								tvTeststatusTimer.setVisibility(View.INVISIBLE);
							}
						});
						customDialog = new CustomDialog(WebPageLoadTimerActivity.this, msg, R.string.dialog_title);
						customDialog.show();
					}
				}
			});		

			try {
				Thread.sleep(2500L); //Dialog to display
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if(customDialog != null && customDialog.isShowing())
			{
				customDialog.dismiss();
				customDialog = null;
			}
			if(netflag)
			{
				blinkFlag = false;
				netflag = false;
				Intent intent = new Intent(WebPageLoadTimerActivity.this, SendByteCountActivity.class);
				intent.putExtra("isRunAllTest", true);
				startActivity(intent);
				finish();
			}
		}
		else if(netflag)
		{
			//setTestFailResult();
			myData.setWifiState(6);
			blinkFlag = false;
			netflag = false;
			finish();
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if(customDialog != null && customDialog.isShowing())
		{
			customDialog.dismiss();
			customDialog = null;
		}
	}

	private void setTestFailResult()
	{
		myData.setWebPageLoad("0");
		myData.setWebPagesCount("0");
	}
}
