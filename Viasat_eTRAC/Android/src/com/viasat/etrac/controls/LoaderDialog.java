package com.viasat.etrac.controls;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

import com.viasat.etrac.R;

public class LoaderDialog extends Dialog
{
	private Context context;
	private boolean isFinish = false;
	@Override
	public void onBackPressed() 
	{
		super.onBackPressed();
		dismiss();
		if(isFinish)
			((Activity) context).finish();
	}
	
	public void setIsFinishTrue()
	{
		isFinish = true;
	}

	public LoaderDialog(Context context,String msg)
	{
		super(context);
		this.context = context;
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/** Design the dialog in main.xml file */
		setContentView(R.layout.internet_check);
		
		TextView tvMessage = (TextView)findViewById(R.id.tvMessage);
		tvMessage.setText(msg);
	}
}
