package com.viasat.etrac.listeners;

public interface StatusListener 
{
	public void testStarted();
	public void testStatusUpdated(String data);
	public void testCompleted();
	public void testCanceled();
}
