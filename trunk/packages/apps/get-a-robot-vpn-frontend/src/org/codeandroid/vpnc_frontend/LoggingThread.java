package org.codeandroid.vpnc_frontend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;


public class LoggingThread extends Thread
{
	private BufferedReader bufferedReader;
	private String tag;
	private boolean quit = false;

	public LoggingThread(InputStream inputStream, String tag)
	{
		this.tag = tag;
		bufferedReader = new BufferedReader( new InputStreamReader(inputStream) );
	}
	
	@Override
	public void run()
	{
		try
		{
			for( String line = bufferedReader.readLine(); line != null && !quit; line = bufferedReader.readLine() )
			{
				Log.d( tag, line );
			}
		}
		catch( IOException e )
		{
			Log.w( tag, e );
		}
	}
	
	public void quit()
	{
		quit = true;
	}

}
