package com.viasat.etrac.adapters;

import java.util.Vector;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.viasat.etrac.R;
import com.viasat.etrac.WebPageLoadTimerActivity;
import com.viasat.etrac.dataobj.LinkDataDo;

public class UrlLoadAdapter extends BaseAdapter
{
	private Context context;
	private Vector<LinkDataDo> vecLinkDataDos;
	private int pos = -1;
	private boolean isLoad = true;
	
	public UrlLoadAdapter(Context context , Vector<LinkDataDo> vecLinkDataDos)
	{
		this.context = context;
		this.vecLinkDataDos = vecLinkDataDos;
	}
	
	public int getCount() 
	{
		if(vecLinkDataDos != null && vecLinkDataDos.size() > 0)
			return vecLinkDataDos.size();
		return 0;
	}

	public Object getItem(int position) 
	{
		return position;
	}

	public long getItemId(int position) 
	{
		return position;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		LinkDataDo linkDataDo = vecLinkDataDos.get(position);
		final ViewHolder viewHolder;
		if(convertView == null)
		{
			convertView = (LinearLayout)LayoutInflater.from(context).inflate(R.layout.list_cell, null);
			viewHolder = new ViewHolder();
			viewHolder.tvLink     	 = (TextView)convertView.findViewById(R.id.tvLink);
			viewHolder.tvLoadTime 	 = (TextView)convertView.findViewById(R.id.tvLoadTime);
			viewHolder.progressBar1 = (ProgressBar)convertView.findViewById(R.id.progressBar1);
			viewHolder.ivLoaded 		 = (ImageView)convertView.findViewById(R.id.ivLoaded);
			convertView.setTag(viewHolder);
		}
		else
			viewHolder = (ViewHolder) convertView.getTag();

		viewHolder.tvLink.setText(linkDataDo.link);
		viewHolder.tvLoadTime.setText(String.valueOf(linkDataDo.loadTime)+" sec");

		viewHolder.tvLink.setTag(position);

		if(position == pos)
		{
			convertView.setBackgroundColor(Color.GREEN);
			viewHolder.progressBar1.setVisibility(View.VISIBLE);
			viewHolder.ivLoaded.setVisibility(View.GONE);
		}
		else
		{
			convertView.setBackgroundColor(Color.WHITE);
			viewHolder.ivLoaded.setVisibility(View.VISIBLE);
			viewHolder.progressBar1.setVisibility(View.INVISIBLE);
			if(linkDataDo.isTryLoadDone)
			{
				if(linkDataDo.isLoaded)
					viewHolder.ivLoaded.setImageResource(R.drawable.check);
				else
					viewHolder.ivLoaded.setImageResource(R.drawable.no);
			}
			else
				viewHolder.ivLoaded.setImageResource(R.drawable.uncheck);

		}

		return convertView;
	}

	private class ViewHolder 
	{
		TextView tvLink ,tvLoadTime;
		ProgressBar progressBar1;
		ImageView ivLoaded;
	}

	public void loadViews(int posn)
	{
		pos = posn;
		notifyDataSetChanged();
		if(pos > -1)
		{
			((WebPageLoadTimerActivity)context).loadWebView(pos,vecLinkDataDos.get(pos).link);
		}
	}

	public void refreshView(int posn , long val)
	{
		if(posn > -1)
		{
			LinkDataDo linkDataDo = vecLinkDataDos.get(posn);
			linkDataDo.loadTime   = val / 1000.0000;
			if(val > 0)
				linkDataDo.isLoaded = true;
			linkDataDo.isTryLoadDone = true;
			
			if(isLoad)
			{
				if(posn < vecLinkDataDos.size() - 1)
				{					
					loadViews(posn+1);
				}
				else
				{
					loadViews(-1);
					((WebPageLoadTimerActivity)context).loadingCompleted(vecLinkDataDos);
				}
			}
			else
			{
				
			}

		}
	}
	
	public Vector<LinkDataDo> getLoadData()
	{
		isLoad = false;
		if(vecLinkDataDos != null && vecLinkDataDos.size() > 0)
			return vecLinkDataDos;
		return null;
	}
}
