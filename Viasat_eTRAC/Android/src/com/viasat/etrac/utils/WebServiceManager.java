package com.viasat.etrac.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.viasat.etrac.services.MySSLSocketFactory;

import android.widget.Toast;


public class WebServiceManager
{

	//--------------
	static final String HTTP_ACCEPT = "Accept";
	static final String HTTP_AUTHORIZATION = "Authorization";
	static final String HTTP_CONTENT = "Content-type";
	//--------------

	private String ACESS_TOKEN = "authId";
	// ================ Static Classes ================
	public static class SMSAPIConnection
	{
		String mAccessToken;
		
		//
		public SMSAPIConnection(String accessToken)
		{
			mAccessToken= accessToken;
			
		}
		public String getAccessToken()
		{
			return mAccessToken;
		}
		
	}
	// ================ Static Fields ================
	static WebServiceManager sInstance= new WebServiceManager();


	//user credentials
	static final String SMSAPI_USERNAME= "etrac_client";
	static final String SMSAPI_USERPASS= "ioq6Q6YWY1WAy33d";

	
	public static WebServiceManager getInstance()
	{
		return sInstance;
	}

	public boolean isJSONValid(String testMe) {
		try {
			new JSONObject(testMe);
		} catch (JSONException ex) {
			try {
				new JSONArray(testMe);
			} catch (JSONException ex1) {
				return false;
			}
		}
		return true;
	}


	


	public SMSAPIConnection getAuthID(String url)
	{
		// Create a new HttpClient and Post Header
		HttpClient client = getNewHttpClient();
   
		HttpPost post = new HttpPost(url);
		HttpResponse resp = null;
		String jsonString= "";
		try {
			/*----POST data to PHP Server---*/
			post.setHeader("Content-type", "application/json");
			post.setHeader("Accept", "application/json");
			post.setHeader("X-OpenAM-Username", "etrac_client");
			post.setHeader("X-OpenAM-Password", "ioq6Q6YWY1WAy33d");
            
			resp = client.execute(post);

			
			int retCode = resp.getStatusLine().getStatusCode();

			if((retCode >= HttpStatus.SC_OK) && (retCode < HttpStatus.SC_MULTIPLE_CHOICES)) {
				if(resp == null)
				{
					return null;
				}
				jsonString= getStringContent(resp);
				JSONObject jsonObject= new JSONObject(jsonString);

				SMSAPIConnection smsapiConnection= new SMSAPIConnection(
						jsonObject.getString(ACESS_TOKEN));
				return smsapiConnection;
			}else if(retCode == HttpStatus.SC_FORBIDDEN){
				resp = client.execute(post);
			}
				
			}catch (JSONException e)
			{
				return null;
			}
			catch (UnsupportedEncodingException e)
			{
				return null;
			}catch (ClientProtocolException e) {
				return null;
			}
			catch (IOException e) {
				return null;
			}
		return null;
		}
	
	

	
	public HttpClient getNewHttpClient() 
	{
	    try
	    {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	        return new DefaultHttpClient(ccm, params);
	    }
	    catch (Exception e) 
	    {
	        return new DefaultHttpClient();
	    }
	}
	
