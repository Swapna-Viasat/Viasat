package com.viasat.etrac;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.viasat.etrac.common.ShareConstants;
import com.viasat.etrac.controls.CustomDialog;
import com.viasat.etrac.controls.ThroughputTest;
import com.viasat.etrac.dataobj.RouterDo;
import com.viasat.etrac.dataobj.ServerInfoDo;
import com.viasat.etrac.listeners.StatusListener;
import com.viasat.etrac.services.ServerAccess;
import com.viasat.etrac.utils.CalenderUtils;
import com.viasat.etrac.utils.LogUtils;
import com.viasat.etrac.utils.NetworkUtils;
import com.viasat.etrac.utils.WebServiceManager;
import com.viasat.etrac.utils.eTracUtils;

public class ThroughputActivity extends Activity implements OnClickListener , StatusListener
{
	private Button btnRunTest,btnStopTest;
	private TextView tvTitle,tvServerRes,tvTeststatus,tvServices,tvTeststatusTimer;
	private eTracUtils myData;
	private GetServiceData getServiceData;
	private ThroughputTest throughputTest;
	boolean countText = false;
	boolean isToBlink = false;
	private CustomDialog customDialog;
	private ProgressBar pbLoader;
	private Thread work;
	private boolean netflag;
	private int count;
	private PopupWindow popupWindow;
	private int width;
	private String defServer = "Amazon(cloud)";
	private ImageView ivBack;
	private ScheduledExecutorService executor;
	int startTimeCount = 0;
	long t1,t2 ;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.throughput);

		myData = (eTracUtils) getApplication();

		tvTitle = (TextView)findViewById(R.id.tvTitle);
		btnRunTest 	= (Button)findViewById(R.id.btnRunTest);
		btnStopTest	= (Button)findViewById(R.id.btnStopTest);
		tvServerRes = (TextView)findViewById(R.id.tvServerRes);
		tvTeststatus= (TextView)findViewById(R.id.tvTeststatus);
		tvTeststatusTimer= (TextView)findViewById(R.id.tvTeststatusTimer);
		pbLoader 	= (ProgressBar)findViewById(R.id.pbLoader);
		tvServices	= (TextView)findViewById(R.id.tvServices);
		ivBack	   = (ImageView)findViewById(R.id.ivBack);
		t1 = t2 = 0;
		if(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false))
			ivBack.setVisibility(View.VISIBLE);
		tvTeststatusTimer.setVisibility(View.INVISIBLE);
		ivBack.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				netflag = false;
				if(getServiceData != null && !getServiceData.isCancelled())
					getServiceData.cancel(true);
				if(executor != null && !executor.isShutdown())
					executor.shutdown();
				stopTest();
				finish();
			}
		});

		tvTitle.setText(R.string.throughtput);

		new Handler().postDelayed(new Runnable()
		{

			@Override
			public void run()
			{
				width = tvServices.getWidth();
			}
		}, 100);

		getServiceData = new GetServiceData();
		getServiceData.execute(myData.getIperfUrl());

		tvServices.setOnClickListener(this);
		btnRunTest.setOnClickListener(this);
		btnStopTest.setOnClickListener(this);

		startWifiCheck();
	}

	@Override
	protected void onResume()
	{
		super.onResume();		
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId()) 
		{
		case R.id.btnRunTest:
			ServerInfoDo infoDo = (ServerInfoDo) v.getTag();
			if(infoDo != null)
			{
				statrTimer();
				tvTeststatus.setText(R.string.testProgress);
				dissableRun();
				tvServerRes.setText("");
				initIperf(infoDo);
			}
			else
			{
				tvTeststatusTimer.setVisibility(View.INVISIBLE);
				customDialog = new CustomDialog(ThroughputActivity.this, getString(R.string.pls_sel_server), R.string.throughtput);
				customDialog.setCancelable(false);
				customDialog.show();
			}
			break;
		case R.id.btnStopTest:
			stopTest();
			break;
		case R.id.tvServices:
			if(popupWindow != null && !popupWindow.isShowing())
				popupWindow.showAsDropDown(v, -5, 0);
			break;
		}
	}

	private class  GetServiceData extends AsyncTask<String, Integer, String>
	{
		@Override
		protected void onPreExecute() 
		{
		}

		@Override
		protected void onPostExecute(String string) 
		{
			if(string == null)
			{
				OnClickListener clickListener = new OnClickListener() 
				{						
					@Override
					public void onClick(View v) 
					{
						customDialog.dismiss();
						if(!(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false)))
						finish();
					}
				};
				tvTeststatusTimer.setVisibility(View.INVISIBLE);
				customDialog = new CustomDialog(ThroughputActivity.this, getString(R.string.serverNotRes), R.string.throughtput,clickListener,false);
				customDialog.setCancelable(false);
				customDialog.show();
				if(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false) && netflag)
				{
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
								Intent intent = new Intent(ThroughputActivity.this, VideoLoadTestActivity.class);
								intent.putExtra("isRunAllTest", true);
								startActivity(intent);
								finish();
							}
						}
					}, myData.getRunAllTestWait());
				}
				return;
			}
			try 
			{
				JSONObject jsonObject = new JSONObject(string);
				JSONArray jsonArray = jsonObject.getJSONObject("iperf3Servers").getJSONArray("servers");
				if(jsonArray != null && jsonArray.length() > 0)
				{
					Vector<ServerInfoDo> vecServerInfoDos = new Vector<ServerInfoDo>();
					for (int i = 0; i < jsonArray.length(); i++) 
					{
						ServerInfoDo serverInfoDo = new ServerInfoDo();
						JSONObject object = jsonArray.getJSONObject(i);
						serverInfoDo.description = object.getString("description");
						serverInfoDo.ip = object.getString("ip");
						serverInfoDo.port = object.getString("port");
						serverInfoDo.timeout = object.getString("timeout");

						vecServerInfoDos.add(serverInfoDo);
					}
					if(vecServerInfoDos != null && vecServerInfoDos.size() > 0)
						loadData(vecServerInfoDos);
				}
			} 
			catch (JSONException e)
			{
				e.printStackTrace();
			}

		}
		@Override
		protected String doInBackground(String... params) 
		{
			String url = params[0];
			// initilize the default HTTP client object
			final DefaultHttpClient client = new DefaultHttpClient();

			//forming a HttoGet request 
			final HttpGet getRequest = new HttpGet(url);
			try 
			{
				HttpResponse response = client.execute(getRequest);

				//check 200 OK for success
				final int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode != HttpStatus.SC_OK)
				{
					return null;
				}

				HttpEntity entity = response.getEntity();
				InputStream inputStream = null;
				if (entity != null)
				{
					try 
					{
						inputStream = entity.getContent();
						if(inputStream != null)
							return convertInputStreamToString(inputStream);
						return null;
					} 
					finally 
					{
						inputStream.close();
						entity.consumeContent();
					}
				}
			} 
			catch (Exception e) 
			{
				// You Could provide a more explicit error message for IOException
				getRequest.abort();
			} 
			return null;
		}

	}

	// convert inputstream to String
	private static String convertInputStreamToString(InputStream inputStream) throws IOException
	{
		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while((line = bufferedReader.readLine()) != null)
			result += line;
		return result;
	}

	//This function is used to copy the iperf executable to a directory which execute permissions for this application, and then gives it execute permissions.
	//It runs on every initiation of an iperf test, but copies the file only if it's needed.
	public void initIperf(ServerInfoDo infoDo)
	{
		InputStream in;
		try 
		{
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
				// Run the file which was created using APP_PLATFORM := <=android-15
				in = getResources().getAssets().open("iperf_w_epic");
			} else {
				// Run the file which was created using APP_PLATFORM := >=android-16
				in = getResources().getAssets().open("iperf");
			}

			//The asset "iperf" (from assets folder) inside the activity is opened for reading.

		} catch (IOException e2)
		{
			LogUtils.error("initIperf","Error occurred :: please reboot and try again.");
			return;			
		}
		try 
		{
			//Checks if the file already exists, if not copies it.
			new FileInputStream("/data/data/com.viasat.etrac/iperf");
		} 
		catch (FileNotFoundException e1)
		{
			try 
			{
				//The file named "iperf" is created in a system designated folder for this application.
				OutputStream out = new FileOutputStream("/data/data/com.viasat.etrac/iperf", false); 
				// Transfer bytes from "in" to "out"
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0)
				{
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
				//After the copy operation is finished, we give execute permissions to the "iperf" executable using shell commands.
				Process processChmod = Runtime.getRuntime().exec("/system/bin/chmod 744 /data/data/com.viasat.etrac/iperf"); 
				// Executes the command and waits untill it finishes.
				processChmod.waitFor();
			} 
			catch (IOException e) 
			{
				LogUtils.error("initIperf","Error occurred while accessing system resources, please reboot and try again.");
				return;
			} 
			catch (InterruptedException e)
			{
				LogUtils.error("initIperf","Error occurred while accessing system resources, please reboot and try again.");
				return;
			}		
			//Creates an instance of the class IperfTask for running an iperf test, then executes.
			throughputTest = new ThroughputTest(ThroughputActivity.this,infoDo);
			throughputTest.execute();				
			return;					
		} 
		//Creates an instance of the class IperfTask for running an iperf test, then executes.
		throughputTest = new ThroughputTest(ThroughputActivity.this,infoDo);
		throughputTest.execute();
		return;
	}

	@Override
	public void onBackPressed()
	{
		if(!getIntent().hasExtra("isRunAllTest") || ! getIntent().getBooleanExtra("isRunAllTest", false))
		{
			super.onBackPressed();
			netflag = false;
			if(getServiceData != null && !getServiceData.isCancelled())
				getServiceData.cancel(true);
			if(executor != null && !executor.isShutdown())
				executor.shutdown();
			stopTest();
		}
	}

	private void stopTest()
	{
		if (throughputTest != null && !throughputTest.isCancelled())
		{
			throughputTest.cancel(true);
			throughputTest = null;
		}
	}

	@Override
	public void testStarted()
	{
		dissableRun();
		tvServerRes.setText("");
		tvTeststatus.setText(R.string.testRunning);
		if(!isToBlink)
		{
			isToBlink = true;
			tvTeststatusTimer.setVisibility(View.VISIBLE);
			tvTeststatusTimer.setText(getString(R.string.totalTime)+ " " + myData.getTPTimeOut()/1000 + " sec");
			t1 = System.currentTimeMillis();
			blink();
		}
		pbLoader.setVisibility(View.VISIBLE);
	}

	@Override
	public void testStatusUpdated(final String data) 
	{
		if(!data.contains("WARNING") && netflag)
		{
			tvServerRes.append(data);
			if(data.contains("Interval       Transfer     Bandwidth"))
			{
				if(executor != null && !executor.isShutdown())
					executor.shutdown();
				String res = data.split("\n")[1];
				res = res.replaceAll("\\s+", " ").substring(res.indexOf("]")+1);
				String str[] = res.trim().split("\\ ");

				if(str.length == 6)
				{
					String interval = str[0] +" "+str[1];
					String transfer = str[2] +" "+str[3];
					String bandwidth= str[4] +" "+str[5];
					String msg = "Interval: "+interval+"\nTransfer: "+transfer+"\nBandwidth: "+bandwidth;
					myData.setBandWidth(bandwidth);
					updateThroughputServerDB(data);

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
					tvTeststatusTimer.setVisibility(View.INVISIBLE);
					customDialog = new CustomDialog(ThroughputActivity.this, msg, R.string.throughtput,clickListener,false);
					customDialog.setCancelable(false);
					customDialog.show();

					checkIsRunAllTest();
				}
			}
		}
	}

	@Override
	public void testCompleted() 
	{
		enableRun();
		tvTeststatus.setText(R.string.testCompleted);
		if(executor != null && !executor.isShutdown())
			executor.shutdown();
		isToBlink = false;
		pbLoader.setVisibility(View.INVISIBLE);

		if(tvServerRes.getText().toString().contains("not responding") || tvServerRes.getText().toString().contains("error") || tvServerRes.getText().toString().contains("failed"))
		{
			setTestFailResult();
			tvTeststatus.setText(R.string.testFailed);
			if(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false) && netflag)
			{
				tvTeststatusTimer.setVisibility(View.INVISIBLE);
				customDialog = new CustomDialog(ThroughputActivity.this, "Throughput Test Failed.", R.string.dialog_title);
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
							Intent intent = new Intent(ThroughputActivity.this, VideoLoadTestActivity.class);
							intent.putExtra("isRunAllTest", true);
							startActivity(intent);
							finish();
						}
					}
				}, myData.getRunAllTestWait());
			}
			else
			{
				OnClickListener clickListener = new OnClickListener() {
					
					@Override
					public void onClick(View v) 
					{
						netflag = false;
						if(customDialog != null && customDialog.isShowing())
							customDialog.dismiss();
						finish();
					}
				};
				tvTeststatusTimer.setVisibility(View.INVISIBLE);
				customDialog = new CustomDialog(ThroughputActivity.this, "Throughput Test Failed.", R.string.dialog_title,clickListener,false);
				customDialog.show();
			}
		}
	}

	@Override
	public void testCanceled() 
	{
		enableRun();
		if(executor != null && !executor.isShutdown())
			executor.shutdown();
		tvTeststatus.setText(R.string.testCanceled);
		isToBlink = false;
		pbLoader.setVisibility(View.INVISIBLE);
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

	private void dissableRun()
	{
		btnRunTest.setEnabled(false);
		tvServices.setEnabled(false);
		btnStopTest.setEnabled(true);
		btnRunTest.setBackgroundResource(R.drawable.buttonshapegray);
		btnStopTest.setBackgroundResource(R.drawable.buttonshape);
	}

	private void enableRun()
	{
		btnRunTest.setEnabled(true);
		tvServices.setEnabled(true);
		btnStopTest.setEnabled(false);
		btnRunTest.setBackgroundResource(R.drawable.buttonshape);
		btnStopTest.setBackgroundResource(R.drawable.buttonshapegray);
	}

	private void updateThroughputServerDB(String iperfResult) 
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
		String comand 	 = "iperfTestResults";
		String macAdd 	 = "";
		String ssid   	 = "";
		String device  = "Android";
		
		WebServiceManager webServiceManager = WebServiceManager.getInstance();
		String pref_token = prefs.getString(ShareConstants.SP_TOKEN, "");
		String url;
		RouterDo routerInfo = NetworkUtils.routerInfo(ThroughputActivity.this);
		if(routerInfo != null)
		{
			macAdd  = routerInfo.macAdd; //priorly comment this 7-10-2015 swapna
			ssid 	= routerInfo.ssid;
		}
		try {
			url = myData.getServerUrl()+URLEncoder.encode(comand, "UTF-8")+"&flightID="+URLEncoder.encode(flightid, "UTF-8")+"&email="+URLEncoder.encode(email, "UTF-8")+"&serviceName="+URLEncoder.encode(service, "UTF-8")+"&mac="+URLEncoder.encode(macAdd, "UTF-8")+"&device="+URLEncoder.encode(device, "UTF-8")+"&token="+URLEncoder.encode(pref_token, "UTF-8")+"&providerName="+URLEncoder.encode(provider, "UTF-8")+"&ssid="+URLEncoder.encode(ssid, "UTF-8")+"&timeStamp="+URLEncoder.encode(CalenderUtils.getCurrentTimeStamp(), "UTF-8")+"latitude"+URLEncoder.encode(latitude, "UTF-8")+"longitude"+URLEncoder.encode(longitude, "UTF-8")+"iperfJsonResult"+URLEncoder.encode(iperfResult, "UTF-8");
			
			webServiceManager.doHttpPost(pref_token, url);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	/*	List<NameValuePair> pairs = new ArrayList<NameValuePair>(10);
		pairs.add(new BasicNameValuePair("cmd", comand));
		//----------------------------------
		pairs.add(new BasicNameValuePair("email", email));
		pairs.add(new BasicNameValuePair("flightID", flightid));
		pairs.add(new BasicNameValuePair("providerName", provider));
		pairs.add(new BasicNameValuePair("serviceName", service));
		//----------------------------------
		pairs.add(new BasicNameValuePair("iperfJsonResult", iperfResult));
		pairs.add(new BasicNameValuePair("timeStamp", CalenderUtils.getCurrentTimeStamp()));
		ServerAccess serverAceess = new ServerAccess(pairs,myData.getServerName());
		serverAceess.execute();*/
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

					int state = NetworkUtils.isNetworkAvailable(ThroughputActivity.this);
					if(state != 0)
					{
						count ++;
						if(count == 4)
						{
							count = 0;
							myData.setWifiState(state);
							if(throughputTest != null)
								throughputTest.cancel(true);
							if(popupWindow != null && popupWindow.isShowing())
								popupWindow.dismiss();
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
						
						//startTimeCount++;
						t2 = System.currentTimeMillis();
						runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {	           
	                        	long timeDisplay = (myData.getTPTimeOut() - (t2-t1))/1000;	                        	
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
					if (netflag) 
					{
						netflag = false;
						Intent intent = new Intent(ThroughputActivity.this, VideoLoadTestActivity.class);
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

	public PopupWindow CustomPopupWindow(String[] popUpContents, TextView tv ,Vector<ServerInfoDo> vecDos)
	{
		PopupWindow popupWindow = new PopupWindow(this);

		ListView listView = new ListView(this);
		listView.setAdapter(popupAdapter(popUpContents));
		listView.setOnItemClickListener(new DropdownOnItemClickListener(tv,vecDos));

		popupWindow.setFocusable(true);
		popupWindow.setWidth(width);
		popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popupWindow.setContentView(listView);

		int index = Arrays.asList(popUpContents).indexOf(defServer);
		if(index >= 0)
		{
			tv.setText(defServer);
			ServerInfoDo infoDo = vecDos.get(index);
			btnRunTest.setTag(infoDo);

			if(getIntent().hasExtra("isRunAllTest"))
			{
				if(getIntent().getBooleanExtra("isRunAllTest", false))
				{
					new Handler().postDelayed(new Runnable()
					{
						@Override
						public void run()
						{
							btnRunTest.performClick();
						}
					}, 500);
				}
			}
		}

		return popupWindow;
	}
	public class DropdownOnItemClickListener implements OnItemClickListener
	{
		private TextView tv;
		private Vector<ServerInfoDo> vecDos;
		public DropdownOnItemClickListener(TextView tv,Vector<ServerInfoDo> vecDos)
		{
			this.tv = tv;
			this.vecDos = vecDos;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) 
		{
			popupWindow.dismiss();
			String selectedItemText = ((TextView) v).getText().toString();
			tv.setText(selectedItemText);
			ServerInfoDo infoDo = vecDos.get(arg2);
			btnRunTest.setTag(infoDo);
			tvServerRes.setText("");
			tvTeststatus.setText(R.string.testNotRunning);
		}

	}
	/*
	 * adapter where the list values will be set
	 */
	private ArrayAdapter<String> popupAdapter(String contens[]) {

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contens) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) 
			{
				String item = getItem(position);
				TextView listItem = new TextView(ThroughputActivity.this);
				listItem.setText(item);
				listItem.setTextSize(18);
				listItem.setPadding(10, 10, 10, 10);
				listItem.setBackgroundColor(Color.WHITE);
				listItem.setTextColor(Color.BLACK);
				return listItem;
			}
		};

		return adapter;
	}

	private void loadData(final Vector<ServerInfoDo> vecDos)
	{
		if(vecDos != null && vecDos.size() > 0)
		{
			final String[] services = new String[vecDos.size()];
			for (ServerInfoDo serverInfoDo : vecDos)
				services[vecDos.indexOf(serverInfoDo)] = serverInfoDo.description;

			if(width <= 0)
			{
				new Handler().postDelayed(new Runnable()
				{

					@Override
					public void run()
					{
						width = tvServices.getWidth();
						popupWindow = CustomPopupWindow(services, tvServices,vecDos);
					}
				}, 100);
			}
			else
			{
				popupWindow = CustomPopupWindow(services, tvServices,vecDos);
			}
		}
	}

	private void statrTimer()
	{
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() 
			{
				if(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false) && netflag)
				{
					setTestFailResult();
					tvTeststatusTimer.setVisibility(View.INVISIBLE);
					customDialog = new CustomDialog(ThroughputActivity.this, "Throughput Test Time Out.", R.string.dialog_title);
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
								Intent intent = new Intent(ThroughputActivity.this, VideoLoadTestActivity.class);
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
					myData.setWifiState(7);
					netflag = false;
					finish();
				}

			}
		}, myData.getTPTimeOut());
	}

	private void setTestFailResult()
	{
		myData.setBandWidth("0");
	}
}
