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
	private Process process;
	private LoggingThread stdoutLogging;
	private LoggingThread stderrLogging;
	private int vpncProcessId;
	private NetworkConnectionInfo connectionInProgress;
	
	public VpncProcessHandler()
	{
		Util.debug( "VpncProcessHandler instantiated" );
		vpncProcessId = Util.getProcessId();
	}

	public void connect(final VPNC vpnc, NetworkConnectionInfo info)
	{
		if( vpncProcessId > 0 )
		{
			Util.debug( "Asked to connect but there is a pid for an existing vpnc connection" );
			vpncProcessId = Util.getProcessId(); //double-check if we're still connected
			if( vpncProcessId > 0 )
			{
				Util.warn( "Asked to connect but there is a pid for an existing vpnc connection" );
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
				Util.debug( "tun does not exist" );
				vpnc.setConnected( false, info );
				this.connectionInProgress = null;
			}
			
			PrintWriter logWriter = new PrintWriter( vpnc.openFileOutput( "lastConnection.log", Context.MODE_PRIVATE ) );
			OutputStream os = process.getOutputStream();
			InputStream is = process.getInputStream();
			if( is.available() > 0 )
			{
				Util.debug( Util.readString( is, logWriter, true ) );
			}
			Util.writeLine( os, logWriter, "/data/data/org.codeandroid.vpnc_frontend/files/vpnc --script /data/data/org.codeandroid.vpnc_frontend/files/vpnc-script --no-detach" );

			Util.debug( Util.readString( is, logWriter, true ) );
			Util.debug( "IP " + gateway );
			Util.writeLine( os, logWriter, gateway );
			Util.debug( Util.readString( is, logWriter, true ) );
			Util.debug( "group id: " + ipsecId );
			Util.writeLine( os, logWriter, ipsecId );
			Util.debug( Util.readString( is, logWriter, true ) );
			Util.debug( "group pwd " + secret );
			Util.writeLine( os, logWriter, secret );
			Util.debug( Util.readString( is, logWriter, true ) );
			Util.debug( "user " + xauth );
			Util.writeLine( os, logWriter, xauth );
			Util.debug( Util.readString( is, logWriter, true ) );
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
			Util.error( "While reading from / writing to process stream", e );
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
			Util.debug( "password " + String.valueOf(maskedPassword) );
			logWriter.println(maskedPassword);
			Util.writeLine( os, null, password );
			logWriter.flush();
			Util.debug( "done interacting with vpnc" );
			
			//Now wait for connection confirmation, process death or timeout:
			long idleTimeout = 30000;
			long idleInterval = 500;
			String connectString = "vpnc-script ran to completion";
			while( idleTimeout > 0 )
			{
				String stdinString = Util.readString( is, logWriter, false );
				if( stdinString != null )
				{
					Util.debug( "process stdout: " + stdinString );
					logWriter.println( "process stdout" + "\t" + stdinString );
				}
				String stderrString = Util.readString( es, logWriter, false );
				if( stderrString != null )
				{
					Util.debug( "process stderr: " + stderrString );
					logWriter.println( "process stderr" + "\t" + stderrString );
				}
				String concatenation = stdinString  + " - " + stderrString;
				if( concatenation.contains(connectString) )
				{
					String msg = "Connect string detected!";
					Util.debug( msg );
					logWriter.println(msg);
					vpnc.setConnected(true, info);
					this.connectionInProgress = null;
					stdoutLogging = new LoggingThread( is, logWriter, "process stdout: ", Log.DEBUG );
					stdoutLogging.start();
					stderrLogging = new LoggingThread( process.getErrorStream(), logWriter, "process stderr: ", Log.VERBOSE );
					stderrLogging.start();
					return;
				}
				else
				{
					vpncProcessId = Util.getProcessId();
					if( vpncProcessId == -1 )
					{
						String msg = "process had died, return as failed connection";
						Util.debug( msg );
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
							Util.debug( msg );
							logWriter.println(msg);
							Thread.sleep(idleInterval);
						}
						catch( InterruptedException e )
						{
							Util.error( "While waiting to get vpnc process status", e );
						}
						idleTimeout -= idleInterval;
					}
				}
			}
			//reaching this point means we've timed out, kill process
			vpncProcessId = Util.getProcessId();
			if( vpncProcessId > 0 )
			{
				String msg = "vpnc still trying to connect but we've timed out. Will try to kill it and return now.";
				Util.debug( msg );
				logWriter.println(msg);
				disconnect();
				vpnc.setConnected(false, info);
			}
		}
		catch( IOException e )
		{
			Util.error( "While reading from / writing to process stream", e );
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
			vpncProcessId = Util.getProcessId();
		}
		if( vpncProcessId > 0 )
		{
			Util.debug( "will kill process " + vpncProcessId );
			try
			{
				Process killProcess = Runtime.getRuntime().exec("su -c sh");
				OutputStream os = killProcess.getOutputStream();
				Util.writeLine( os, null, "kill " + vpncProcessId );
				Util.writeLine( os, null, "exit" );
				os.close();
				Util.debug( "killProcess exited with exit value of " + killProcess.waitFor() );
				killProcess.destroy();
				vpncProcessId = -1;
				
				if( process != null )
				{
					process.destroy();
					process = null;
					Util.debug( "process killed" );
				}
				if( stderrLogging != null )
				{
					stderrLogging.quit();
					stdoutLogging.quit();
				}
			}
			catch( IOException e )
			{
				Util.error( e.getMessage(), e );
			}
			catch( InterruptedException e )
			{
				Util.error( e.getMessage(), e );
			}
			return true;
		}
		else
		{
			Util.debug( "Will ignore disconnect call, no vpnc pid stored" );
			return false;
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
}