	public String getStringContent(HttpResponse response)
	{
		BufferedReader reader = null;
		StringBuilder builder = new StringBuilder();
		try {
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}
			return builder.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	public boolean doHttpPost(SMSAPIConnection smsapiConnection, String url)
	{
		HttpPost post = new HttpPost(url);

		try {

		if(smsapiConnection != null)
		{
			post.setHeader(HTTP_CONTENT, "application/json");
			post.setHeader(HTTP_ACCEPT,"application/json");
			post.setHeader("X-Identity-Token",smsapiConnection.getAccessToken());
		
		}
			 HttpClient client = getNewHttpClient();
		     HttpResponse resp;
			
				resp = client.execute(post);
			
		     int retCode = resp.getStatusLine().getStatusCode();
		     if(retCode == HttpStatus.SC_OK)
				{
					LogUtils.info("Test","Passed: PHP Server Response Code: " + retCode);
					return true;
				}
				else
				{
					LogUtils.info("Test","Failed: PHP Server Response Code: " + retCode);
					return false;
				}
		   
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return false;
		
	}

	public HttpResponse doHttpGet(SMSAPIConnection smsapiConnection, String smsQuery)
	{
		if(smsapiConnection == null)
			return null;
		HttpClient client = getNewHttpClient();
		String url = "";


		HttpGet getRequest = new HttpGet(smsQuery);

		getRequest.setHeader("X-Identity-Token",smsapiConnection.getAccessToken());
	//	String a = myData.getToken();
	//	getRequest.setHeader("X-Identity-Token",a);

		try
		{
			HttpResponse response = client.execute(getRequest);

			int retCode = response.getStatusLine().getStatusCode();

			if((retCode >= HttpStatus.SC_OK) && (retCode < HttpStatus.SC_MULTIPLE_CHOICES))
			{
				
				return response;
			
			}
		}
		catch (IOException e)
		{
			return null;
		}
		
		return null;
	}

	public String getTermsDataFromService(String token,String smsQuery)
	{
		HttpResponse response = doHttpGet_(token,smsQuery);
		String result;
		try {
			result = EntityUtils.toString(response.getEntity());
			JSONObject object = (JSONObject) new JSONTokener(result).nextValue();

			if(isJSONValid(object.toString()) == false)
				return null;

			JSONObject sys  = object.getJSONObject("termsOfService");
			String res_ = sys.getString("value");
			res_="<html><body>"+res_.replace("\n\n", "<br><br/>")+"</body></html>";
			return res_;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	
	public String getHelpDataFromService(String url,String smsQuery)
	{
		HttpResponse response = doHttpGet_(url,smsQuery);
		String result;
		try {
			result = EntityUtils.toString(response.getEntity());
			JSONObject object = (JSONObject) new JSONTokener(result).nextValue();

			if(isJSONValid(object.toString()) == false)
				return null;

			JSONObject sys  = object.getJSONObject("help");
			String res_ = sys.getString("value");
			res_="<html><body>"+res_.replace("\n\n", "<br><br/>")+"</body></html>";
			return res_;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	public boolean doHttpPost(String token, String url)
	{
		HttpPost post = new HttpPost(url);

		try {

		
			post.setHeader(HTTP_CONTENT, "application/json");
			post.setHeader(HTTP_ACCEPT,"application/json");
			post.setHeader("X-Identity-Token",token);
		
			 HttpClient client = getNewHttpClient();
		     HttpResponse resp;
			
				resp = client.execute(post);
			
		     int retCode = resp.getStatusLine().getStatusCode();
		     if(retCode == HttpStatus.SC_OK)
				{
					LogUtils.info("Test","Passed: PHP Server Response Code: " + retCode);
					return true;
				}
				else
				{
					LogUtils.info("Test","Failed: PHP Server Response Code: " + retCode);
					return false;
				}
		   
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return false;
		
	}
	
	public HttpResponse doHttpGet_(String token, String smsQuery)
	{
		
		HttpClient client = getNewHttpClient();
		String url = "";


		HttpGet getRequest = new HttpGet(smsQuery);

		getRequest.setHeader("X-Identity-Token",token);
	//	String a = myData.getToken();
	//	getRequest.setHeader("X-Identity-Token",a);

		try
		{
			HttpResponse response = client.execute(getRequest);

			int retCode = response.getStatusLine().getStatusCode();

			if((retCode >= HttpStatus.SC_OK) && (retCode < HttpStatus.SC_MULTIPLE_CHOICES))
			{
				
				return response;
			
			}
		}
		catch (IOException e)
		{
			return null;
		}
		
		return null;
	}
	public String getVersionFromService(String token,String smsQuery)
	{
		HttpResponse response = doHttpGet_(token,smsQuery);
		String result;
		try {
			result = EntityUtils.toString(response.getEntity());
			JSONObject object = (JSONObject) new JSONTokener(result).nextValue();

			if(isJSONValid(object.toString()) == false)
				return null;

			JSONObject sys  = object.getJSONObject("version");
			String res_ = sys.getString("string");
			return res_;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	
}
