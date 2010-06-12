package org.codeandroid.vpnc_frontend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import android.util.Log;

public class Util
{

	private static final String LOG_TAG = "VPN_Connections";
	public static final int DISCONNECT_NOTIFICATION = 1;
	private static final int pidColumn = getPidColumn();

	public static int getPidColumn()
	{
		try
		{
			Process psProcess = Runtime.getRuntime().exec( "sh" );
			OutputStream os = psProcess.getOutputStream();
			InputStream is = psProcess.getInputStream();
			writeLine( os, null, "ps | grep PID" );
			writeLine( os, null, "exit" );
			try
			{
				psProcess.waitFor();
			}
			catch( InterruptedException interruptedException )
			{
				Log.e( LOG_TAG, "While trying to read process id", interruptedException );
				return -1;
			}
			String headerLine = readString( is, null, false );
			Log.d( LOG_TAG, "Read PS header line as " + headerLine );
			if( headerLine == null || headerLine.trim().length() == 0 )
			{
				Log.e( LOG_TAG, "Attempt to do a PS did not return anything" );
				return -1;
			}
			else
			{
				StringTokenizer tokenizer = new StringTokenizer( headerLine, " ", false );
				int columnCount = tokenizer.countTokens();
				for( int index = 0; index < columnCount; index++ )
				{
					if( "PID".equals( tokenizer.nextToken() ) )
					{
						Log.d( LOG_TAG, "PID is in column #" + index );
						return index;
					}
				}
				return -1;
			}
		}
		catch( IOException e )
		{
			Log.e( LOG_TAG, "While trying to read process id", e );
			return -1;
		}
	}

	public static int getProcessId()
	{
		if( pidColumn == -1 )
		{
			return -1;
		}
		try
		{
			Process psProcess = Runtime.getRuntime().exec( "sh" );
			OutputStream os = psProcess.getOutputStream();
			InputStream is = psProcess.getInputStream();
			writeLine( os, null, "ps | grep 'vpnc$'" );
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
			String pidStringLine = readString( is, null, false );
			Log.d( LOG_TAG, "Read vpnc process line as " + pidStringLine );
			if( pidStringLine == null || pidStringLine.trim().length() == 0 )
			{
				Log.d( LOG_TAG, "Attempt to read vpnc process id did not return anything" );
				return -1;
			}
			else
			{
				StringTokenizer tokenizer = new StringTokenizer( pidStringLine, " ", false );
				String pidString = tokenizer.nextToken();
				for( int index = 0; index < pidColumn; index++ )
				{
					pidString = tokenizer.nextToken();
				}
				Log.d( LOG_TAG, "Read vpnc process id as " + pidString );
				try
				{
					return Integer.parseInt( pidString );
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

	public static String readString(InputStream is, PrintWriter logWriter, boolean block) throws IOException
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
			logWriter.println( string );
		}
		return string;
	}

	public static void writeLine(OutputStream os, PrintWriter logWriter, String value) throws IOException
	{
		String line = value + "\n";
		os.write( line.getBytes() );
		if( logWriter != null )
		{
			logWriter.println( value );
		}
	}

	public static void debug(String msg)
	{
		Log.d( LOG_TAG, msg );
	}

	public static void debug(String msg, Throwable throwable)
	{
		Log.d( LOG_TAG, msg, throwable );
	}

	public static void info(String msg)
	{
		Log.i( LOG_TAG, msg );
	}

	public static void info(String msg, Throwable throwable)
	{
		Log.i( LOG_TAG, msg, throwable );
	}

	public static void warn(String msg)
	{
		Log.w( LOG_TAG, msg );
	}

	public static void warn(String msg, Throwable throwable)
	{
		Log.w( LOG_TAG, msg, throwable );
	}

	public static void error(String msg)
	{
		Log.e( LOG_TAG, msg );
	}

	public static void error(String msg, Throwable throwable)
	{
		Log.e( LOG_TAG, msg, throwable );
	}

	public static void printLog(int priority, String msg)
	{
		Log.println( priority, LOG_TAG, msg );
	}
}
