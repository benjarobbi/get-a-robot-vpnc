package org.codeandroid.vpnc_frontend;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import android.content.Context;
import android.util.Log;


public class VpncProcessHandler
{
	private static final String TAG = "vpnc service";
	
	private Process process;
	private LoggingThread stdoutLogging;
	private LoggingThread stderrLogging;
	private int vpncProcessId;
	private NetworkConnectionInfo connectionInProgress;
	
	public VpncProcessHandler()
	{
		Log.d( TAG, "VPNC_Service instantiated" );
		getProcessId();
	}

	public void connect(final VPNC vpnc, NetworkConnectionInfo info)
	{
		if( vpncProcessId > 0 )
		{
			Log.d( TAG, "Asked to connect but there is a pid for an existing vpnc connection" );
			getProcessId(); //double-check if we're still connected
			if( vpncProcessId > 0 )
			{
				Log.w( TAG, "Asked to connect but there is a pid for an existing vpnc connection" );
				vpnc.setConnected( false, info );
			}
		}
		try
		{
			this.connectionInProgress = info;
			String gateway = info.getIpSecGateway();
			String ipsecId = info.getIpSecId();
			String secret = info.getIpSecSecret();
			String password = info.getPassword();
			String xauth = info.getXauth();
			process = Runtime.getRuntime().exec("su -c sh");
			if( !new File("/dev/net/tun").exists() )
			{
				Log.d( TAG, "tun does not exist" );
				vpnc.setConnected( false, info );
				this.connectionInProgress = null;
			}
			
			PrintWriter logWriter = new PrintWriter( vpnc.openFileOutput( "lastConnection.log", Context.MODE_PRIVATE ) );
			OutputStream os = process.getOutputStream();
			InputStream is = process.getInputStream();
			if( is.available() > 0 )
			{
				Log.d( TAG, readString( is, logWriter, true ) );
			}
			writeLine( os, logWriter, "/data/data/org.codeandroid.vpnc_frontend/files/vpnc --script /data/data/org.codeandroid.vpnc_frontend/files/vpnc-script --no-detach" );

			Log.d( TAG, readString( is, logWriter, true ) );
			Log.d( TAG, "IP " + gateway );
			writeLine( os, logWriter, gateway );
			Log.d( TAG, readString( is, logWriter, true ) );
			Log.d( TAG, "group id: " + ipsecId );
			writeLine( os, logWriter, ipsecId );
			Log.d( TAG, readString( is, logWriter, true ) );
			Log.d( TAG, "group pwd " + secret );
			writeLine( os, logWriter, secret );
			Log.d( TAG, readString( is, logWriter, true ) );
			Log.d( TAG, "user " + xauth );
			writeLine( os, logWriter, xauth );
			Log.d( TAG, readString( is, logWriter, true ) );
			logWriter.flush();
			logWriter.close();
			if( password == null || password.length() == 0 )
			{
				Runnable uiTask = new Runnable()
				{
					public void run()
					{
						vpnc.getPassword();
					}
				};
				vpnc.getHandler().post( uiTask );
			}
			else
			{
				continueConnection(vpnc, info.getPassword());
			}
		}
		catch( IOException e )
		{
			Log.e( TAG, "While reading from / writing to process stream", e );
			vpnc.setConnected( false, info );
			this.connectionInProgress = null;
		}
	}

