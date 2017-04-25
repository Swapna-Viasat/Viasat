package com.viasat.etrac;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.viasat.etrac.common.ShareConstants;
import com.viasat.etrac.utils.CalenderUtils;
import com.viasat.etrac.utils.NetworkUtils;
import com.viasat.etrac.utils.WebServiceManager;
import com.viasat.etrac.utils.WebServiceManager.SMSAPIConnection;
import com.viasat.etrac.utils.eTracUtils;

public class ViaSatEtracStart extends Activity{
	Message msg;
	SharedPreferences prefs;
	int instructVal;
	//stopping splash screen starting home activity.
	private static final int STOPSPLASH = 0;
	//time duration in millisecond for which your splash screen should visible to
	//user. here i have taken half second
	private static final long SPLASHTIME = 3000;
	boolean countText = false;
	boolean appViewState = false;
	private TextView textVer;
    eTracUtils myData;
	//handler for splash screen
	private Handler splashHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STOPSPLASH:
				ViaSatEtracStart.this.finish(); 
				//Generating and Starting new intent on splash time out	
				if(instructVal == 0){
					Intent instructIntent = new Intent(ViaSatEtracStart.this,InstructionsActivity.class);
					instructIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(instructIntent);
				}else{
					Intent intent = new Intent(ViaSatEtracStart.this,IpCheckActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
				}

				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);	
		myData = (eTracUtils) getApplication();
		
		textVer = (TextView)findViewById(R.id.textTata);
		
		try 
		{
			String verServer = getVersion();
			if(verServer!= null){
				textVer.setText(String.format(getString(R.string.ver), verServer));
			}else{
				String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
				
				textVer.setText(String.format(getString(R.string.ver), versionName));
			}		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		
		CalenderUtils.getCurrentTimeStamp();
		
		instructVal = prefs.getInt(ShareConstants.SP_INSTRUCT, 0);
		
		/** Timer to start flashscreen */
		//Generating message and sending it to splash handle 
		msg = new Message();
		msg.what = STOPSPLASH;
		splashHandler.sendMessageDelayed(msg, SPLASHTIME);
	}
	
	
	private SMSAPIConnection getAuthId() 
	{
		String data = "" ;
		if(checkInternet() == true)
		{
		
		 WebServiceManager webServiceManager =WebServiceManager.getInstance();
			SMSAPIConnection smsapiConnection = webServiceManager.getAuthID(myData.getTokenUrl());
			return smsapiConnection;
		}
		return null;
	}
	
	
	public boolean checkInternet()
	{
		boolean ret = false;
		ret = NetworkUtils.hasActiveInternetConnection(ViaSatEtracStart.this);
		if(ret == false)
			ret = NetworkUtils.hasActiveInternetConnection(ViaSatEtracStart.this);
		
		return ret;
	}
	
	
	private String getVersion()
	{
		SMSAPIConnection smsapiConnection = getAuthId();
		prefs = this.getSharedPreferences(ShareConstants.mainPrefsName, Context.MODE_PRIVATE);
        if(smsapiConnection.getAccessToken()!=null) {
            prefs.edit().putString(ShareConstants.SP_TOKEN, smsapiConnection.getAccessToken()).commit();
        }
		com.viasat.etrac.utils.WebServiceManager webServiceManager = com.viasat.etrac.utils.WebServiceManager.getInstance();
		String pref_token = prefs.getString(ShareConstants.SP_TOKEN, "");
		if(pref_token != null){
			 String	 data = webServiceManager.getVersionFromService(pref_token,myData.getVersionUrl());
			 return data;
		}else{
			 smsapiConnection = getAuthId();
			prefs = this.getSharedPreferences(ShareConstants.mainPrefsName, Context.MODE_PRIVATE);
			prefs.edit().putString(ShareConstants.SP_TOKEN, smsapiConnection.getAccessToken()).commit();
		}
			 return null;
		
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		splashHandler.removeMessages(msg.what);	
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		appViewState = true;
		blink();
	}

	@Override
	protected void onPause() {
		super.onPause();
		appViewState = false;
		//finish();
	}
	
	private void blink(){
		final Handler handler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				int timeToBlink = 400;    //in milissegunds
				try{Thread.sleep(timeToBlink);}catch (Exception e) {}
				handler.post(new Runnable() {
					@Override
					public void run() {
						TextView txt = (TextView) findViewById(R.id.app_text);
						TextView txt1 = (TextView) findViewById(R.id.app_text1);
						if(countText == true)
						{
							txt.setTextColor(0xFFFFFFFF);
							txt1.setTextColor(0xFFFFFFFF);
							countText = false;
						}else{
							txt.setTextColor(0xFFFF6600);
							txt1.setTextColor(0xFFFF6600);	
							countText = true;
						}
						
						if(appViewState == true){
							blink();
						}
					}
				});
			}
		}).start();
	}
}
