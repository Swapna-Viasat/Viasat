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
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
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
public class SendByteCountActivity extends Activity
{
	private TextView tvTitle;
	private CustomDialog customDialog;

	private eTracUtils myData;
	private Thread work;
	private boolean netflag;
	private int count;
	private ImageView ivBack;

	@SuppressWarnings({ })
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sendbytecount);

		myData = (eTracUtils) getApplication();


		tvTitle      = (TextView)findViewById(R.id.tvTitle);
		ivBack	   = (ImageView)findViewById(R.id.ivBack);
		if(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false))
			ivBack.setVisibility(View.VISIBLE);

		ivBack.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				netflag = false;
				finish();
			}
		});

		tvTitle.setText(R.string.sendByteCount);

		startWifiCheck();

		SendByteCountMeasurment();
	}


	private void SendByteCountMeasurment()
	{
		runOnUiThread(new Runnable() 
		{
			@Override
			public void run() 
			{
				RouterDo routerInfo = NetworkUtils.routerInfo(SendByteCountActivity.this);
				if(routerInfo != null)
				{
					long RxByte = TrafficStats.getTotalRxBytes();
					long TxByte = TrafficStats.getTotalTxBytes();

					float mStartRX = (float)RxByte/(float)(1024*1024);
					float mStartTX = (float)TxByte/(float)(1024*1024);

					String sRx = String.format("%.2f", mStartRX);
					String sTx = String.format("%.2f", mStartTX);
					
					myData.setRecByte(sRx);
					myData.setSendByte(sTx);
					//swapna added 14-10-2015
					updateByteCountServerDB(RxByte,TxByte);

					StringBuffer stringBuffer = new StringBuffer();
					stringBuffer.append("Device Received: ").append(sRx).append(" Mb")
					.append("\n\nDevice Transmitted: ").append(sTx).append(" Mb");

					OnClickListener clickListener = new OnClickListener() 
					{						
						@Override
						public void onClick(View v) 
						{
							customDialog.dismiss();
							netflag = false;
							finish();
						}
					};

					customDialog = new CustomDialog(SendByteCountActivity.this, stringBuffer.toString(), R.string.bCount_title, clickListener, false);
					customDialog.setCancelable(false);
					customDialog.show();

					checkIsRunAllTest();
				}
			}
		});
	}

	private void updateByteCountServerDB(long sRx, long sTx) 
	{
		String token  	 = "0";
		String ssid   	 = "";
		String macAdd 	 = "";
		//----------------------------------
		RouterDo routerInfo = NetworkUtils.routerInfo(SendByteCountActivity.this);
		if(routerInfo != null)
		{
			macAdd  = routerInfo.macAdd;
			ssid 	= routerInfo.ssid;
		}
		SharedPreferences prefs = this.getSharedPreferences(ShareConstants.mainPrefsName, Context.MODE_PRIVATE);
		String email    = prefs.getString(ShareConstants.SP_EMAIL, "");
		String flightid = prefs.getString(ShareConstants.SP_FLIGHTID, "");
		String provider = prefs.getString(ShareConstants.SP_PROVIDER, "");
		String service  = prefs.getString(ShareConstants.SP_SERVICE, "");
		//----------------------------------
		String comand 	 = "byteCount";
		WebServiceManager webServiceManager = WebServiceManager.getInstance();
		String pref_token = prefs.getString(ShareConstants.SP_TOKEN, "");
		String url;

			
			 //url=  "https://sms.viasat.io/qa/etrac/byteCount?flightID="+URLEncoder.encode(flightid, "UTF-8")+"&token="+URLEncoder.encode(pref_token, "UTF-8")+"&providerName="+URLEncoder.encode(provider, "UTF-8")+"&ssid="+URLEncoder.encode(ssid, "UTF-8")+"&timeStamp="+URLEncoder.encode("2015-10-14", "UTF-8")+"&byteReceived="+sRx+"&byteSent="+URLEncoder.encode(sTx, "UTF-8");
		
				//url = "https://sms.viasat.io/qa/etrac/byteCount?flightID="+flightid+"&token=0&providerName=prov35&ssid=ssid34&timeStamp=2015-10-13&byteReceived=35&byteSent=35";
				
					//url = myData.getServerUrl()+URLEncoder.encode(comand, "UTF-8")+"&flightID="+URLEncoder.encode(flightid, "UTF-8")+"&email="+URLEncoder.encode(email, "UTF-8")+"&serviceName="+URLEncoder.encode(service, "UTF-8")+"&mac="+URLEncoder.encode(macAdd, "UTF-8")+"&token="+URLEncoder.encode(pref_token, "UTF-8")+"&providerName="+URLEncoder.encode(provider, "UTF-8")+"&ssid="+URLEncoder.encode(ssid, "UTF-8")+"&timeStamp="+URLEncoder.encode(CalenderUtils.getCurrentTimeStamp(), "UTF-8")+"&byteSent="+URLEncoder.encode(ssid, "UTF-8")+sTx+"&byteReceived="+sRx;
				
		Uri.Builder builder = new Uri.Builder();
		/*builder.scheme("https")
		    .authority("sms.viasat.io")
		    .appendPath("qa")
		    .appendPath("etrac")
		     .appendPath("byteCount")
		    .appendQueryParameter("flightID", flightid)
		    .appendQueryParameter("token", pref_token)
		       .appendQueryParameter("providerName", provider)
		          .appendQueryParameter("ssid", ssid)
		             .appendQueryParameter("timeStamp", "2015-10-13")
		                .appendQueryParameter("byteReceived", "11")
		                 .appendQueryParameter("byteSent", "11");*/
		builder.scheme("https")
	    .authority("sms.viasat.io")
	    .appendPath("qa")
	    .appendPath("etrac")
	     .appendPath("byteCount")
	    .appendQueryParameter("flightID", "aa22")
	    .appendQueryParameter("token", "AQIC5wM2LY4Sfcy5idg9xxFc6yS6_-8Qqpk9wyXAvyRgSMw.*AAJTSQACMDIAAlNLABQtNDgzNzM1NTI5MTMwODI5ODU1NgACUzEAAjAz*")
	       .appendQueryParameter("providerName", "sss")
	          .appendQueryParameter("ssid", "ccc")
	             .appendQueryParameter("timeStamp", "2015-10-13")
	                .appendQueryParameter("byteReceived", "11")
	                 .appendQueryParameter("byteSent", "11");
			
			//String Url1 = builder.build().toString();
		String Url1 = "https://sms.viasat.io/qa/etrac/byteCount?flightID=flight35&token=0&providerName=prov35&ssid=ssid34&timeStamp=2015-10-13&byteReceived=35&byteSent=35";
				 webServiceManager.doHttpPost(pref_token, Url1);
			
				
				
			//String url_prev = myData.getServerUrl()+URLEncoder.encode(comand, "UTF-8")+"&flightID="+URLEncoder.encode(flightid, "UTF-8")+"&email="+URLEncoder.encode(email, "UTF-8")+"&serviceName="+URLEncoder.encode(service, "UTF-8")+"&mac="+URLEncoder.encode(macAdd, "UTF-8")+"&token="+URLEncoder.encode(pref_token, "UTF-8")+"&providerName="+URLEncoder.encode(provider, "UTF-8")+"&ssid="+URLEncoder.encode(ssid, "UTF-8")+"&timeStamp="+URLEncoder.encode(CalenderUtils.getCurrentTimeStamp(), "UTF-8")+"&byteSent="+URLEncoder.encode(ssid, "UTF-8")+sTx+"&byteReceived="+sRx;
				
		 
		/*List<NameValuePair> pairs = new ArrayList<NameValuePair>(10);
		pairs.add(new BasicNameValuePair("cmd", comand));
		//----------------------------------
		pairs.add(new BasicNameValuePair("email", email));
		pairs.add(new BasicNameValuePair("flightID", flightid));
		pairs.add(new BasicNameValuePair("providerName", provider));
		pairs.add(new BasicNameValuePair("serviceName", service));
		pairs.add(new BasicNameValuePair("token", token));
		pairs.add(new BasicNameValuePair("ssid", ssid));
		//----------------------------------
		pairs.add(new BasicNameValuePair("byteSent", sTx));
		pairs.add(new BasicNameValuePair("byteReceived", sRx));
		pairs.add(new BasicNameValuePair("timeStamp", CalenderUtils.getCurrentTimeStamp()));
		ServerAccess serverAceess = new ServerAccess(pairs,myData.getServerName());
		serverAceess.execute();*/
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
					int state = NetworkUtils.isNetworkAvailable(SendByteCountActivity.this);
					if(state != 0)
					{
						count ++;
						if(count == 4)
						{
							count = 0;
							myData.setWifiState(3);
							myData.setWifiState(state);
							netflag = false;
							finish();
							break;
						}
					}
					else
						count = 0;

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
						Intent intent = new Intent(SendByteCountActivity.this, ThroughputActivity.class);
						intent.putExtra("isRunAllTest", true);
						startActivity(intent);
						finish();
					}
				}
			}, 2500L);
		}
		else
			netflag = false;
	}
}
