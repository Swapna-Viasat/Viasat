package com.viasat.etrac;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
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
import com.viasat.etrac.utils.NetworkUtils;
import com.viasat.etrac.utils.WebServiceManager;
import com.viasat.etrac.utils.eTracUtils;

@SuppressLint("SetJavaScriptEnabled")
public class VideoLoadTestActivity extends Activity implements OnTouchListener
{
	private TextView tvTitle,tvTeststatus,tvTeststatusTimer;
	private WebView wvVideoLoadTest;
	private CustomDialog customDialog;
	private boolean countText = false;
	private boolean isToBlink = false;
	private eTracUtils myData;
	private ProgressBar pbLoader;
	private Thread work;
	private boolean netflag;
	private int count;
	private String videoId , initialQuality ,initialBufferTime;
	boolean playerFlag = false , isPlaying = false;
	private Thread workYoutube;
	private int width,height;
	private long sTime=0, eTime=0;
	private ScheduledExecutorService executor;
	private ImageView ivBack;
	private String HTML_DATA;
	int startTimeCount = 0;
	long t1,t2 ;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_load_test);

		myData = (eTracUtils) getApplication();
		sTime=0;
		eTime=0;
		playerFlag = false;
		t1 = t2 = 0;
		tvTitle      = (TextView)findViewById(R.id.tvTitle);
		wvVideoLoadTest  = (WebView)findViewById(R.id.wvVideoLoadTest);
		tvTeststatus= (TextView)findViewById(R.id.tvTeststatus);
		tvTeststatusTimer= (TextView)findViewById(R.id.tvTeststatusTimer);
		pbLoader = (ProgressBar)findViewById(R.id.pbLoader);
		ivBack	   = (ImageView)findViewById(R.id.ivBack);
		tvTeststatusTimer.setVisibility(View.INVISIBLE);
		if(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false))
			ivBack.setVisibility(View.VISIBLE);

		ivBack.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				wvVideoLoadTest.loadUrl("about:blank");
				netflag = false;
				if(executor != null && !executor.isShutdown())
					executor.shutdown();
				finish();
			}
		});

		tvTitle.setText(R.string.videoLoadTimer);
		pbLoader.setVisibility(View.VISIBLE);
		wvVideoLoadTest.setOnTouchListener(this);

		if(wvVideoLoadTest.getTag() != null)
		{
			String[] str = ((String) wvVideoLoadTest.getTag()).split(",");
			width = Integer.parseInt(str[0]);
			height = Integer.parseInt(str[1]);
		}

		wvVideoLoadTest.setWebViewClient(new WebViewClient()
		{
			@Override
			public void onPageFinished(WebView view, String url) 
			{
				super.onPageFinished(view, url);
				wvVideoLoadTest.loadUrl("javascript:alert(getInitialQuality())");
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) 
			{
				if(url.equalsIgnoreCase("about:videoComplete"))
					wvVideoLoadTest.loadUrl("javascript:alert(getInitialQuality())");
				return true;
			}
		});


		wvVideoLoadTest.getSettings().setJavaScriptEnabled(true);
		String ua=wvVideoLoadTest.getSettings().getUserAgentString();
		wvVideoLoadTest.getSettings().setUserAgentString(ua);

		if(android.os.Build.VERSION.SDK_INT < 17)
		{	
			wvVideoLoadTest.getSettings().setUserAgentString("Mobile");
			wvVideoLoadTest.getSettings().setPluginState(PluginState.ON);
			//wvVideoLoadTest.loadUrl(myData.getVideoLoadTestUrl());	
		}
		else
		{
			wvVideoLoadTest.getSettings().setUserAgentString(ua);
			wvVideoLoadTest.getSettings().setMediaPlaybackRequiresUserGesture(false);
		}

		wvVideoLoadTest.clearCache(true);
		wvVideoLoadTest.clearHistory();
		wvVideoLoadTest.setWebChromeClient(new MyChromeClient());

		startWifiCheck();
	}


	class MyChromeClient extends WebChromeClient
	{	
		
		public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result)  
		{
			if(netflag == false)
				return true;

			if(initialQuality == null && !message.equalsIgnoreCase("undefined"))
			{
				initialQuality = message;
				wvVideoLoadTest.loadUrl("javascript:alert(getInitialBufferTime())");
			}
			else if(initialQuality != null && initialBufferTime == null)
			{
				if(message.equalsIgnoreCase("0"))
					message =String.valueOf((eTime - sTime));
				initialBufferTime = message;
				wvVideoLoadTest.loadUrl("javascript:alert(getVideoId())");
			}
			else if(initialQuality != null && initialBufferTime != null && videoId == null)
			{
				videoId = message;
				checkData();
			}

			result.confirm();
			return true;
		};


		@Override
		public boolean onConsoleMessage(ConsoleMessage consoleMessage)
		{
			if(consoleMessage.message().equalsIgnoreCase("playing"))
			{
				eTime = System.currentTimeMillis();
				isPlaying = true;
			}
			if(consoleMessage.message().equalsIgnoreCase("player quality change") && sTime == 0)
				sTime = System.currentTimeMillis();
			return super.onConsoleMessage(consoleMessage);
		}
	}

	private void checkData()
	{

		if(videoId == null || initialQuality == null|| initialBufferTime == null)
			return;
		
		wvVideoLoadTest.loadUrl("about:blank");

		myData.setVideoPlayBack(initialBufferTime);

		updateVideoLoadTestServerDB(videoId,initialQuality,initialBufferTime);

		String msg = "Video Id: "+videoId+"\nInitial Quality: "+initialQuality+"\nInitial Buffer Time: "+initialBufferTime+" ms";

		isToBlink = false;
		runOnUiThread(new Runnable() 
		{
			public void run() 
			{
				tvTeststatus.setText(R.string.testCompleted);
				pbLoader.setVisibility(View.INVISIBLE);
			}
		});

		if(netflag == false)
			return;

		if(executor != null && !executor.isShutdown())
			executor.shutdown();

		OnClickListener clickListener = new OnClickListener() 
		{						
			@Override
			public void onClick(View v) 
			{
				customDialog.dismiss();
				finish();
			}
		};
		tvTeststatusTimer.setVisibility(View.INVISIBLE);
		customDialog = new CustomDialog(VideoLoadTestActivity.this, msg, R.string.videoLoadTimer,clickListener,false);
		customDialog.setCancelable(false);
		customDialog.show();

		checkIsRunAllTest();
	}

	private void updateVideoLoadTestServerDB(String videoId, String initialQuality, String initialBufferTime) 
	{
		//----------------------------------
		SharedPreferences prefs = this.getSharedPreferences(ShareConstants.mainPrefsName, Context.MODE_PRIVATE);
		String email    = prefs.getString(ShareConstants.SP_EMAIL, "");
		String flightid = prefs.getString(ShareConstants.SP_FLIGHTID, "");
		String provider = prefs.getString(ShareConstants.SP_PROVIDER, "");
		String service  = prefs.getString(ShareConstants.SP_SERVICE, "");
		String latitude = prefs.getString(ShareConstants.SP_LATI, "");
		String longitude= prefs.getString(ShareConstants.SP_LONG, "");
		
		//----------------------------------
		String comand = "videoTestResults";
		String macAdd 	 = "";
		String ssid   	 = "";
		String device  = "Android";
		RouterDo routerInfo = NetworkUtils.routerInfo(VideoLoadTestActivity.this);
		if(routerInfo != null)
		{
			macAdd  = routerInfo.macAdd; //priorly comment this 7-10-2015 swapna
			ssid 	= routerInfo.ssid;
		}
		WebServiceManager webServiceManager = WebServiceManager.getInstance();
		String pref_token = prefs.getString(ShareConstants.SP_TOKEN, "");
		String url;
	
		try {
				
				url = "https://sms.viasat.io/qa/etrac/videoTestResults?"+"&flightID="+URLEncoder.encode(flightid, "UTF-8")+"&email="+URLEncoder.encode(email, "UTF-8")+"&serviceName="+URLEncoder.encode(service, "UTF-8")+"&mac="+URLEncoder.encode(macAdd, "UTF-8")+"&device="+URLEncoder.encode(device, "UTF-8")+"&token="+URLEncoder.encode(pref_token, "UTF-8")+"&providerName="+URLEncoder.encode(provider, "UTF-8")+"&ssid="+URLEncoder.encode(ssid, "UTF-8")+"&timeStamp="+URLEncoder.encode(CalenderUtils.getCurrentTimeStamp(), "UTF-8")+"&latitude="+URLEncoder.encode(latitude, "UTF-8")+"&longitude="+URLEncoder.encode(longitude, "UTF-8")+"videoId"+URLEncoder.encode(videoId, "UTF-8")+"&initialQuality="+URLEncoder.encode(initialQuality, "UTF-8")+"&initialBufferTime="+URLEncoder.encode(initialBufferTime, "UTF-8")+"&cmd=videoTestResults";
			//String url_prev =  myData.getServerUrl()+URLEncoder.encode(comand, "UTF-8")+"&flightID="+URLEncoder.encode(flightid, "UTF-8")+"&email="+URLEncoder.encode(email, "UTF-8")+"&serviceName="+URLEncoder.encode(service, "UTF-8")+"&mac="+URLEncoder.encode(macAdd, "UTF-8")+"&device="+URLEncoder.encode(device, "UTF-8")+"&token="+URLEncoder.encode(pref_token, "UTF-8")+"&providerName="+URLEncoder.encode(provider, "UTF-8")+"&ssid="+URLEncoder.encode(ssid, "UTF-8")+"&timeStamp="+URLEncoder.encode(CalenderUtils.getCurrentTimeStamp(), "UTF-8")+"latitude"+URLEncoder.encode(latitude, "UTF-8")+"longitude"+URLEncoder.encode(longitude, "UTF-8")+"initialBufferTime"+URLEncoder.encode(initialBufferTime, "UTF-8")+"initialQuality"+URLEncoder.encode(initialQuality, "UTF-8")+"videoId"+URLEncoder.encode(videoId, "UTF-8");
			
				 webServiceManager.doHttpPost(pref_token, url);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
/*		List<NameValuePair> pairs = new ArrayList<NameValuePair>(15);
		pairs.add(new BasicNameValuePair("cmd", comand));
		//----------------------------------
		pairs.add(new BasicNameValuePair("email", email));
		pairs.add(new BasicNameValuePair("flightID", flightid));
		pairs.add(new BasicNameValuePair("providerName", provider));
		pairs.add(new BasicNameValuePair("serviceName", service));
		//----------------------------------
		pairs.add(new BasicNameValuePair("videoId", videoId));
		pairs.add(new BasicNameValuePair("initialQuality", initialQuality));
		pairs.add(new BasicNameValuePair("initialBufferTime", initialBufferTime));
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
			isToBlink = true;
			tvTeststatusTimer.setVisibility(View.VISIBLE);
			tvTeststatusTimer.setText(getString(R.string.totalTime)+ " " + myData.getVLTimeOut()/1000 + " sec");
			t1 = System.currentTimeMillis();
			blink();
		}

		if(playerFlag == false)
		{
			startYoutube();
			statrTimer();
			playerFlag = true;
		}
	}
	@Override
	public void onBackPressed() 
	{
		if(!getIntent().hasExtra("isRunAllTest") || ! getIntent().getBooleanExtra("isRunAllTest", false))
		{
			wvVideoLoadTest.loadUrl("about:blank");
			//wvVideoLoadTest.clearView();
			if(executor != null && !executor.isShutdown())
				executor.shutdown();
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
					int state = NetworkUtils.isNetworkAvailable(VideoLoadTestActivity.this);
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
									wvVideoLoadTest.loadUrl("about:blank");
								}
							});
							myData.setWifiState(3);
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
					if(isToBlink == true)
					{
						t2 = System.currentTimeMillis();
						runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {	     
	                        	long timeDisplay = (myData.getVLTimeOut() - (t2-t1))/1000;	                        	
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
			}
		};

		work = new Thread(runnable);
		work.start();
	}

	private void startYoutube()
	{
		Runnable runnable = new Runnable() 
		{			
			public void run() 
			{
				HTML_DATA = null;
				try
				{
					HTML_DATA = getHTML(myData.getVideoLoadTestUrl());
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}				

				runOnUiThread(new Runnable() 
				{
					@Override
					public void run() {
						if(netflag)
							wvVideoLoadTest.loadDataWithBaseURL(null, HTML_DATA, "text/html", "UTF-8", null);
					}
				});
			}
		};

		workYoutube = new Thread(runnable);
		workYoutube.start();
	}

	String getHTML(String url) throws IOException
	{
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);

		String html = "";
		html = EntityUtils.toString(response.getEntity(), "UTF-8");

		if(height > 0)
			html = html.replace("height: '195'", "height: '"+height+"'");
		if(width > 0)
			html = html.replace("width: '320'", "width: '"+width+"'");


		return html;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		return true;
	}

	private void statrTimer()
	{
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() 
			{
				if(isPlaying)
				{
					wvVideoLoadTest.loadUrl("javascript:alert(getInitialQuality())");
				}
				else if(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false) && netflag && !isPlaying)
				{
					setTestFailResult();
					tvTeststatusTimer.setVisibility(View.INVISIBLE);
					customDialog = new CustomDialog(VideoLoadTestActivity.this, "Video Load Timer Time Out.", R.string.dialog_title);
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
								wvVideoLoadTest.loadUrl("about:blank");
								netflag = false;
								Intent intent = new Intent(VideoLoadTestActivity.this, TestResultActivity.class);
								intent.putExtra("isRunAllTest", true);
								startActivity(intent);
								finish();
							}
						}
					}, myData.getRunAllTestWait());
				}
				else if(netflag && !isPlaying)
				{
					setTestFailResult();
					myData.setWifiState(4);
					wvVideoLoadTest.loadUrl("about:blank");
					netflag = false;
					finish();
				}

			}
		}, myData.getVLTimeOut());
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
						Intent intent = new Intent(VideoLoadTestActivity.this, TestResultActivity.class);
						intent.putExtra("isRunAllTest", true);
						startActivity(intent);
						finish();
					}
				}
			}, myData.getRunAllTestWait());
		}
		else
			netflag = false;
	}

	private void setTestFailResult()
	{
		myData.setVideoPlayBack("0");
	}
}
