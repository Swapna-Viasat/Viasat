package com.viasat.etrac;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.viasat.etrac.common.ShareConstants;
import com.viasat.etrac.controls.CustomDialog;
import com.viasat.etrac.dataobj.RouterDo;
import com.viasat.etrac.services.ServerAccess;
import com.viasat.etrac.utils.CalenderUtils;
import com.viasat.etrac.utils.LocationUtils;
import com.viasat.etrac.utils.NetworkUtils;
import com.viasat.etrac.utils.WebServiceManager;
import com.viasat.etrac.utils.eTracUtils;
import com.viasat.etrac.utils.WebServiceManager.SMSAPIConnection;

@SuppressLint("SetJavaScriptEnabled")
public class SpeedTestActivity extends Activity
{
	private TextView tvTitle,tvTeststatus,tvTeststatusTimer;
	private WebView wvSpeedTest;
	private CustomDialog customDialog;
	private boolean countText = false;
	private boolean isToBlink = false;
	private SharedPreferences prefs;
	private eTracUtils myData;
	private ProgressBar pbLoader;
	private Thread work;
	private boolean netflag;
	private int count;
	private String download , upload ,ping , jitter;
	private ImageView ivBack;
	int startTimeCount = 0;
	String release = null;
	long t1,t2 ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speed_test);

		myData = (eTracUtils) getApplication();

		prefs = this.getSharedPreferences(ShareConstants.mainPrefsName, Context.MODE_PRIVATE);

		tvTitle      = (TextView)findViewById(R.id.tvTitle);
		wvSpeedTest  = (WebView)findViewById(R.id.wvSpeedTest);
		tvTeststatus= (TextView)findViewById(R.id.tvTeststatus);
		tvTeststatusTimer= (TextView)findViewById(R.id.tvTeststatusTimer);
		pbLoader = (ProgressBar)findViewById(R.id.pbLoader);
		tvTitle.setText(R.string.speedTest);
		t1 = t2 = 0;
		ivBack	   = (ImageView)findViewById(R.id.ivBack);
		tvTeststatusTimer.setVisibility(View.INVISIBLE);
		
		if(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false))
			ivBack.setVisibility(View.VISIBLE);

		ivBack.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				wvSpeedTest.loadUrl("about:blank");
				netflag = false;
				finish();
			}
		});

		pbLoader.setVisibility(View.VISIBLE);
		tvTeststatusTimer.setVisibility(View.INVISIBLE);		
		wvSpeedTest.setWebViewClient(new WebViewClient()
		{
			@Override
			public void onPageFinished(WebView view, String url) 
			{
				super.onPageFinished(view, url);
				wvSpeedTest.loadUrl("javascript:alert(document.getElementById('download').innerHTML)");
				//checkFor1Min();
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) 
			{
				wvSpeedTest.loadUrl("javascript:alert(document.getElementById('download').innerHTML)");
				//checkFor1Min();
				return super.shouldOverrideUrlLoading(view, url);
			}
		});

		wvSpeedTest.clearCache(true);
		wvSpeedTest.clearHistory();
		wvSpeedTest.getSettings().setJavaScriptEnabled(true);
		wvSpeedTest.setWebChromeClient(new MyChromeClient());
		wvSpeedTest.loadUrl(myData.getSpeedTestUrl());
		
		//Get Android Version
		getAndroidVersion();

		//Start for Thread For networkcheck
		startWifiCheck();
		
	}
	
	public void getAndroidVersion() {
	    release = Build.VERSION.RELEASE;
	}


	class MyChromeClient extends WebChromeClient
	{
		public void onProgressChanged(WebView view, int newProgress)
        {
               if(newProgress == 100)
               {
                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 + 3) //LOLIPOP issue for SpeedTest
                            wvSpeedTest.loadUrl("javascript:alert(document.getElementById('download').innerHTML)");
                     else if(release.contains("4.4.4") == true)		//Kitkat 4.4.4
                    	 wvSpeedTest.loadUrl("javascript:alert(document.getElementById('download').innerHTML)");
               }
               super.onProgressChanged(view, newProgress);
        }

		public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result)  
		{

			if(netflag == false)
				return true;

			if(download == null)
			{
				download = message;
				wvSpeedTest.loadUrl("javascript:alert(document.getElementById('upload').innerHTML)");
			}
			else if(upload == null)
			{
				upload = message;
				wvSpeedTest.loadUrl("javascript:alert(document.getElementById('ping').innerHTML)");
			}
			else if(ping == null)
			{
				ping = message;
				wvSpeedTest.loadUrl("javascript:alert(document.getElementById('jitter').innerHTML)");
			}
			else if(jitter == null)
			{
				jitter = message;
				checkData();
			}

			result.confirm();
			return true;
		};
	}

	private void checkData()
	{

		if(download == null || upload == null|| ping == null|| jitter == null)
			return;

		String msg;
		final int statusMsg;

		if(Float.parseFloat(download) >= 0 && Float.parseFloat(upload) >= 0)
		{
			myData.setULSpeed(upload);
			myData.setDLSpeed(download);
			myData.setLatency(ping);
			myData.setJitter(jitter);

			updateSpeedTestServerDB(download,upload,ping);

			msg = "Download: "+download+" Mbps\nUpload: "+upload+" Mbps\nLatency: "+ping+" ms\nJitter: "+jitter+" ms";

			statusMsg = R.string.testCompleted;
		}
		else
		{
			msg = getString(R.string.testFailed);
			statusMsg = R.string.testFailed;
		}
		
		wvSpeedTest.loadUrl("about:blank");

		isToBlink = false;
		runOnUiThread(new Runnable() 
		{
			public void run() 
			{
				tvTeststatus.setText(statusMsg);
				pbLoader.setVisibility(View.INVISIBLE);
				tvTeststatusTimer.setVisibility(View.INVISIBLE);
			}
		});

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
		
		customDialog = new CustomDialog(SpeedTestActivity.this, msg, R.string.speedTest,clickListener,false);
		customDialog.setCancelable(false);
		customDialog.show();

		checkIsRunAllTest();

	}

	private void updateSpeedTestServerDB(String download, String upload, String ping) 
	{
		String comand 	 = "speedTestResults";
		String macAdd 	 = "";
		String ssid   	 = "";
		String token  	 = "0";
		String device  = "Android";

		SharedPreferences prefs = this.getSharedPreferences(ShareConstants.mainPrefsName, Context.MODE_PRIVATE);
		String email    = prefs.getString(ShareConstants.SP_EMAIL, "");
		String flightid = prefs.getString(ShareConstants.SP_FLIGHTID, "");
		String provider = prefs.getString(ShareConstants.SP_PROVIDER, "");
		String service  = prefs.getString(ShareConstants.SP_SERVICE, "");
		String latitude = prefs.getString(ShareConstants.SP_LATI, "");
		String longitude= prefs.getString(ShareConstants.SP_LONG, "");

		RouterDo routerInfo = NetworkUtils.routerInfo(SpeedTestActivity.this);
		if(routerInfo != null)
		{
			macAdd  = routerInfo.macAdd;
			ssid 	= routerInfo.ssid;
		}
		WebServiceManager webServiceManager = WebServiceManager.getInstance();
		String pref_token = prefs.getString(ShareConstants.SP_TOKEN, "");
		String url;
	
		try {
			

            url = "https://sms.viasat.io/qa/etrac/speedTestResults?"+"&flightID="+URLEncoder.encode(flightid, "UTF-8")+"&email="+URLEncoder.encode(email, "UTF-8")+"&serviceName="+URLEncoder.encode(service, "UTF-8")+"&mac="+URLEncoder.encode(macAdd, "UTF-8")+"&device="+URLEncoder.encode(device, "UTF-8")+"&token="+URLEncoder.encode(pref_token, "UTF-8")+"&providerName="+URLEncoder.encode(provider, "UTF-8")+"&ssid="+URLEncoder.encode(ssid, "UTF-8")+"&timeStamp="+URLEncoder.encode(CalenderUtils.getCurrentTimeStamp(), "UTF-8")+"&latitude="+URLEncoder.encode(latitude, "UTF-8")+"&longitude="+URLEncoder.encode(longitude, "UTF-8")+"&ping="+URLEncoder.encode(ping, "UTF-8")+"&upload="+URLEncoder.encode(upload, "UTF-8")+"&download="+URLEncoder.encode(download, "UTF-8")+"&cmd=speedTestResults";

			String url_Prev = myData.getServerUrl()+URLEncoder.encode(comand, "UTF-8")+"&flightID="+URLEncoder.encode(flightid, "UTF-8")+"&email="+URLEncoder.encode(email, "UTF-8")+"&serviceName="+URLEncoder.encode(service, "UTF-8")+"&mac="+URLEncoder.encode(macAdd, "UTF-8")+"&device="+URLEncoder.encode(device, "UTF-8")+"&token="+URLEncoder.encode(pref_token, "UTF-8")+"&providerName="+URLEncoder.encode(provider, "UTF-8")+"&ssid="+URLEncoder.encode(ssid, "UTF-8")+"&timeStamp="+URLEncoder.encode(CalenderUtils.getCurrentTimeStamp(), "UTF-8")+"&latitude="+URLEncoder.encode(latitude, "UTF-8")+"&longitude="+URLEncoder.encode(longitude, "UTF-8")+"&ping="+URLEncoder.encode(ping, "UTF-8")+"&upload="+URLEncoder.encode(upload, "UTF-8")+"&download="+URLEncoder.encode(download, "UTF-8");
			 webServiceManager.doHttpPost(pref_token, url);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	/*	List<NameValuePair> pairs = new ArrayList<NameValuePair>(15);
		pairs.add(new BasicNameValuePair("cmd", comand));
		pairs.add(new BasicNameValuePair("email", email));
		pairs.add(new BasicNameValuePair("flightID", flightid));
		pairs.add(new BasicNameValuePair("providerName", provider));
		pairs.add(new BasicNameValuePair("serviceName", service));
		pairs.add(new BasicNameValuePair("ssid", ssid));
		pairs.add(new BasicNameValuePair("mac", macAdd));
		pairs.add(new BasicNameValuePair("device", device));
		pairs.add(new BasicNameValuePair("token", token));
		pairs.add(new BasicNameValuePair("latitude", latitude));
		pairs.add(new BasicNameValuePair("longitude", longitude));
		pairs.add(new BasicNameValuePair("ping", ping));
		pairs.add(new BasicNameValuePair("upload", upload));
		pairs.add(new BasicNameValuePair("download", download));
		pairs.add(new BasicNameValuePair("timeStamp", CalenderUtils.getCurrentTimeStamp()));
		ServerAccess serverAceess = new ServerAccess(pairs,myData.getServerName());
		serverAceess.execute();*/
	}

	private void blink()
	{
		final Handler handler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				int timeToBlink = 400;    //in milissegunds
				try{Thread.sleep(timeToBlink);}catch (Exception e) {}
				handler.post(new Runnable() {
					@Override
					public void run() {
						if(countText == true)
						{
							tvTeststatus.setTextColor(0xFFFFFFFF);
							countText = false;
						}else{
							tvTeststatus.setTextColor(0xFFD3D3D3);
							countText = true;
						}

						if(isToBlink == true)
						{
							blink();
						}
						else
						{
							tvTeststatus.setTextColor(0xFFFFFFFF);
						}
					}
				});
			}
		}).start();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if(!isToBlink)
		{
			t1 = System.currentTimeMillis();
			checkFor1Min();
			isToBlink = true;
			tvTeststatusTimer.setVisibility(View.VISIBLE);
			tvTeststatusTimer.setText(getString(R.string.totalTime)+ " " + myData.getSPTimeOut()/1000 + " sec");
			blink();
		}
		getLocation();
	}

	private void getLocation()
	{
		LocationUtils locationUtils = new LocationUtils(SpeedTestActivity.this);
		if(locationUtils.canGetLocation())
		{
			double latitude = locationUtils.getLatitude();
			double longitude = locationUtils.getLongitude();
			prefs.edit().putString(ShareConstants.SP_LATI, latitude+"").commit();
			prefs.edit().putString(ShareConstants.SP_LONG, longitude+"").commit();
		}
	}

	@Override
	public void onBackPressed() 
	{
		if(!getIntent().hasExtra("isRunAllTest") || ! getIntent().getBooleanExtra("isRunAllTest", false))
		{
			super.onBackPressed();
			netflag = false;
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
					int state = NetworkUtils.isNetworkAvailable(SpeedTestActivity.this);
					if(state != 0)
					{
						count ++;
						if(count == 4)
						{
							count = 0;
							runOnUiThread(new Runnable()
							{

								@Override
								public void run() 
								{
									wvSpeedTest.loadUrl("about:blank");
								}
							});
							myData.setWifiState(state);
							netflag = false;
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
					if((isToBlink == true )&&(t1>0))
					{
						t2 = System.currentTimeMillis();
						runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {	           
	                        	long timeDisplay = (myData.getSPTimeOut() - ((t2-t1)))/1000;
	        					tvTeststatusTimer.setText(getString(R.string.totalTime)+ " " +timeDisplay + " sec");
	                        }
	                    });
					}
					//-------------------

					//wait for 500ms
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
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

	private void checkIsRunAllTest()
	{
		if(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false) && netflag)
		{
			if(customDialog != null)
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
						netflag = false;
						Intent intent = new Intent(SpeedTestActivity.this, WebPageLoadTimerActivity.class);
						intent.putExtra("isRunAllTest", true);
						intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
						finish();
					}
				}
			}, myData.getRunAllTestWait());
		}
		else
			netflag = false;
	}

	private void checkFor1Min()
	{
		new Handler().postDelayed(new Runnable() 
		{
			public void run() 
			{
				if(netflag && download == null)
				{
					setTestFailResult();
					if(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false))
					{
						tvTeststatusTimer.setVisibility(View.INVISIBLE);
						customDialog = new CustomDialog(SpeedTestActivity.this, "Speed Test Time Out.", R.string.dialog_title);
						customDialog.show();
						new Handler().postDelayed(new Runnable()
						{
							@Override
							public void run() 
							{
								if(customDialog != null && customDialog.isShowing())
									customDialog.dismiss();
								if(netflag)
								{
									netflag = false;
									Intent intent = new Intent(SpeedTestActivity.this, WebPageLoadTimerActivity.class);
									intent.putExtra("isRunAllTest", true);
									startActivity(intent);
									finish();
								}
							}
						}, myData.getRunAllTestWait());
					}
					else if(netflag)
					{
						netflag = false;
						myData.setWifiState(5);
						finish();
					}
				}
			}
		}, myData.getSPTimeOut());
	}

	private void setTestFailResult()
	{
		myData.setULSpeed("0");
		myData.setDLSpeed("0");
		myData.setLatency("0");
		myData.setJitter("0");
	}
}
