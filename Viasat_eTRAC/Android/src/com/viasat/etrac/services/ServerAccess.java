package com.viasat.etrac.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

import android.os.AsyncTask;

public class ServerAccess extends AsyncTask<String, Integer, Boolean>
{
	private String serverUrl = "";
	private List<NameValuePair> pairs ;
	public ServerAccess(List<NameValuePair> pairs, String url) 
	{
		this.serverUrl = url;
		this.pairs = pairs;
	}
	protected void onProgressUpdate() {
		//called when the background task makes any progress
	}

	protected void onPreExecute() {
		//called before doInBackground() is started
	}

	protected void onPostExecute(Boolean flag) {
		//called after doInBackground() has finished
	}

	@Override
	protected Boolean doInBackground(String... params) {
		boolean ret = false;
		// Create a new HttpClient and Post Header
		HttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		HttpPost post = new HttpPost(serverUrl);

		try {
			/*----POST data to PHP Server---*/

			//Encode data before posting
			UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(pairs);

			//Set the POST properties
			post.setEntity(uefe);

			// Execute the HTTP Post Request
			HttpResponse response = client.execute(post);

			// Get Response Code from Server
			int retCode = response.getStatusLine().getStatusCode();

			if(retCode == HttpStatus.SC_OK)
			{
				//System.out.println("Test me: OK");
				return true;
			}
			else
			{
				//System.out.println("Test me: FAIL:" + retCode);
				return false;
			}
		}
		catch (UnsupportedEncodingException uee) {			
			//uee.printStackTrace();
		} catch (ClientProtocolException cpe) {
			//cpe.printStackTrace();
		} catch (IOException ioe) {
			//ioe.printStackTrace();
		}

		return ret;
	}
}
