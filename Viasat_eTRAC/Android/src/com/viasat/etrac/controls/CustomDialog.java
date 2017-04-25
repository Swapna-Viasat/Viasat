package com.viasat.etrac.controls;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.viasat.etrac.R;

public class CustomDialog extends Dialog {
	@Override
	public void onBackPressed() 
	{
		super.onBackPressed();
//		dismiss();
	}

	public Button okButton,cancelButton;

	public CustomDialog(Context context,int msg) {
		super(context);
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/** Design the dialog in main.xml file */
		setContentView(R.layout.mydialog);
		TextView myText = (TextView)findViewById(R.id.textView3);
		myText.setText(msg);
		okButton = (Button) findViewById(R.id.btnok);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v == okButton)
					dismiss();
			}
		});
	}
	
	public CustomDialog(Context context,int msg, int title) {
		super(context);
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/** Design the dialog in main.xml file */
		setContentView(R.layout.mydialog);
		TextView myTitle = (TextView)findViewById(R.id.textView1);
		myTitle.setText(title);
		TextView myText = (TextView)findViewById(R.id.textView3);
		myText.setText(msg);
		okButton = (Button) findViewById(R.id.btnok);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == okButton)
					dismiss();
			}
		});
	}
	
	public CustomDialog(Context context,String msg, int title) {
		super(context);
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/** Design the dialog in main.xml file */
		setContentView(R.layout.mydialog);
		TextView myTitle = (TextView)findViewById(R.id.textView1);
		myTitle.setText(title);
		TextView myText = (TextView)findViewById(R.id.textView3);
		myText.setText(msg);
		okButton = (Button) findViewById(R.id.btnok);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == okButton)
					dismiss();
			}
		});
	}
	
	public CustomDialog(Context context,int msg, int title,android.view.View.OnClickListener listener) 
	{
		super(context);
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/** Design the dialog in main.xml file */
		setContentView(R.layout.mydialog);
		TextView myTitle = (TextView)findViewById(R.id.textView1);
		myTitle.setText(title);
		TextView myText = (TextView)findViewById(R.id.textView3);
		myText.setText(msg);
		okButton = (Button) findViewById(R.id.btnok);
		cancelButton = (Button)findViewById(R.id.btnCancel);
		cancelButton.setVisibility(View.VISIBLE);
		
		okButton.setOnClickListener(listener);
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == cancelButton)
					dismiss();
			}
		});
	}
	
	public CustomDialog(Context context,String msg, int title,android.view.View.OnClickListener listener,boolean flag) 
	{
		super(context);
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/** Design the dialog in main.xml file */
		setContentView(R.layout.mydialog);
		TextView myTitle = (TextView)findViewById(R.id.textView1);
		myTitle.setText(title);
		TextView myText = (TextView)findViewById(R.id.textView3);
		myText.setText(msg);
		okButton = (Button) findViewById(R.id.btnok);
		cancelButton = (Button)findViewById(R.id.btnCancel);
		if(flag)
			cancelButton.setVisibility(View.VISIBLE);
		
		okButton.setOnClickListener(listener);
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == cancelButton)
					dismiss();
			}
		});
	}
	
	public CustomDialog(Context context)
	{
		super(context);
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/** Design the dialog in main.xml file */
		setContentView(R.layout.internet_check);
	}
	
	public CustomDialog(Context context,View view)
	{
		super(context);
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/** Design the dialog in main.xml file */
		setContentView(view);
	}
}
