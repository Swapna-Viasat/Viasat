package com.viasat.etrac.dataobj;

import org.json.JSONException;
import org.json.JSONObject;

public class TestResultDo 
{
	public String time = "";
	public String flightId = "";
	public String provider = "";
	public String pingTime = "";
	public String uploadSpeed = "";
	public String downloadSpeed = "";
	public String webPageLT = "";
	public String videoBufTime = "";
	public String bandwidth = "";
	public String carrier = "";
	public String webPageCount = "";
	public String jitter = "";
	public String recByte = "";
	public String sendByte = "";
	public JSONObject getJSONObject() 
	{
	    JSONObject obj = new JSONObject();
	    try 
	    {
	        obj.put("time", this.time);
	        obj.put("flightId", this.flightId);
	        obj.put("provider", this.provider);
	        obj.put("pingTime", this.pingTime);
	        obj.put("uploadSpeed", this.uploadSpeed);
	        obj.put("downloadSpeed", this.downloadSpeed);
	        obj.put("webPageLT", this.webPageLT);
	        obj.put("videoBufTime", this.videoBufTime);
	        obj.put("bandwidth", this.bandwidth);
	        obj.put("carrier", this.carrier);
	        obj.put("webPageCount", this.webPageCount);
	        obj.put("jitter", this.jitter);
	        obj.put("recByte", this.recByte);
	        obj.put("sendByte", this.sendByte);
	    } 
	    catch (JSONException e)
	    {
	        e.printStackTrace();
	    }
	    return obj;
	}
}
