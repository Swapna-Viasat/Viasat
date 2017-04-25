package com.viasat.etrac.controls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.AsyncTask;

import com.viasat.etrac.dataobj.ServerInfoDo;
import com.viasat.etrac.listeners.StatusListener;


//The main class for executing iperf instances.
public class ThroughputTest extends AsyncTask<Void, String, String>
{

	private ServerInfoDo infoDo;
	private StatusListener listener;
	public ThroughputTest(StatusListener listener,ServerInfoDo infoDo)
	{
		this.infoDo = infoDo;
		this.listener = listener;
	}

	Process process = null;

	@Override
	protected void onPreExecute()
	{
		listener.testStarted();
	}

	//This function is used to implement the main task that runs on the background.
	@Override
	protected String doInBackground(Void... voids) 
	{
		//Iperf command syntax check using a Regular expression to protect the system from user exploitation.
		//String str = "-c "+infoDo.ip+" -p "+infoDo.port+" -t "+infoDo.timeout;
		String str = "-c "+infoDo.ip+" -p "+infoDo.port+" -t "+infoDo.timeout + " -u";
		
		try
		{
			//The user input for the parameters is parsed into a string list as required from the ProcessBuilder Class.
			String[] commands = str.split(" ");
			List<String> commandList = new ArrayList<String>(Arrays.asList(commands));
			//If the first parameter is "iperf", it is removed
			if (commandList.get(0).equals((String)"iperf")) 
			{
				commandList.remove(0);
			}
			//The execution command is added first in the list for the shell interface.
			commandList.add(0,"/data/data/com.viasat.etrac/iperf");
			//The process is now being run with the verified parameters.
			process = new ProcessBuilder().command(commandList).redirectErrorStream(true).start();
			//A buffered output of the stdout is being initialized so the iperf output could be displayed on the screen.
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			int read;
			//The output text is accumulated into a string buffer and published to the GUI
			char[] buffer = new char[4096];
			StringBuffer output = new StringBuffer();
			while ((read = reader.read(buffer)) > 0)
			{
				output.append(buffer, 0, read);
				//This is used to pass the output to the thread running the GUI, since this is separate thread.
				publishProgress(output.toString());
				output.delete(0, output.length());
			}
			reader.close();
			process.destroy();
		}
		catch (IOException e) 
		{
			publishProgress("\nError while running the Throughput Test");
			e.printStackTrace();
		}
		return null;
	}

	//This function is called by AsyncTask when publishProgress is called.
	@Override
	public void onProgressUpdate(String... strings)
	{
		listener.testStatusUpdated(strings[0]);
	}

	//This function is called by the AsyncTask class when IperfTask.cancel is called.
	//It is used to terminate an already running task.
	@Override
	public void onCancelled() 
	{
		//The running process is destroyed and system resources are freed.
		if (process != null) 
		{
			process.destroy();
			try 
			{
				process.waitFor();
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}		

		listener.testCanceled();
	}

	@Override
	public void onPostExecute(String result) 
	{
		//The running process is destroyed and system resources are freed.
		if (process != null) 
		{
			process.destroy();

			try
			{
				process.waitFor();
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		listener.testCompleted();
	}

}
