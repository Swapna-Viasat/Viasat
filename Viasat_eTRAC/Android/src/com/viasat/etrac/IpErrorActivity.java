package com.viasat.etrac;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.viasat.etrac.controls.CustomDialog;
import com.viasat.etrac.utils.NetworkUtils;


public class IpErrorActivity extends Activity
{
	private Button btnTryAgain;
	private CustomDialog loader , wifiAlert;
	private TextView tvTitle;
	Thread work;
	int flagNet = 1;
	int timecount = 0;
	boolean loopflag = true;
	private int eTracId = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.iperror);
		
		if(getIntent().hasExtra("value"))
			showAlert(getIntent().getIntExtra("value", 0));

		flagNet = 1;
		timecount = 0;
		loopflag = true;

		loader = new CustomDialog(IpErrorActivity.this);
		loader.setCancelable(false);
		btnTryAgain  = (Button)findViewById(R.id.btnTryAgain);
		tvTitle      = (TextView)findViewById(R.id.tvTitle);

		tvTitle.setText(R.string.error);

		eTracId = android.os.Process.myPid();

		btnTryAgain.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) 
			{
				loader.show();
				startInterCheck();
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		android.os.Process.killProcess(eTracId);
	}

	public void startInterCheck()
	{
		timecount = 0;
		//---------------------------
		//Thread for changing Wi-fi status runtime.
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				while(true){			

					if(timecount == 0)
					{
						//wait for 1sec
						try {
							Thread.sleep(1000);
							timecount++;
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					flagNet = NetworkUtils.isNetworkAvailable(IpErrorActivity.this);

					if(flagNet == 0)
					{
						//Internet Available
						checkInternet(flagNet);
						break;

					}
					else if(flagNet == 1)
					{
						//WiFi not available
						checkInternet(flagNet);
						break;
					}
					else
					{	
						if(timecount == 2)
						{
							checkInternet(flagNet);
							break;
						}
					}

					if(!loader.isShowing())
					{
						finish();
						android.os.Process.killProcess(eTracId);
					}

					//wait for 500ms
					try {
						Thread.sleep(500);
						timecount++;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		work = new Thread(runnable);
		work.start();
	}

	private void checkInternet(final int value)
	{
		if (value == 0) 
		{
			Intent intent = new Intent(IpErrorActivity.this, RegistrationActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			loader.dismiss();
			startActivity(intent);
			finish();
		}
		else
		{
			loader.dismiss();	

			runOnUiThread(new Runnable() {
				public void run() {
					showAlert(value);               			
				}
			});
		}
	}
	
	private void showAlert(int val)
	{
		int msg = -1;
		if(val == 1)
		{
			msg = R.string.internet_error3;
			OnClickListener listener = new OnClickListener() 
			{
				@Override
				public void onClick(View v)
				{
					wifiAlert.dismiss();
					startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
				}
			};
			wifiAlert = new CustomDialog(IpErrorActivity.this, msg, R.string.dialog_title,listener);
		}
		else if(val == 2)
		{
			msg = R.string.internet_error4;
			wifiAlert = new CustomDialog(IpErrorActivity.this, msg, R.string.dialog_title);
		}
		if(val != -1)
		{
			wifiAlert.setCancelable(false);
			wifiAlert.show();
		}
	}
}
