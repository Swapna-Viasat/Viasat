package com.viasat.etrac.adapters;

import java.util.Vector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.viasat.etrac.R;
import com.viasat.etrac.TestDataCompareActivity;
import com.viasat.etrac.dataobj.TestResultDo;

public class TestDataAdapter extends BaseAdapter
{
	private Context context;
	private Vector<TestResultDo> testResultDos;
	public TestDataAdapter(Context context, Vector<TestResultDo> testResultDos) 
	{
		this.context = context;
		this.testResultDos = testResultDos;
	}

	@Override
	public int getCount() 
	{
		if(testResultDos != null && testResultDos.size() > 0)
			return testResultDos.size();
		return 0;
	}

	@Override
	public Object getItem(int position) 
	{
		return position;
	}

	@Override
	public long getItemId(int position) 
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		TestResultDo testResultDo = testResultDos.get(position);
		if(convertView == null)
			convertView = LayoutInflater.from(context).inflate(R.layout.test_data_cell, null);
		
		TextView tvTime,tvFlightId,tvPT,tvUS,tvDS,tvWPL,tvVBT,tvBW,tvCarrier,tvBWLable,tvBWColon,tvJitter,tvWPC,tvRB,tvSB,tvTimeTitle;
		tvTime     = (TextView)convertView.findViewById(R.id.tvTime);
		tvFlightId = (TextView)convertView.findViewById(R.id.tvFlightId);
		tvPT       = (TextView)convertView.findViewById(R.id.tvPT);
		tvUS       = (TextView)convertView.findViewById(R.id.tvUS);
		tvDS       = (TextView)convertView.findViewById(R.id.tvDS);
		tvWPL      = (TextView)convertView.findViewById(R.id.tvWPL);
		tvVBT      = (TextView)convertView.findViewById(R.id.tvVBT);
		tvBW       = (TextView)convertView.findViewById(R.id.tvBW);
		tvCarrier  = (TextView)convertView.findViewById(R.id.tvCarrier);
		tvBWLable  = (TextView)convertView.findViewById(R.id.tvBWLable);
		tvBWColon  = (TextView)convertView.findViewById(R.id.tvBWColon);
		tvJitter   = (TextView)convertView.findViewById(R.id.tvJitter);
		tvWPC  	   = (TextView)convertView.findViewById(R.id.tvWPC);
		tvRB       = (TextView)convertView.findViewById(R.id.tvRB);
		tvSB       = (TextView)convertView.findViewById(R.id.tvSB);
		tvTimeTitle= (TextView)convertView.findViewById(R.id.tvTimeTitle);
		
		if(testResultDo != null)
		{
			String date[] = testResultDo.time.split(" ");
			if(date.length == 3)
			{
				tvTime.setText(date[0]+" "+date[1]);
				//tvTimeTitle.append(" ("+date[2]+")");
				tvTimeTitle.setText(context.getString(R.string.time)+" ("+date[2]+")");
			}
			else
				tvTime.setText(testResultDo.time);
			
			tvFlightId.setText(testResultDo.flightId);
			if(context instanceof TestDataCompareActivity)
			{
				tvPT.setText(((TestDataCompareActivity) context).checkMilis(Double.parseDouble(testResultDo.pingTime)));
				tvUS.setText(((TestDataCompareActivity) context).checkMbps(Double.parseDouble(testResultDo.uploadSpeed)));
				tvDS.setText(((TestDataCompareActivity) context).checkMbps(Double.parseDouble(testResultDo.downloadSpeed)));
				tvWPL.setText(((TestDataCompareActivity) context).checkSecs(Double.parseDouble(testResultDo.webPageLT)));
				tvVBT.setText(((TestDataCompareActivity) context).checkMilis(Double.parseDouble(testResultDo.videoBufTime)));
				tvJitter.setText(((TestDataCompareActivity) context).checkMilis(Double.parseDouble(testResultDo.jitter)));
				tvWPC.setText(testResultDo.webPageCount);
				tvRB.setText(((TestDataCompareActivity) context).checkMbps(Double.parseDouble(testResultDo.recByte)));
				tvSB.setText(((TestDataCompareActivity) context).checkMbps(Double.parseDouble(testResultDo.sendByte)));
				
				String bw[] = testResultDo.bandwidth.split(" ");
				if(bw != null && bw.length >= 2)
				{
					tvBW.setVisibility(View.VISIBLE);
					tvBWLable.setVisibility(View.VISIBLE);
					tvBWColon.setVisibility(View.VISIBLE);
					tvBW.setText(((TestDataCompareActivity) context).getDecimalFormat(Double.parseDouble(bw[0])) + " "+bw[1]);
				}
				if(testResultDo.carrier.length() <= 0)
					testResultDo.carrier = context.getString(R.string.viasat);
				tvCarrier.setText(String.format(context.getString(R.string.carreier), testResultDo.carrier));
			}
		}

		return convertView;
	}

	public void refresh(Vector<TestResultDo> testResultDos)
	{
		this.testResultDos = testResultDos;
		notifyDataSetChanged();
	}
}
