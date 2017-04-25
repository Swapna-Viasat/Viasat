package com.viasat.etrac;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.viasat.etrac.adapters.TestMenuAdapter;
import com.viasat.etrac.controls.CustomDialog;
import com.viasat.etrac.dataobj.RouterDo;
import com.viasat.etrac.listeners.ItemClickListener;
import com.viasat.etrac.utils.NetworkUtils;
import com.viasat.etrac.utils.eTracUtils;

public class TestMenuActivity extends Activity implements OnClickListener , ItemClickListener
{
	private TextView tvTitle;
	private String[] testMenu = {"Run All Tests", "Speed Test" , "Web Page Load Timer" , "Send Byte Count Measurement" , "Throughput Test (iPerf3)" , "Video Load Timer" , "Test Results", "Router Details"};
	private ListView lvTestMenu;
	private ImageView ivBack,ivHelp;
	private TestMenuAdapter testMenuAdapter;
	private Thread work;
	private CustomDialog loader , wifiAlert;
	private eTracUtils myData;
	private String toastMSg = "Test will run for 4-7 minutes be patient, you can see the programs on the screen, but app will not respond to inputs during the runs.";

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.testmenu);

		myData = (eTracUtils) getApplication();
		myData.clearTestValues();

		tvTitle    = (TextView)findViewById(R.id.tvTitle);
		lvTestMenu = (ListView)findViewById(R.id.lvTestMenu);
		ivBack	   = (ImageView)findViewById(R.id.ivBack);
		ivHelp	   = (ImageView)findViewById(R.id.ivHelp);
		
		tvTitle.setText(R.string.testmenu);
		ivBack.setVisibility(View.VISIBLE);
		ivBack.setOnClickListener(this);
		ivHelp.setVisibility(View.VISIBLE);
		ivHelp.setOnClickListener(this);

		testMenuAdapter = new TestMenuAdapter(TestMenuActivity.this,testMenu);
		lvTestMenu.setAdapter(testMenuAdapter);

		ivBack.setTag(true);
		enableList();
		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run() 
			{
				if((Boolean) ivBack.getTag())
				{
					Toast toast = Toast.makeText(getApplicationContext(),toastMSg, Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					performTest(0);
				}		
			}
		}, 1000);

	}
	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
		case R.id.ivBack:
			v.setTag(false);
			finish();
			break;
		case R.id.ivHelp:
			Intent intent = new Intent(TestMenuActivity.this, HelpActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
		
		}
		
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		ivBack.setTag(false);
	}
	@Override
	public void itemClicked(Object object) 
	{
		if(object != null && object instanceof Integer)
			performTest((Integer)object);
	}

	private void performTest(int pos)
	{
		switch (pos) 
		{
		case 6:
			moveToNext(pos);
			break;

		default:
			checkInternet(pos);
			break;
		}
	}

	private void checkInternet(final int pos)
	{
		{
			loader = new CustomDialog(TestMenuActivity.this);
			loader.setCancelable(false);
			loader.show();
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					takeAction(pos);
				}
			};

			work = new Thread(runnable);
			work.start();
		}
	}

	public void takeAction(int pos)
	{
		int ret = -1;
		ret = NetworkUtils.isNetworkAvailable(TestMenuActivity.this);
		if( ret == 0) //Internet Available
		{
			if(pos != 7 && pos != 3)
				loader.dismiss();
			moveToNext(pos);
		}
		else
		{
			if(pos == 7)
			{
				ret = NetworkUtils.isWiFiAvailable(TestMenuActivity.this);
				if( ret == 0) //Internet Available
				{
					showRouterDetails(new RouterDo());
				}
			}
			else
			{
				loader.dismiss();
				showNetAlertDialog(ret);
			}
			enableList();
		}
	}

	public void showNetAlertDialog(final int value)
	{
		runOnUiThread(new Runnable() {
			public void run() {
				loader.dismiss();
				showAlert(value);
			}
		});
	}

	private void moveToNext(final int pos) 
	{
		switch (pos) 
		{
		case 0:
			RunAllTests();
			enableList();
			break;
		case 1:
			SpeedTest();
			enableList();
			break;
		case 2:
			WebPageLoadTimer();
			enableList();
			break;
		case 3:
			SendByteCountMeasurment();
			enableList();
			break;
		case 4:
			ThroughPutTest();
			enableList();
			break;
		case 5:
			VideoLoadTimer();
			enableList();
			break;
		case 6:
			TestResult();
			enableList();
			break;
		case 7:
			RouterDetails();
			enableList();
			break;
		}
	}
	private void TestResult()
	{
		Intent intent = new Intent(TestMenuActivity.this, TestResultActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	private void RouterDetails()
	{
		RouterDo routerInfo = NetworkUtils.routerInfo(TestMenuActivity.this);
		myData.setRouterDetails(routerInfo);
		showRouterDetails(routerInfo);
	}

	private void SendByteCountMeasurment()
	{
		Intent intent = new Intent(TestMenuActivity.this, SendByteCountActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
//		runOnUiThread(new Runnable() 
//		{
//			@Override
//			public void run() 
//			{
//				RouterDo routerInfo = NetworkUtils.routerInfo(TestMenuActivity.this);
//				loader.dismiss();
//				if(routerInfo != null)
//				{
//					long RxByte = TrafficStats.getTotalRxBytes();
//					long TxByte = TrafficStats.getTotalTxBytes();
//
//					float mStartRX = (float)RxByte/(float)(1024*1024);
//					float mStartTX = (float)TxByte/(float)(1024*1024);
//
//					String sRx = String.format("%.2f", mStartRX);
//					String sTx = String.format("%.2f", mStartTX);
//
//					StringBuffer stringBuffer = new StringBuffer();
//					stringBuffer.append("Device Received: ").append(sRx).append(" Mb")
//					.append("\n\nDevice Transmitted: ").append(sTx).append(" Mb");
//
//					CustomDialog customDialog = new CustomDialog(TestMenuActivity.this, stringBuffer.toString(), R.string.bCount_title);
//					customDialog.setCancelable(false);
//					customDialog.show();
//
//					updateByteCountServerDB(sRx,sTx);
//				}
//			}
//		});
	}

	private void VideoLoadTimer() 
	{
		Intent intent = new Intent(TestMenuActivity.this, VideoLoadTestActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}
	private void ThroughPutTest() 
	{
		Intent intent = new Intent(TestMenuActivity.this, ThroughputActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}
	private void WebPageLoadTimer()
	{
		Intent intent = new Intent(TestMenuActivity.this, WebPageLoadTimerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}
	private void SpeedTest() 
	{
		Intent intent = new Intent(TestMenuActivity.this, SpeedTestActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}
	private void RunAllTests()
	{		
		Intent intent = new Intent(TestMenuActivity.this, SpeedTestActivity.class);
		intent.putExtra("isRunAllTest", true);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
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
			wifiAlert = new CustomDialog(TestMenuActivity.this, msg, R.string.dialog_title,listener);
		}
		else if(val == 2)
		{
			msg = R.string.internet_error4;
			wifiAlert = new CustomDialog(TestMenuActivity.this, msg, R.string.dialog_title);
		}
		else if(val == 3)
		{
			msg = R.string.setAgent;
			wifiAlert = new CustomDialog(TestMenuActivity.this, msg, R.string.dialog_title);
		}
		else if(val == 4)
		{
			wifiAlert = new CustomDialog(TestMenuActivity.this, "Video Load Timer Time Out.", R.string.dialog_title);
		}
		else if(val == 5)
		{
			wifiAlert = new CustomDialog(TestMenuActivity.this, "Speed Test Time Out.", R.string.dialog_title);
		}
		else if(val == 6)
		{
			String alertMsg = "WebPage Load Timer Test Time Out.";
			int toatalCount = Integer.parseInt(myData.getWebPageCount());
			if(toatalCount > 0)
			{
				String totalRuntime = myData.getWebPageTotTime();
				String avgRuntime = myData.getWebPageLoad();
				alertMsg = alertMsg + "\n\nToatal load Time: "+totalRuntime+" seconds\nAvg. load Time: "+avgRuntime+" seconds" + "\nWebpages loaded: "+toatalCount;
			}
			wifiAlert = new CustomDialog(TestMenuActivity.this, alertMsg, R.string.dialog_title);
		}
		else if(val == 7)
		{
			wifiAlert = new CustomDialog(TestMenuActivity.this, "Throughput Test Time Out.", R.string.dialog_title);
		}
		if(val != -1 && val != 0)
		{
			wifiAlert.setCancelable(false);
			wifiAlert.show();
		}
	}

	/*private void updateByteCountServerDB(String sRx, String sTx) 
	{
		String comand 	 = "byteCount";
		List<NameValuePair> pairs = new ArrayList<NameValuePair>(10);
		pairs.add(new BasicNameValuePair("cmd", comand));
		pairs.add(new BasicNameValuePair("byteSent", sTx));
		pairs.add(new BasicNameValuePair("byteReceived", sRx));
		pairs.add(new BasicNameValuePair("timeStamp", CalenderUtils.getCurrentTimeStamp()));
		ServerAccess serverAceess = new ServerAccess(pairs,myData.getServerName());
		serverAceess.execute();
	}*/

	@Override
	protected void onResume() 
	{
		super.onResume();
		showAlert(myData.getWifiState());
		myData.setWifiState(-1);
	}

	private void showRouterDetails(final RouterDo routerDo)
	{
		loader.dismiss();
		runOnUiThread(new Runnable() 
		{
			@Override
			public void run() 
			{
				if(routerDo != null)
				{
					LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.router_details, null);
					TextView tvRouterName = (TextView)layout.findViewById(R.id.tvRouterName);
					TextView tvRouterIP = (TextView)layout.findViewById(R.id.tvRouterIP);
					TextView tvRouterBSSID = (TextView)layout.findViewById(R.id.tvRouterBSSID);
					TextView tvRouterLinkSpeed = (TextView)layout.findViewById(R.id.tvRouterLinkSpeed);
					TextView tvISPName = (TextView)layout.findViewById(R.id.tvISPName);
					TextView tvISPIP = (TextView)layout.findViewById(R.id.tvISPIP);
					TextView tvCountry = (TextView)layout.findViewById(R.id.tvCountry);
					ImageView ivCountry = (ImageView)layout.findViewById(R.id.ivCountry);
					ImageView ivCountryDummy = (ImageView)layout.findViewById(R.id.ivCountryDummy);
					TextView tvOk = (TextView)layout.findViewById(R.id.tvOk);

					tvRouterName.setText(routerDo.ssid);
					tvRouterIP.setText(routerDo.routerIpAddress);
					tvRouterBSSID.setText(routerDo.macAdd);
					tvRouterLinkSpeed.setText(routerDo.linkSpeed +" Mbps");
					//if(routerDo.serOrg.length() > 24)
					//	routerDo.serOrg = routerDo.serOrg.substring(0, 20)+"...";
					tvISPName.setText(routerDo.serOrg);
					tvISPIP.setText(routerDo.serIp);
					tvCountry.setText(routerDo.serCountryCode);
					if(routerDo.serCountryCode.equalsIgnoreCase("INDIA") || routerDo.serCountryCode.toUpperCase().contains("IN"))
					{
						ivCountry.setBackgroundResource(R.drawable.in);
						ivCountryDummy.setBackgroundResource(R.drawable.in);
					}
					else if(routerDo.serCountryCode.equalsIgnoreCase("USA") || routerDo.serCountryCode.toUpperCase().contains("US"))
					{
						ivCountry.setBackgroundResource(R.drawable.us);
						ivCountryDummy.setBackgroundResource(R.drawable.us);
					}
					else if(routerDo.serCountryCode.equalsIgnoreCase("CANADA") || routerDo.serCountryCode.toUpperCase().contains("CA"))
					{
						ivCountry.setBackgroundResource(R.drawable.ca);
						ivCountryDummy.setBackgroundResource(R.drawable.ca);
					}

					final CustomDialog customDialog = new CustomDialog(TestMenuActivity.this, layout);
					customDialog.setCancelable(false);
					tvOk.setOnClickListener(new OnClickListener() 
					{
						@Override
						public void onClick(View v) 
						{
							customDialog.dismiss();
						}
					});
					customDialog.show();
				}
			}
		});
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
		if(loader != null && loader.isShowing())
			loader.dismiss();
	}
	private void enableList()
	{
		testMenuAdapter.changeClickState();
	}

}
