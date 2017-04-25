package com.viasat.etrac.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.viasat.etrac.dataobj.LinkDataDo;
import com.viasat.etrac.dataobj.WebLoadDo;
import com.viasat.etrac.listeners.DataListener;
import com.viasat.etrac.utils.WebServiceManager;

public class  GetWebLoadData extends AsyncTask<String, Integer, String>
{
	private DataListener listener;
	private String pref_token;
	public GetWebLoadData(DataListener listener, String pref_token) 
	{
		this.listener = listener;
		this.pref_token = pref_token;
		
	}
	
	@Override
	protected void onPreExecute() 
	{
	}

	@Override
	protected void onPostExecute(String string) 
	{
		if(string == null)
		{
			return;
		}
		try 
		{
			JSONObject jsonObject = new JSONObject(string);
			if(jsonObject != null && jsonObject.getString("response").equalsIgnoreCase("success"))
			{
				JSONObject jsonObject2 = jsonObject.getJSONObject("webPageTimer");
				if(jsonObject2 != null)
				{
					WebLoadDo webLoadDo = new WebLoadDo();
					webLoadDo.timeout = jsonObject2.getString("timeout");
					webLoadDo.numURLs = jsonObject2.getInt("numURLs");
					if(webLoadDo.numURLs > 0)
					{
						Vector<LinkDataDo> vecLinkDataDos = new Vector<LinkDataDo>();
						for (int i = 0; i < webLoadDo.numURLs; i++) 
						{
							LinkDataDo linkDataDo = new LinkDataDo();
							linkDataDo.link = jsonObject2.getString("url"+i);
							
							if(linkDataDo.link.contains("linkedin"))
								linkDataDo.link = "http://www.facebook.com";
							vecLinkDataDos.add(linkDataDo);
						}
						if(vecLinkDataDos.size() > 0)
							webLoadDo.vecLinkDataDos = vecLinkDataDos;
					}

					if(webLoadDo != null)
					{
						listener.dataDownloaded(webLoadDo);
					}
				}
			}
		} 
		catch (JSONException e)
		{
			e.printStackTrace();
		}

	}

	@Override
	protected String doInBackground(String... params) 
	{
		String url = params[0];
		// initilize the default HTTP client object
		//final DefaultHttpClient client = new DefaultHttpClient();
		WebServiceManager webServiceManager = WebServiceManager.getInstance();
		final  HttpClient client = webServiceManager.getNewHttpClient();

		//forming a HttoGet request 
		final HttpGet getRequest = new HttpGet(url);
		getRequest.setHeader("X-Identity-Token",pref_token);
		try 
		{
			HttpResponse response = client.execute(getRequest);

			//check 200 OK for success
			final int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode != HttpStatus.SC_OK)
			{
				return null;
			}

			HttpEntity entity = response.getEntity();
			InputStream inputStream = null;
			if (entity != null)
			{
				try 
				{
					inputStream = entity.getContent();
					if(inputStream != null)
						return convertInputStreamToString(inputStream);
					return null;
				} 
				finally 
				{
					inputStream.close();
					entity.consumeContent();
				}
			}
		} 
		catch (Exception e) 
		{
			// You Could provide a more explicit error message for IOException
			getRequest.abort();
		} 
		return null;
	}
	
	// convert inputstream to String
	private static String convertInputStreamToString(InputStream inputStream) throws IOException
	{
		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while((line = bufferedReader.readLine()) != null)
			result += line;
		return result;
	}

}
