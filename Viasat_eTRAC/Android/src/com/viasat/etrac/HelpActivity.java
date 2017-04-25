package com.viasat.etrac;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.viasat.etrac.common.ShareConstants;
import com.viasat.etrac.controls.CustomDialog;
import com.viasat.etrac.services.ServerAccess;
import com.viasat.etrac.utils.NetworkUtils;
import com.viasat.etrac.utils.eTracUtils;
import com.viasat.etrac.utils.WebServiceManager.SMSAPIConnection;

public class HelpActivity extends Activity implements OnClickListener{
	private SharedPreferences prefs;
	private Button btn_Help_Close;
	private WebView wvhelp;
	private TextView tvTitle;
	private eTracUtils myData;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		
		prefs = this.getSharedPreferences(ShareConstants.mainPrefsName, Context.MODE_PRIVATE);

		btn_Help_Close = (Button)findViewById(R.id.btn_Help_Close);
		wvhelp = (WebView)findViewById(R.id.wvhelp);
		tvTitle = (TextView)findViewById(R.id.tvTitle);
		
		btn_Help_Close.setVisibility(View.VISIBLE);
		btn_Help_Close.setOnClickListener(this);
		myData = (eTracUtils) getApplication();
		String helpData = getHelpData();
		tvTitle.setText("ViaSat eTRAC App Help");
		if(helpData != null){
			wvhelp.loadData(helpData, "text/html", "utf-8");
		}else{
			wvhelp.loadUrl("file:///android_asset/help.html");
		}
		
		
		btn_Help_Close.setOnClickListener(this);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		finish();
	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
			case R.id.btn_Help_Close:
				finish();
				break;
				
				
				
			
		}
	}
	
	private String getHelpData()
	{
		
		com.viasat.etrac.utils.WebServiceManager webServiceManager = com.viasat.etrac.utils.WebServiceManager.getInstance();
		 String	 data = webServiceManager.getHelpDataFromService(prefs.getString(ShareConstants.SP_TOKEN, ""),myData.getHelpUrl());
			 return data;
		
	}
		
		public boolean checkInternet()
		{
			boolean ret = false;
			ret = NetworkUtils.hasActiveInternetConnection(HelpActivity.this);
			if(ret == false)
				ret = NetworkUtils.hasActiveInternetConnection(HelpActivity.this);
			
			return ret;
		}
}
