package com.viasat.etrac;

import java.text.DecimalFormat;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.viasat.etrac.adapters.TestDataAdapter;
import com.viasat.etrac.common.ShareConstants;
import com.viasat.etrac.dataobj.TestResultDo;

public class TestDataCompareActivity extends Activity implements OnClickListener
{
	private TextView tvTitle,tvAvgPT,tvAvgUS,tvAvgDS,tvAvgWPL,tvAvgVBT,tvAvgBW,tvViasat,tvNodata,tvTestCount;
	private ImageView ivBack, ivLeftArr, ivRightArr;
	private SharedPreferences prefs;
	private ListView lvTestResults;
	private TestDataAdapter testDataAdapter;
	private Vector<TestResultDo> vecViaSat, vecGOgo, vecPanasonic, vecRow44, vecOthers, vecAno;
	private String[] providers = {"Exede in the Air", "Gogo" , "Panasonic" , "Row44" , "Anonymous", "Others"};
	private int selected = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_data_compare);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		prefs = this.getSharedPreferences(ShareConstants.mainPrefsName, Context.MODE_PRIVATE);

		tvTitle    = (TextView)findViewById(R.id.tvTitle);
		ivBack	   = (ImageView)findViewById(R.id.ivBack);
		tvViasat   = (TextView)findViewById(R.id.tvViasat);
		tvAvgPT    = (TextView)findViewById(R.id.tvAvgPT);
		tvAvgUS    = (TextView)findViewById(R.id.tvAvgUS);
		tvAvgDS    = (TextView)findViewById(R.id.tvAvgDS);
		tvAvgWPL   = (TextView)findViewById(R.id.tvAvgWPL);
		tvAvgVBT   = (TextView)findViewById(R.id.tvAvgVBT);
		tvAvgBW    = (TextView)findViewById(R.id.tvAvgBW);
		lvTestResults = (ListView)findViewById(R.id.lvTestResults);
		tvNodata     = (TextView)findViewById(R.id.tvNodata);
		tvTestCount     = (TextView)findViewById(R.id.tvTestCount);

		ivLeftArr  = (ImageView)findViewById(R.id.ivLeftArr);
		ivRightArr  = (ImageView)findViewById(R.id.ivRightArr);

		tvTitle.setText(R.string.testDataCompar);
		ivBack.setVisibility(View.VISIBLE);
		tvNodata.setVisibility(View.GONE);
		ivBack.setOnClickListener(this);
		ivLeftArr.setOnClickListener(this);
		ivRightArr.setOnClickListener(this);

		testDataAdapter = new TestDataAdapter(TestDataCompareActivity.this, null);
		lvTestResults.setAdapter(testDataAdapter);

		vecViaSat = new Vector<TestResultDo>();
		vecGOgo = new Vector<TestResultDo>();
		vecPanasonic = new Vector<TestResultDo>();
		vecRow44 = new Vector<TestResultDo>();
		vecOthers = new Vector<TestResultDo>();
		vecAno = new Vector<TestResultDo>();

		loadTestResultData();
		showTestResultData();
	}

	private void loadAVGResult(Vector<TestResultDo> testResultDos)
	{
		double totalPT = 0,totalUS = 0,totalDS = 0,totalWPL = 0,totalVBT = 0,totalBW = 0;
		int totalCount = testResultDos.size();
		String bwUnit = "Kbits/sec";
		for (TestResultDo testResultDo : testResultDos) 
		{
			if(testResultDo != null)
			{
				totalPT = totalPT + Double.parseDouble(testResultDo.pingTime);
				totalUS = totalUS + Double.parseDouble(testResultDo.uploadSpeed);
				totalDS = totalDS + Double.parseDouble(testResultDo.downloadSpeed);
				totalWPL = totalWPL + Double.parseDouble(testResultDo.webPageLT);
				totalVBT = totalVBT + Double.parseDouble(testResultDo.videoBufTime);
				String bw[] = testResultDo.bandwidth.split(" ");
				if(bw != null && bw.length >= 2)
				{
					totalBW = totalBW + Double.parseDouble(bw[0]);
					bwUnit = bw[1];
				}
			}
		}

		tvAvgPT.setText(checkMilis(totalPT/totalCount));
		tvAvgUS.setText(checkMbps(totalUS/totalCount));
		tvAvgDS.setText(checkMbps(totalDS/totalCount));
		tvAvgWPL.setText(checkSecs(totalWPL/totalCount));
		tvAvgVBT.setText(checkMilis(totalVBT/totalCount));
		tvAvgBW.setText(getDecimalFormat(totalBW/totalCount) + " "+bwUnit);
	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId()) 
		{
		case R.id.ivBack:
			openPrevActivity();
			finish();
			break;
		case R.id.ivLeftArr:
			if(selected > 0)
			{
				selected = selected - 1;
				updateData(selected);
				if(selected == 0)
					ivLeftArr.setVisibility(View.INVISIBLE);
				if(!ivRightArr.isShown())
					ivRightArr.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.ivRightArr:
			if(selected < 5)
			{
				selected = selected + 1;
				updateData(selected);
				if(selected == 5)
					ivRightArr.setVisibility(View.INVISIBLE);
				if(!ivLeftArr.isShown())
					ivLeftArr.setVisibility(View.VISIBLE);
			}
			break;
		}
	}

	@Override
	public void onBackPressed()
	{
		openPrevActivity();
		finish();		
	}

	public void openPrevActivity()
	{
		Intent intent = getIntent();
		String activity = intent.getStringExtra("activity");
		if(activity.equals("true")){
			intent = new Intent(TestDataCompareActivity.this,RegistrationActivity.class);
			startActivity(intent);
		}else{
			intent = new Intent(TestDataCompareActivity.this,TestResultActivity.class);
			startActivity(intent);
		}
	}	

	private void resetAvgData()
	{
		tvAvgPT.setText("0.0");
		tvAvgUS.setText("0.0");
		tvAvgDS.setText("0.0");
		tvAvgWPL.setText("0.0");
		tvAvgVBT.setText("0.0");
		tvAvgBW.setText("0.0");
	}

	public String checkMilis(double val)
	{
		String result = null;

		if(val > 1000)
		{
			val = val/1000;
			if(val > 60)
			{
				val = val/60;
				result = getDecimalFormat(val) + " min";
			}
			else
				result = getDecimalFormat(val) + " sec";
		}
		else
			result = getDecimalFormat(val) + " ms";

		return result;
	}

	public String checkSecs(double val)
	{
		String result = null;

		if(val > 60)
		{
			val = val/60;
			result = getDecimalFormat(val) + " min";
		}
		else
			result = getDecimalFormat(val) + " sec";

		return result;
	}

	public String checkMbps(double val)
	{
		String result = null;

		if(val > 1000)
		{
			val = val/1000;
			result = getDecimalFormat(val) + " Gbps";
		}
		else
			result = getDecimalFormat(val) + " Mbps";

		return result;
	}

	public String getDecimalFormat(double val)
	{
		DecimalFormat decimalFormat = new DecimalFormat("0.00");
		return decimalFormat.format(val);
	}

	private void loadTestResultData()
	{
		String jArrayString = prefs.getString(ShareConstants.SP_TEST_RESULTS, "NOPREFSAVED");
		if (jArrayString.matches("NOPREFSAVED")) return;
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

					if(testResultDo.provider.equalsIgnoreCase(providers[0]))
						vecViaSat.add(testResultDo);
					else if(testResultDo.provider.equalsIgnoreCase(providers[1]))
						vecGOgo.add(testResultDo);
					else if(testResultDo.provider.equalsIgnoreCase(providers[2]))
						vecPanasonic.add(testResultDo);
					else if(testResultDo.provider.equalsIgnoreCase(providers[3]))
						vecRow44.add(testResultDo);
					else if(testResultDo.provider.equalsIgnoreCase(providers[4]))
						vecAno.add(testResultDo);
					else						
						vecOthers.add(testResultDo);
				}
			} 
			catch (JSONException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void showTestResultData()
	{
		String provider = prefs.getString(ShareConstants.SP_PROVIDER, "");
		if(provider.equalsIgnoreCase(providers[0]))
		{
			selected = 0;
			ivLeftArr.setVisibility(View.INVISIBLE);
		}
		else if(provider.equalsIgnoreCase(providers[1]))
		{
			selected = 1;
		}
		else if(provider.equalsIgnoreCase(providers[2]))
		{
			selected = 2;
		}
		else if(provider.equalsIgnoreCase(providers[3]))
		{
			selected = 3;
		}
		else if(provider.equalsIgnoreCase(providers[4]))
		{
			selected = 4;
		}
		else 
		{
			selected = 5;
			ivRightArr.setVisibility(View.INVISIBLE);
		}
		updateData(selected);
	}

	private void updateData(int sel)
	{
		resetAvgData();
		switch (sel) 
		{
		case 0:
			showViasatData();
			break;
		case 1:
			showGogoData();
			break;
		case 2:
			showPanasonicData();
			break;
		case 3:
			showRow44Data();
			break;
		case 4:
			showAnoData();
			break;
		case 5:			
			showOthersData();
			break;
		}
	}

	private void showViasatData()
	{
		tvViasat.setText("ViaSat");
		if(vecViaSat != null && vecViaSat.size() > 0)
		{
			tvNodata.setVisibility(View.GONE);
			lvTestResults.setVisibility(View.VISIBLE);
			testDataAdapter.refresh(vecViaSat);
			loadAVGResult(vecViaSat);
		}
		else
		{
			lvTestResults.setVisibility(View.GONE);
		}
		tvTestCount.setText(getString(R.string.totalTest)+ " " +vecViaSat.size());
	}

	private void showGogoData()
	{
		tvViasat.setText("Gogo");
		if(vecGOgo != null && vecGOgo.size() > 0)
		{
			tvNodata.setVisibility(View.GONE);
			lvTestResults.setVisibility(View.VISIBLE);
			testDataAdapter.refresh(vecGOgo);
			loadAVGResult(vecGOgo);
		}
		else
		{
			lvTestResults.setVisibility(View.GONE);
		}
		tvTestCount.setText(getString(R.string.totalTest)+ " " +vecGOgo.size());
	}

	private void showPanasonicData()
	{
		tvViasat.setText("Panasonic");
		if(vecPanasonic != null && vecPanasonic.size() > 0)
		{
			tvNodata.setVisibility(View.GONE);
			lvTestResults.setVisibility(View.VISIBLE);
			testDataAdapter.refresh(vecPanasonic);
			loadAVGResult(vecPanasonic);
		}
		else
		{
			lvTestResults.setVisibility(View.GONE);
		}
		tvTestCount.setText(getString(R.string.totalTest)+ " " +vecPanasonic.size());
	}

	private void showRow44Data()
	{
		tvViasat.setText("Row44");
		if(vecRow44 != null && vecRow44.size() > 0)
		{
			tvNodata.setVisibility(View.GONE);
			lvTestResults.setVisibility(View.VISIBLE);
			testDataAdapter.refresh(vecRow44);
			loadAVGResult(vecRow44);
		}
		else
		{
			lvTestResults.setVisibility(View.GONE);
		}
		tvTestCount.setText(getString(R.string.totalTest)+ " " +vecRow44.size());
	}

	private void showAnoData()
	{
		tvViasat.setText("Anonymous");
		if(vecAno != null && vecAno.size() > 0)
		{
			tvNodata.setVisibility(View.GONE);
			lvTestResults.setVisibility(View.VISIBLE);
			testDataAdapter.refresh(vecAno);
			loadAVGResult(vecAno);
		}
		else
		{
			lvTestResults.setVisibility(View.GONE);
		}
		tvTestCount.setText(getString(R.string.totalTest)+ " " +vecAno.size());
	}

	private void showOthersData()
	{
		tvViasat.setText("Others");
		if(vecOthers != null && vecOthers.size() > 0)
		{
			tvNodata.setVisibility(View.GONE);
			lvTestResults.setVisibility(View.VISIBLE);
			testDataAdapter.refresh(vecOthers);
			loadAVGResult(vecOthers);
		}
		else
		{
			lvTestResults.setVisibility(View.GONE);
		}
		tvTestCount.setText(getString(R.string.totalTest)+ " " +vecOthers.size());
	}
}
