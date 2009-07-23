package org.codeandroid.vpnc_frontend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import android.util.Log;


public class Util
{
	public static final String LOG_TAG = "VPN_Connections";
	
	public static int getProcessId()
	{
		try
		{
			Process psProcess = Runtime.getRuntime().exec( "sh" );
			OutputStream os = psProcess.getOutputStream();
			InputStream is = psProcess.getInputStream();
			writeLine( os, null, "ps | grep 'vpnc$' | cut -c 10-14" );
			writeLine( os, null, "exit" );
			try
			{
				psProcess.waitFor();
			}
			catch( InterruptedException interruptedException )
			{
				Log.e( LOG_TAG, "While trying to read process id", interruptedException );
				return 0;
			}
			String pidString = readString(is, null, false);
			if( pidString == null || pidString.trim().length() == 0 )
			{
				Log.d( LOG_TAG, "Attempt to read vpnc process id did not return anything" );
				return -1;
			}
			else
			{
				pidString = pidString.trim();
				Log.d( LOG_TAG, "Read vpnc process id as " + pidString );
				try
				{
					return Integer.parseInt(pidString);
				}
				catch( NumberFormatException e )
				{
					Log.w( LOG_TAG, "Could not parse process id of " + pidString, e );
					return 0;
				}
			}
		}
		catch( IOException e )
		{
			Log.e( LOG_TAG, "While trying to read process id", e );
			return 0;
		}
	}

	public static String readString( InputStream is, PrintWriter logWriter, boolean block ) throws IOException
	{
		if( !block && is.available() == 0 )
		{
			//Caller doesn't want to wait for data and there isn't any available right now
			return null;
		}
		byte firstByte = (byte)is.read(); //wait till something becomes available
		int available = is.available();
		byte[] characters = new byte[available + 1];
		characters[0] = firstByte;
		is.read( characters, 1, available );
		String string = new String( characters );
		if( logWriter != null )
		{
			logWriter.println(string);
		}
		return string;
	}

	public static void writeLine(OutputStream os, PrintWriter logWriter, String value) throws IOException
	{
		String line = value + "\n";
		os.write( line.getBytes() );
		if( logWriter != null )
		{
			logWriter.println(value);
		}
	}
}
