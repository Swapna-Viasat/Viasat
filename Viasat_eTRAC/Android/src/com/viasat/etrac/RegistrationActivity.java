package com.viasat.etrac;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.viasat.etrac.common.ShareConstants;
import com.viasat.etrac.controls.CustomDialog;
import com.viasat.etrac.dataobj.RouterDo;
import com.viasat.etrac.utils.CalenderUtils;
import com.viasat.etrac.utils.LocationUtils;
import com.viasat.etrac.utils.NetworkUtils;
import com.viasat.etrac.utils.WebServiceManager;
import com.viasat.etrac.utils.eTracUtils;

public class RegistrationActivity extends Activity implements OnClickListener
{
	private TextView tvTitle,etProvider,etService;
	private EditText etEmail,etFlightId,etComments;
	private Button btnSubmit,btnClearAll;
	private String[] providers = {"Exede in the Air", "Gogo" , "Panasonic" , "Row44" , "Anonymous" , "Others"};
	private String[] services  = {"Free", "Paid"};
	private String provider = "" , service = "" ,email = "" , flightid = "" , comments = "";
	private SharedPreferences prefs;
	private String defProvider = "Anonymous";
	private String defService = "Free";
	Thread work;
	private CustomDialog loader , wifiAlert;
	eTracUtils myData;
	private PopupWindow popupWindow;
	private int width;
	//	private ScrollView svLayout;
	boolean isHas = false;
	private ImageView ivDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);

		myData = (eTracUtils) getApplication();

		tvTitle      = (TextView)findViewById(R.id.tvTitle);
		etEmail      = (EditText)findViewById(R.id.etEmail);
		etFlightId   = (EditText)findViewById(R.id.etFlightId);
		etComments   = (EditText)findViewById(R.id.etComments);
		etProvider	 = (TextView)findViewById(R.id.etProvider);
		etService	 = (TextView)findViewById(R.id.etService);
		btnSubmit	 = (Button)findViewById(R.id.btnSubmit);
		btnClearAll	 = (Button)findViewById(R.id.btnClearAll);
		ivDatabase	   = (ImageView)findViewById(R.id.ivDatabase);
		ivDatabase.setVisibility(View.VISIBLE);
		tvTitle.setText(R.string.registration);
		btnSubmit.setOnClickListener(this);
		btnClearAll.setOnClickListener(this);
		etProvider.setOnClickListener(this);
		etService.setOnClickListener(this);
		ivDatabase.setOnClickListener(this);
		setData();
	}

	@Override
	public void onClick(final View v) 
	{
		switch (v.getId()) 
		{
		case R.id.btnSubmit:
			toggleBtn((Button) v);
			validate();	
			break;

		case R.id.btnClearAll:
			clearAllFields();
			break;
		case R.id.etProvider:
			new Handler().postDelayed(new Runnable()
			{

				@Override
				public void run()
				{
					width = etProvider.getWidth();
					popupWindow = CustomPopupWindow(providers, (TextView) v);
					popupWindow.showAsDropDown(v, -5, 0);
				}
			}, 100);
			break;
		case R.id.etService:
			new Handler().postDelayed(new Runnable()
			{

				@Override
				public void run()
				{
					width = etProvider.getWidth();
					popupWindow = CustomPopupWindow(services, (TextView) v);
					popupWindow.showAsDropDown(v, -5, 0);
				}
			}, 100);
			break;
		case R.id.ivDatabase:
			provider = etProvider.getText().toString();
			prefs.edit().putString(ShareConstants.SP_PROVIDER, provider).commit();
			Intent intent = new Intent(RegistrationActivity.this, TestDataCompareActivity.class);
			intent.putExtra("activity","true");
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			finish();
			break;
		}


	}

	private void validate()
	{
		email 	 = etEmail.getText().toString();
		flightid = etFlightId.getText().toString();
		comments = etComments.getText().toString();
		provider = etProvider.getText().toString();
		service  = etService.getText().toString();
		int msg = -1;

		if(!provider.equalsIgnoreCase(defProvider))
		{
			if(!(email.trim().length() > 0))
				msg = R.string.fill_all;
		}
		if(!(flightid.trim().length() > 0)
				|| !(provider.trim().length() > 0)
				|| !(service.trim().length() > 0))
			msg = R.string.fill_all;
		else if(!provider.equalsIgnoreCase(defProvider) && !isValidEmail(email))
			msg = R.string.valid_email;
		else if(!isValidflightId(flightid))
			msg = R.string.valid_flightid;

		if(msg != -1)
		{
			CustomDialog customDialog = new CustomDialog(RegistrationActivity.this, msg, R.string.dialog_title);
			customDialog.setCancelable(false);
			customDialog.show();
			toggleBtn(btnSubmit);
		}
		else
		{
			loader = new CustomDialog(RegistrationActivity.this);
			loader.setCancelable(false);
			loader.show();
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					takeAction();
				}
			};

			work = new Thread(runnable);
			work.start();		
		}

	}

	public void takeAction()
	{
		int ret = NetworkUtils.isNetworkAvailable(RegistrationActivity.this);
		if(ret != 0)
			ret = NetworkUtils.isNetworkAvailable(RegistrationActivity.this);

		if( ret == 0) //Internet Available
		{
			saveData();
			//Sumanta : 14-11-2014
			updateFlightDetailsServerDB();
			moveToNext();
			toggleBtn(btnSubmit);
		}
		else
		{
			showNetAlertDialog(ret);
		}
	}

	public void showNetAlertDialog(final int value)
	{
		runOnUiThread(new Runnable() {
			public void run() {
				loader.dismiss();
				showAlert(value);
				toggleBtn(btnSubmit);
			}
		});
	}


	private void moveToNext() 
	{
		loader.dismiss();
		Intent intent = new Intent(RegistrationActivity.this, TestMenuActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	private void updateFlightDetailsServerDB() 
	{
		String comand 	 = "updateFlight";
		String macAdd 	 = "";
		String ssid   	 = "";
		String token  	 = "0";
		String device  = "Android";

		RouterDo routerInfo = NetworkUtils.routerInfo(RegistrationActivity.this);
		if(routerInfo != null)
		{
			macAdd  = routerInfo.macAdd;
			ssid 	= routerInfo.ssid;
			myData.setRouterDetails(routerInfo);
		}
		WebServiceManager webServiceManager = WebServiceManager.getInstance();
		String pref_token = prefs.getString(ShareConstants.SP_TOKEN, "");
		String url;
		try {
			url = myData.getServerUrl()+URLEncoder.encode(comand, "UTF-8")+"&flightID="+URLEncoder.encode(flightid, "UTF-8")+"&email="+URLEncoder.encode(email, "UTF-8")+"&serviceName="+URLEncoder.encode(service, "UTF-8")+"&mac="+URLEncoder.encode(macAdd, "UTF-8")+"&device="+URLEncoder.encode(device, "UTF-8")+"&token="+URLEncoder.encode(pref_token, "UTF-8")+"&providerName="+URLEncoder.encode(provider, "UTF-8")+"&ssid="+URLEncoder.encode(ssid, "UTF-8")+"&timeStamp="+URLEncoder.encode(CalenderUtils.getCurrentTimeStamp(), "UTF-8");
		
		 webServiceManager.doHttpPost(pref_token, url);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
/*		List<NameValuePair> pairs = new ArrayList<NameValuePair>(10);
		pairs.add(new BasicNameValuePair("cmd", comand));
		pairs.add(new BasicNameValuePair("email", email));
		pairs.add(new BasicNameValuePair("flightID", flightid));
		pairs.add(new BasicNameValuePair("providerName", provider));
		pairs.add(new BasicNameValuePair("serviceName", service));
		pairs.add(new BasicNameValuePair("ssid", ssid));
		pairs.add(new BasicNameValuePair("mac", macAdd));
		pairs.add(new BasicNameValuePair("device", device));
		pairs.add(new BasicNameValuePair("token", token));
		pairs.add(new BasicNameValuePair("timeStamp", CalenderUtils.getCurrentTimeStamp()));		


		// Create a new HttpClient and Post Header
		HttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		HttpPost post = new HttpPost(myData.getServerName());
*/

		/*try {
			----POST data to PHP Server---

			//Encode data before posting
			UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(pairs);

			//Set the POST properties
			post.setEntity(uefe);


			//----------------------------
			//post.setHeader("Accept", "application/json");
			//post.setHeader("Content-type", "application/json");
			//post.setHeader("X-OpenAM-Username", "etrac_client");
			//post.setHeader("X-OpenAM-Password", "ioq6Q6YWY1WAy33d");
			//----------------------------

			// Execute the HTTP Post Request
			HttpResponse response = client.execute(post);

			// Get Response Code from Server
			int retCode = response.getStatusLine().getStatusCode();

			if(retCode == HttpStatus.SC_OK)
			{
				//System.out.println("eTRAC Passed: PHP Server Response Code: " + retCode);
			}
			else
			{
				//System.out.println("eTRAC Failed: PHP Server Response Code: " + retCode);
			}
		}*/
		
	}

	private void clearAllFields()
	{
		etEmail.setText("");
		etFlightId.setText("");
		etComments.setText("");
		etProvider.setText("");
		etService.setText("");

		prefs.edit().putString(ShareConstants.SP_EMAIL, "").commit();
		prefs.edit().putString(ShareConstants.SP_COMMENTS, "").commit();
		prefs.edit().putString(ShareConstants.SP_FLIGHTID, "").commit();
		prefs.edit().putString(ShareConstants.SP_PROVIDER, defProvider).commit();
		prefs.edit().putString(ShareConstants.SP_SERVICE, defService).commit();
	}

	// validating email id
	private boolean isValidEmail(String email) {
		String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
				+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
	// validating flightId
	private boolean isValidflightId(String flightId)
	{
		boolean hasDigit = false , hasLetter = false;
		for (int i=0; i<flightId.length(); i++) 
		{
			char c = flightId.charAt(i);
			if(!hasDigit && Character.isDigit(c))
				hasDigit =true;
			else if (!hasLetter && Character.isLetter(c))
				hasLetter = true;

			if(hasDigit && hasLetter)
				return true;
		}
		return false;
	}

	private void setData()
	{
		prefs = this.getSharedPreferences(ShareConstants.mainPrefsName, Context.MODE_PRIVATE);
		email    = prefs.getString(ShareConstants.SP_EMAIL, "");
		flightid = prefs.getString(ShareConstants.SP_FLIGHTID, "");
		comments = prefs.getString(ShareConstants.SP_COMMENTS, "");
		provider = prefs.getString(ShareConstants.SP_PROVIDER, defProvider);
		service  = prefs.getString(ShareConstants.SP_SERVICE, defService);

		etEmail.setText(email);
		etFlightId.setText(flightid);
		etComments.setText(comments);
		etProvider.setText(provider);
		etService.setText(service);
	}

	private void saveData()
	{
		if(isValidEmail(email))
			prefs.edit().putString(ShareConstants.SP_EMAIL, email).commit();
		if(comments.trim().length() > 0)
			prefs.edit().putString(ShareConstants.SP_COMMENTS, comments).commit();
		prefs.edit().putString(ShareConstants.SP_FLIGHTID, flightid).commit();
		prefs.edit().putString(ShareConstants.SP_PROVIDER, provider).commit();
		prefs.edit().putString(ShareConstants.SP_SERVICE, service).commit();
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		getLocation();
	}
	private void getLocation()
	{
		LocationUtils locationUtils = new LocationUtils(RegistrationActivity.this);
		if(locationUtils.canGetLocation())
		{
			double latitude = locationUtils.getLatitude();
			double longitude = locationUtils.getLongitude();
			prefs.edit().putString(ShareConstants.SP_LATI, latitude+"").commit();
			prefs.edit().putString(ShareConstants.SP_LONG, longitude+"").commit();
		}
		else
		{
			prefs.edit().putString(ShareConstants.SP_LATI, "").commit();
			prefs.edit().putString(ShareConstants.SP_LONG, "").commit();
			locationUtils.showSettingsAlert();
		}
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
			wifiAlert = new CustomDialog(RegistrationActivity.this, msg, R.string.dialog_title,listener);
		}
		else if(val == 2)
		{
			msg = R.string.internet_error4;
			wifiAlert = new CustomDialog(RegistrationActivity.this, msg, R.string.dialog_title);
		}
		if(val != -1)
		{
			wifiAlert.setCancelable(false);
			wifiAlert.show();
		}
	}

	public PopupWindow CustomPopupWindow(String[] popUpContents, TextView et ) {

		PopupWindow popupWindow = new PopupWindow(this);

		ListView listView = new ListView(this);
		listView.setAdapter(popupAdapter(popUpContents));
		listView.setOnItemClickListener(new DropdownOnItemClickListener(et));

		popupWindow.setFocusable(true);
		popupWindow.setWidth(width);
		popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popupWindow.setContentView(listView);

		return popupWindow;
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
				TextView listItem = new TextView(RegistrationActivity.this);
				listItem.setText(item);
				listItem.setTextSize(16);
				listItem.setPadding(10, 10, 10, 10);
				listItem.setBackgroundColor(Color.WHITE);
				listItem.setTextColor(Color.BLACK);
				return listItem;
			}
		};

		return adapter;
	}

	public class DropdownOnItemClickListener implements OnItemClickListener
	{
		private TextView et;
		public DropdownOnItemClickListener(TextView et)
		{
			this.et = et;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) 
		{
			popupWindow.dismiss();

			String selectedItemText = ((TextView) v).getText().toString();
			et.setText(selectedItemText);
		}

	}
	
	private void toggleBtn(Button btn)
	{
		btn.setClickable(!btn.isClickable());
	}
}
