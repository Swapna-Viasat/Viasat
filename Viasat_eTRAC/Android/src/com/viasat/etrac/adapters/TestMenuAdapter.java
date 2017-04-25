package com.viasat.etrac.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.viasat.etrac.R;
import com.viasat.etrac.listeners.ItemClickListener;

public class TestMenuAdapter extends BaseAdapter
{
	private Context context;
	private String[] testMenu;
	private int icons[] = {R.drawable.run_all_test , R.drawable.speed_test , R.drawable.web_page_loader , R.drawable.send_byte_count , R.drawable.through_put, R.drawable.video_load_timer , R.drawable.test_result , R.drawable.router_details};
	private boolean isClickable = true;
	public TestMenuAdapter(Context context, String[] testMenu) 
	{
		this.context = context;
		this.testMenu = testMenu;
	}

	@Override
	public int getCount() 
	{
		if(testMenu != null && testMenu.length > 0)
			return testMenu.length;
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
		String str = testMenu[position];
		if(convertView == null)
			convertView = LayoutInflater.from(context).inflate(R.layout.testmenu_cell, null);

		TextView tvTest  = (TextView)convertView.findViewById(R.id.tvTest);
		ImageView ivIcon = (ImageView)convertView.findViewById(R.id.ivIcon);
		tvTest.setText(str);
		ivIcon.setBackgroundResource(icons[position]);
		convertView.setTag(position);
		convertView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				int pos = (Integer) v.getTag();
				//Sumanta: Commented 14-11-2014
				if(context instanceof ItemClickListener && isClickable)
				{
					changeClickState();
					((ItemClickListener) context).itemClicked(pos);
				}
//				if(pos == 0 || pos == 2 || pos == 5)
//				{
//					Toast toast = Toast.makeText(context, testMenu[pos], Toast.LENGTH_LONG);
//					toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//					toast.show();
//				}
			}

		});

		return convertView;
	}
	public void changeClickState() 
	{
		isClickable = !isClickable;
	}
}