	public void continueConnection(VPNC vpnc, String password)
	{
		NetworkConnectionInfo info = this.connectionInProgress;
		PrintWriter logWriter = null;
		try
		{
			logWriter = new PrintWriter( vpnc.openFileOutput( "lastConnection.log", Context.MODE_APPEND ) );
			OutputStream os = process.getOutputStream();
			InputStream is = process.getInputStream();
			InputStream es = process.getErrorStream();
			char[] maskedPassword = password.toCharArray();
			for( int i = 0; i < maskedPassword.length; i++ )
			{
				maskedPassword[i] = '*';
			}
			Log.d( TAG, "password " + String.valueOf(maskedPassword) );
			logWriter.println(maskedPassword);
			writeLine( os, null, password );
			logWriter.flush();
			Log.d( TAG, "done interacting with vpnc" );
			
			//Now wait for connection confirmation, process death or timeout:
			long idleTimeout = 30000;
			long idleInterval = 500;
			String connectString = "vpnc-script ran to completion";
			while( idleTimeout > 0 )
			{
				String stdinString = readString( is, logWriter, false );
				if( stdinString != null )
				{
					Log.d( "process stdout", stdinString );
					logWriter.println( "process stdout" + "\t" + stdinString );
				}
				String stderrString = readString( es, logWriter, false );
				if( stderrString != null )
				{
					Log.d( "process stderr", stderrString );
					logWriter.println( "process stderr" + "\t" + stderrString );
				}
				String concatenation = stdinString  + " - " + stderrString;
				if( concatenation.contains(connectString) )
				{
					String msg = "Connect string detected!";
					Log.d( TAG, msg );
					logWriter.println(msg);
					vpnc.setConnected(true, info);
					this.connectionInProgress = null;
					stdoutLogging = new LoggingThread( is, logWriter, "process stdout", Log.DEBUG );
					stdoutLogging.start();
					stderrLogging = new LoggingThread( process.getErrorStream(), logWriter, "process stderr", Log.VERBOSE );
					stderrLogging.start();
					return;
				}
				else
				{
					getProcessId();
					if( vpncProcessId == -1 )
					{
						String msg = "process had died, return as failed connection";
						Log.d( TAG, msg );
						logWriter.println(msg);
						vpnc.setConnected(false, info);
						this.connectionInProgress = null;
						return;
					}
					else
					{
						//will keep waiting
						try
						{
							String msg = "vpnc still trying to connect. Will check again in " + idleInterval + " milliseconds";
							Log.d( TAG, msg );
							logWriter.println(msg);
							Thread.sleep(idleInterval);
						}
						catch( InterruptedException e )
						{
							Log.e( TAG, "While waiting to get vpnc process status", e );
						}
						idleTimeout -= idleInterval;
					}
				}
			}
			//reaching this point means we've timed out, kill process
			getProcessId();
			if( vpncProcessId > 0 )
			{
				String msg = "vpnc still trying to connect but we've timed out. Will try to kill it and return now.";
				Log.d( TAG, msg );
				logWriter.println(msg);
				disconnect();
				vpnc.setConnected(false, info);
			}
		}
		catch( IOException e )
		{
			Log.e( TAG, "While reading from / writing to process stream", e );
			vpnc.setConnected(false, info);
			this.connectionInProgress = null;
		}
		finally
		{
			if( logWriter != null )
			{
				logWriter.flush();
				logWriter.close();
			}
				
		}
	}

	public boolean disconnect()
	{
		if( vpncProcessId == -1 )
		{
			getProcessId();
		}
		if( vpncProcessId > 0 )
		{
			Log.d( TAG, "will kill process " + vpncProcessId );
			try
			{
				Process killProcess = Runtime.getRuntime().exec("su -c sh");
				OutputStream os = killProcess.getOutputStream();
				writeLine( os, null, "kill " + vpncProcessId );
				writeLine( os, null, "exit" );
				os.close();
				Log.d( TAG, "killProcess exited with exit value of " + killProcess.waitFor() );
				killProcess.destroy();
				vpncProcessId = -1;
				
				if( process != null )
				{
					process.destroy();
					process = null;
					Log.d( TAG, "process killed" );
				}
				if( stderrLogging != null )
				{
					stderrLogging.quit();
					stdoutLogging.quit();
				}
			}
			catch( IOException e )
			{
				Log.e( TAG, e.getMessage(), e );
			}
			catch( InterruptedException e )
			{
				Log.e( TAG, e.getMessage(), e );
			}
			return true;
		}
		else
		{
			Log.d( TAG, "Will ignore disconnect call, no vpnc pid stored" );
			return false;
		}
	}
	
	private void getProcessId()
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
				Log.e( TAG, "While trying to read process id", interruptedException );
				vpncProcessId = 0;
				return;
			}
			String pidString = readString(is, null, false);
			if( pidString == null || pidString.trim().length() == 0 )
			{
				Log.d( TAG, "Attempt to read vpnc process id did not return anything" );
				vpncProcessId = -1;
			}
			else
			{
				pidString = pidString.trim();
				Log.d( TAG, "Read vpnc process id as " + pidString );
				try
				{
					vpncProcessId = Integer.parseInt(pidString);
				}
				catch( NumberFormatException e )
				{
					Log.w( TAG, "Could not parse process id of " + pidString, e );
					vpncProcessId = 0;
				}
			}
		}
		catch( IOException e )
		{
			Log.e( TAG, "While trying to read process id", e );
		}
	}
	
	public boolean isConnected()
	{
		if( vpncProcessId > 0 )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private static String readString( InputStream is, PrintWriter logWriter, boolean block ) throws IOException
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

	private static void writeLine(OutputStream os, PrintWriter logWriter, String value) throws IOException
	{
		String line = value + "\n";
		os.write( line.getBytes() );
		if( logWriter != null )
		{
			logWriter.println(value);
		}
	}
}
