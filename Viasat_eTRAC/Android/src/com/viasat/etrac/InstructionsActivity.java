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
import com.viasat.etrac.utils.NetworkUtils;
import com.viasat.etrac.utils.WebServiceManager.SMSAPIConnection;
import com.viasat.etrac.utils.eTracUtils;

public class InstructionsActivity extends Activity implements OnClickListener{
	private SharedPreferences prefs;
	private Button btnAccept , btnDecline;
	private WebView wvInstuctions;
	private TextView tvTitle;
	eTracUtils myData;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.instruction);
		myData = (eTracUtils) getApplication();
		prefs = this.getSharedPreferences(ShareConstants.mainPrefsName, Context.MODE_PRIVATE);

		btnAccept = (Button)findViewById(R.id.btnAccept);
		btnDecline = (Button)findViewById(R.id.btnDecline);
		wvInstuctions = (WebView)findViewById(R.id.wvInstuctions);
		tvTitle = (TextView)findViewById(R.id.tvTitle);
		String helpData = getTermsData();
		tvTitle.setText("Terms and Conditions");
		if(helpData != null){
			wvInstuctions.loadData(helpData, "text/html", "utf-8");
		}else{
			wvInstuctions.loadUrl("file:///android_asset/viasat.html");
		}
		
		
		
		btnAccept.setOnClickListener(this);
		btnDecline.setOnClickListener(this);
	}
	
	
	private String getTermsData()
	{
		if(checkInternet() == true)
		{
			
			com.viasat.etrac.utils.WebServiceManager webServiceManager = com.viasat.etrac.utils.WebServiceManager.getInstance();
			
				
				 String	 data = webServiceManager.getTermsDataFromService(prefs.getString(ShareConstants.SP_TOKEN, ""),myData.getTosAgreementUrl());
				 return data;
			
		}
		
		
		
		 return null;
	}
	
	
	public boolean checkInternet()
	{
		boolean ret = false;
		ret = NetworkUtils.hasActiveInternetConnection(InstructionsActivity.this);
		if(ret == false)
			ret = NetworkUtils.hasActiveInternetConnection(InstructionsActivity.this);
		
		return ret;
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
			case R.id.btnAccept:				
				prefs.edit().putInt(ShareConstants.SP_INSTRUCT, 1).commit();
				Intent intent = new Intent(InstructionsActivity.this,IpCheckActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);				
				break;
			case R.id.btnDecline:				
				CustomDialog customDialog = new CustomDialog(InstructionsActivity.this, R.string.instruct_msg, R.string.dialog_title);
				customDialog.show();				
				break;
		}
	}
}
