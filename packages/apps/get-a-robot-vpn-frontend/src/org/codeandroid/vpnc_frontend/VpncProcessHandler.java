package org.codeandroid.vpnc_frontend;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
		try
		{
			getProcessId();
		}
		catch( IOException e )
		{
			Log.e( TAG, "While trying to read process id", e );
		}
	}

	public void connect(final VPNC vpnc, NetworkConnectionInfo info)
	{
		if( vpncProcessId > 0 )
		{
			Log.d( TAG, "Asked to connect but there is a pid for an existing vpnc connection" );
			try
			{
				getProcessId(); //double-check if we're still connected
			}
			catch( IOException e )
			{
				Log.e( TAG, "While trying to read process id", e );
			}
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
			OutputStream os = process.getOutputStream();
			InputStream is = process.getInputStream();
			if( is.available() > 0 )
			{
				Log.d( TAG, readString( is ) );
			}
			writeLine( os, "/data/data/org.codeandroid.vpnc_frontend/files/vpnc --script /data/data/org.codeandroid.vpnc_frontend/files/vpnc-script --no-detach" );

			Log.d( TAG, readString( is ) );
			Log.d( TAG, "IP " + gateway );
			writeLine( os, gateway );
			Log.d( TAG, readString( is ) );
			Log.d( TAG, "group id: " + ipsecId );
			writeLine( os, ipsecId );
			Log.d( TAG, readString( is ) );
			Log.d( TAG, "group pwd " + secret );
			writeLine( os, secret );
			Log.d( TAG, readString( is ) );
			Log.d( TAG, "user " + xauth );
			writeLine( os, xauth );
			Log.d( TAG, readString( is ) );
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
		try
		{
			OutputStream os = process.getOutputStream();
			InputStream is = process.getInputStream();
			char[] maskedPassword = password.toCharArray();
			for( int i = 0; i < maskedPassword.length; i++ )
			{
				maskedPassword[i] = '*';
			}
			Log.d( TAG, "password " + String.valueOf(maskedPassword) );
			writeLine( os, password );
			Log.d( TAG, "done with vpnc" );
			stdoutLogging = new LoggingThread( is, "process stdout", Log.DEBUG );
			stdoutLogging.start();
			stderrLogging = new LoggingThread( process.getErrorStream(), "process stderr", Log.VERBOSE );
			stderrLogging.start();
			getProcessId();
			if( vpncProcessId > 0 )
			{
				vpnc.setConnected(true, info);
				this.connectionInProgress = null;
			}
			else
			{
				vpnc.setConnected(false, info);
				this.connectionInProgress = null;
			}
		}
		catch( IOException e )
		{
			Log.e( TAG, "While reading from / writing to process stream", e );
			vpnc.setConnected(false, info);
			this.connectionInProgress = null;
		}
	}

	public boolean disconnect()
	{
		if( vpncProcessId > 0 )
		{
			Log.d( TAG, "will kill process " + vpncProcessId );
			try
			{
				Process killProcess = Runtime.getRuntime().exec("su -c sh");
				OutputStream os = killProcess.getOutputStream();
				writeLine( os, "kill " + vpncProcessId );
				writeLine( os, "exit" );
				os.close();
				Log.d( TAG, "killProcess exited with exit value of " + killProcess.waitFor() );
				killProcess.destroy();
				vpncProcessId = -1;
				
				if( process != null )
				{
					stderrLogging.quit();
					stdoutLogging.quit();
					process.destroy();
					process = null;
					Log.d( TAG, "process killed" );
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
	
	private void getProcessId() throws IOException
	{
		Process psProcess = Runtime.getRuntime().exec( "sh" );
		OutputStream os = psProcess.getOutputStream();
		InputStream is = psProcess.getInputStream();
		writeLine( os, "ps | grep 'vpnc$' | cut -c 10-13" );
		writeLine( os, "exit" );
		try
		{
			psProcess.waitFor();
		}
		catch( InterruptedException interruptedException )
		{
			Log.e( TAG, "While trying to read process id", interruptedException );
			return;
		}
		int bytesAvailable = is.available();
		if( bytesAvailable == 0 )
		{
			Log.d( TAG, "Attempt to read vpnc process id did not return anything" );
			vpncProcessId = -1;
		}
		else
		{
			String pidString = readString(is).trim();
			Log.d( TAG, "Read vpnc process id as " + pidString );
			if( pidString == null || pidString.length() == 0 )
			{
			}
			else
			{
				try
				{
					vpncProcessId = Integer.parseInt(pidString);
					Log.d( TAG, "Got the pid for vpnc: " + vpncProcessId );
				}
				catch( NumberFormatException e )
				{
					Log.w( TAG, "Could not parse process id of " + pidString, e );
					vpncProcessId = 0;
				}
			}
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

	private static String readString(InputStream is) throws IOException
	{
		byte firstByte = (byte)is.read(); //wait till something becomes available
		int available = is.available();
		byte[] characters = new byte[available + 1];
		characters[0] = firstByte;
		is.read( characters, 1, available );
		return new String( characters );
	}

	private static void writeLine(OutputStream os, String value) throws IOException
	{
		String line = value + "\n";
		os.write( line.getBytes() );
	}
}
