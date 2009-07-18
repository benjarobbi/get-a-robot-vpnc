package org.codeandroid.vpnc_frontend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class VPNC_Service extends Service
{

	private IVPNC_Service.Stub service = getService();
	private Process process;
	private LoggingThread stdoutLogging;
	private LoggingThread stderrLogging;

	@Override
	public IBinder onBind(Intent intent)
	{
		if( IVPNC_Service.class.getName().equals( intent.getAction() ) )
		{
			Log.i( "vpnc service", "Service created and returned" );
			return service;
		}
		else
		{
			return null;
		}
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		System.out.println( "Service created" );
		try
		{
			process = Runtime.getRuntime().exec( "/system/bin/su" );
		}
		catch( IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	public void onDestroy()
	{
		System.out.println( "Service destroyed" );
		process.destroy();
		process = null;
		super.onDestroy();
	}

	private IVPNC_Service.Stub getService()
	{
		return new IVPNC_Service.Stub()
		{

			public boolean connect(String gateway, String id, String secret, String xauth, String password) throws RemoteException
			{
				Log.w( "vpnc service", "Got called" );
				try
				{
					OutputStream os = process.getOutputStream();
					InputStream is = process.getInputStream();
					char[] maskedPassword = password.toCharArray();
					for( int i = 0; i < maskedPassword.length; i++ )
					{
						maskedPassword[i] = '*';
					}
					if( is.available() > 0 )
					{
						Log.d( "vpn service", readString( is ) );
					}
					os.write( "/data/data/org.codeandroid.vpnc/vpnc --script /data/data/org.codeandroid.vpnc/vpnc-script --no-detach\n".getBytes() );

					Log.d( "vpn service", readString( is ) );
					Log.w( "vpnc service", "IP " + gateway );
					writeLine( os, gateway );
					Log.d( "vpn service", readString( is ) );
					Log.w( "vpnc service", "group id: " + id );
					writeLine( os, id );
					Log.d( "vpn service", readString( is ) );
					Log.w( "vpnc service", "group pwd " + secret );
					writeLine( os, secret );
					Log.d( "vpn service", readString( is ) );
					Log.w( "vpnc service", "user " + xauth );
					writeLine( os, xauth );
					Log.d( "vpn service", readString( is ) );
					Log.w( "vpnc service", "password " + String.valueOf( maskedPassword ) );
					writeLine( os, password );
					Log.d( "vpn service", readString( is ) );
					Log.w( "vpnc service", "done with vpnc" );
					stdoutLogging = new LoggingThread( is, "process stdout" );
					stdoutLogging.start();
					stderrLogging = new LoggingThread( process.getErrorStream(), "process stderr" );
					stderrLogging.start();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
				return true;
			}

			public boolean disconnect() throws RemoteException
			{
				Log.w( "vpnc service", "Got called with disconnect" );
				stdoutLogging.quit();
				stderrLogging.quit();
				process.destroy();
				return true;
			}
		};
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
