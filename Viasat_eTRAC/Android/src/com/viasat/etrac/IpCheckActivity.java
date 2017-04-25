package com.viasat.etrac;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.viasat.etrac.controls.CustomDialog;
import com.viasat.etrac.utils.NetworkUtils;

public class IpCheckActivity extends Activity
{
	private CustomDialog loader;
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
		setContentView(R.layout.ipcheck);

		flagNet = 1;
		timecount = 0;
		loopflag = true;

		tvTitle = (TextView)findViewById(R.id.tvTitle);

		tvTitle.setText(R.string.app_name);

		eTracId = android.os.Process.myPid();

		loader = new CustomDialog(IpCheckActivity.this);
		loader.setCancelable(false);
		loader.show();		

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

					flagNet = NetworkUtils.isNetworkAvailable(IpCheckActivity.this);

					if(flagNet == 0)
					{
						//Internet available
						checkInternet(flagNet);
						break;
					}
					else if(flagNet == 1)
					{
						//Device not connected to WiFi Router						
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


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		android.os.Process.killProcess(eTracId);
	}

	private void checkInternet(final int value)
	{
		if (value == 0) 
		{
			Intent intent = new Intent(IpCheckActivity.this, RegistrationActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			loader.dismiss();
			startActivity(intent);
		}
		else
		{
			Intent intent = new Intent(IpCheckActivity.this, IpErrorActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.putExtra("value", value);
			loader.dismiss();
			startActivity(intent);			
		}
		finish();
	}

}
