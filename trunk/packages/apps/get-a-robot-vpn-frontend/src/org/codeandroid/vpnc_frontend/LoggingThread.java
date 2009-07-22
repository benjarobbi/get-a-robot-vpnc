package org.codeandroid.vpnc_frontend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import android.util.Log;

public class LoggingThread extends Thread
{

	private BufferedReader bufferedReader;
	private PrintWriter logWriter;
	private String tag;
	private boolean quit = false;
	private int priority;

	public LoggingThread(InputStream inputStream, PrintWriter logWriter, String tag, int priority)
	{
		this.tag = tag;
		this.logWriter = logWriter;
		bufferedReader = new BufferedReader( new InputStreamReader( inputStream ) );
		this.priority = priority;
	}

	@Override
	public void run()
	{
		try
		{
			for( String line = bufferedReader.readLine(); line != null && !quit; line = bufferedReader.readLine() )
			{
				Log.println( priority, tag, line );
				if( logWriter != null )
				{
					logWriter.println( tag + "\t" + line );
				}
			}
		}
		catch( IOException e )
		{
			Log.e( tag, e.getMessage(), e );
		}
		finally
		{
			logWriter.flush();
			logWriter.close();
		}
	}

	public void quit()
	{
		quit = true;
	}

}
