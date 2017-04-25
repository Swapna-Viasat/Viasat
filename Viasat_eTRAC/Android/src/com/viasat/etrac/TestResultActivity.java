package com.viasat.etrac;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.viasat.etrac.common.ShareConstants;
import com.viasat.etrac.controls.CustomDialog;
import com.viasat.etrac.dataobj.RouterDo;
import com.viasat.etrac.dataobj.TestResultDo;
import com.viasat.etrac.utils.CalenderUtils;
import com.viasat.etrac.utils.eTracUtils;

public class TestResultActivity extends Activity implements OnClickListener
{
	private TextView tvTitle, tvViaSatSP, tvViaSatDL, tvViaSatUL, tvViaSatLat, tvViaSatWP, tvViaSatVP, tvViaSatBW, tvLabBW, tvViaSatJitter;
	private ImageView ivBack;
	private Button btnResultsDB;
	private eTracUtils myData;
	private SharedPreferences prefs;
	private CustomDialog customDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_result);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		myData = (eTracUtils) getApplication();
		prefs = this.getSharedPreferences(ShareConstants.mainPrefsName, Context.MODE_PRIVATE);


		tvTitle    = (TextView)findViewById(R.id.tvTitle);
		ivBack	   = (ImageView)findViewById(R.id.ivBack);

		tvViaSatSP   = (TextView)findViewById(R.id.tvViaSatSP);
		tvViaSatDL   = (TextView)findViewById(R.id.tvViaSatDL);
		tvViaSatUL   = (TextView)findViewById(R.id.tvViaSatUL);
		tvViaSatLat  = (TextView)findViewById(R.id.tvViaSatLat);
		tvViaSatWP   = (TextView)findViewById(R.id.tvViaSatWP);
		tvViaSatVP   = (TextView)findViewById(R.id.tvViaSatVP);
		tvViaSatBW   = (TextView)findViewById(R.id.tvViaSatBW);
		tvLabBW      = (TextView)findViewById(R.id.tvLabBW);
		tvViaSatJitter = (TextView)findViewById(R.id.tvViaSatJitter);
		btnResultsDB = (Button)findViewById(R.id.btnResultsDB);

		tvTitle.setText(R.string.testResult);
		ivBack.setVisibility(View.VISIBLE);
		ivBack.setOnClickListener(this);
		btnResultsDB.setOnClickListener(this);

		if(prefs.getString(ShareConstants.SP_PROVIDER,"").equalsIgnoreCase("Exede in the Air"))
		{
			tvViaSatSP.setText("ViaSat");
		}		
		else if(prefs.getString(ShareConstants.SP_PROVIDER,"").equalsIgnoreCase("Gogo"))
		{
			tvViaSatSP.setText("Gogo");
		}
		else if(prefs.getString(ShareConstants.SP_PROVIDER,"").equalsIgnoreCase("Panasonic"))
		{
			tvViaSatSP.setText("Panasonic");
		}
		else if(prefs.getString(ShareConstants.SP_PROVIDER,"").equalsIgnoreCase("Row44"))
		{
			tvViaSatSP.setText("Row44");
		}
		else if(prefs.getString(ShareConstants.SP_PROVIDER,"").equalsIgnoreCase("Anonymous"))
		{
			tvViaSatSP.setText("Anonymous");
		}
		else
		{
			tvViaSatSP.setText("Others");
		}


		tvViaSatDL.setText(myData.getDLSpeed());
		tvViaSatUL.setText(myData.getULSpeed());
		tvViaSatLat.setText(myData.getLatency());
		tvViaSatWP.setText(myData.getWebPageLoad());
		tvViaSatVP.setText(myData.getVideoPlayBack());
		tvViaSatJitter.setText(myData.getJitter());
		String bandwidth[] = myData.getBandWidth().split(" ");
		if(bandwidth != null && bandwidth.length >= 2)
		{
			tvLabBW.append(" ("+bandwidth[1]+")");
			tvViaSatBW.setText(bandwidth[0]);
		}

		if(prefs.getString(ShareConstants.SP_PROVIDER,"").equalsIgnoreCase("Exede in the Air"))
		{
			showDialogue();
		}

		if(getIntent().hasExtra("isRunAllTest") && getIntent().getBooleanExtra("isRunAllTest", false))
		{
			RouterDo routerInfo = myData.getRouterDetails()/*NetworkUtils.routerInfo(this)*/;
			updateResultData(routerInfo);
		}

	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
		case R.id.ivBack:
			finish();
			break;
		case R.id.btnResultsDB:
			startActivity(new Intent(TestResultActivity.this, TestDataCompareActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("activity","false"));
			break;
		}
	}

	private void saveData(TestResultDo testResultDo)
	{
		Editor editor = prefs.edit();
		Vector<TestResultDo> testResultDos = getPreviousTestResults();
		if(testResultDos.size() == 50)
			testResultDos.remove(testResultDos.size()-1);
		testResultDos.add(0,testResultDo);

		JSONArray jsonArray = new JSONArray();
		for (int i=0; i < testResultDos.size(); i++)
		{
			jsonArray.put(testResultDos.get(i).getJSONObject());
		}
		editor.remove(ShareConstants.SP_TEST_RESULTS);
		editor.putString(ShareConstants.SP_TEST_RESULTS, jsonArray.toString());
		editor.commit();
	}

	private Vector<TestResultDo> getPreviousTestResults()
	{
		Vector<TestResultDo> testResultDos = new Vector<TestResultDo>();

		String jArrayString = prefs.getString(ShareConstants.SP_TEST_RESULTS, "NOPREFSAVED");
		if (jArrayString.matches("NOPREFSAVED")) return testResultDos;
		else
		{
			try
			{
				JSONArray jArray = new JSONArray(jArrayString);
				for (int j = 0; j < jArray.length(); j++)
				{
					JSONObject jsonObject = jArray.getJSONObject(j);
					TestResultDo testResultDo = new TestResultDo();
					testResultDo.time = jsonObject.getString("time");
					testResultDo.downloadSpeed = jsonObject.getString("downloadSpeed");
					testResultDo.flightId = jsonObject.getString("flightId");
					testResultDo.provider = jsonObject.getString("provider");
					testResultDo.pingTime = jsonObject.getString("pingTime");
					testResultDo.uploadSpeed = jsonObject.getString("uploadSpeed");
					testResultDo.videoBufTime = jsonObject.getString("videoBufTime");
					testResultDo.webPageLT = jsonObject.getString("webPageLT");
					testResultDo.bandwidth = jsonObject.getString("bandwidth");
					testResultDo.carrier = jsonObject.getString("carrier");
					testResultDo.webPageCount = jsonObject.getString("webPageCount");
					testResultDo.jitter = jsonObject.getString("jitter");
					testResultDo.recByte = jsonObject.getString("recByte");
					testResultDo.sendByte = jsonObject.getString("sendByte");

					testResultDos.add(testResultDo);
				}
			} 
			catch (JSONException e) 
			{
				e.printStackTrace();
			}
		}

		return testResultDos;
	}

	private void showDialogue()
	{
		if (myData.getULSpeed().contentEquals("0") == true)
		{
			return;
		} 

		customDialog = new CustomDialog(TestResultActivity.this, R.string.resultAlert, R.string.app_name);
		customDialog.setCancelable(false);
		customDialog.okButton.setEnabled(false);
		customDialog.show();

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run() 
			{
				if(customDialog != null && customDialog.isShowing())
					customDialog.dismiss();
			}
		}, 1500);
	}

	@Override
	public void onBackPressed() 
	{
		super.onBackPressed();
		if(customDialog != null && customDialog.isShowing())
			customDialog.dismiss();
	}

	private void updateResultData(RouterDo routerDo)
	{
		TestResultDo testResultDo = new TestResultDo();
		testResultDo.time = CalenderUtils.getCurrentTimeStamp();
		testResultDo.downloadSpeed = myData.getDLSpeed();
		testResultDo.pingTime = myData.getLatency();
		testResultDo.uploadSpeed = myData.getULSpeed();
		testResultDo.videoBufTime = myData.getVideoPlayBack();
		testResultDo.webPageLT = myData.getWebPageLoad();
		testResultDo.flightId = prefs.getString(ShareConstants.SP_FLIGHTID, "");
		testResultDo.provider = prefs.getString(ShareConstants.SP_PROVIDER, "");
		testResultDo.carrier = routerDo.serOrg;
		testResultDo.webPageCount = myData.getWebPageCount();
		testResultDo.jitter = myData.getJitter();
		testResultDo.recByte = myData.getRecByte();
		testResultDo.sendByte = myData.getSendByte();
		testResultDo.bandwidth = myData.getBandWidth();
		saveData(testResultDo);
	}
}
