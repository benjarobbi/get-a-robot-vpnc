package org.codeandroid.vpnc_frontend;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

// This class manages the creation of files on the disk in the /data/data/ direcotry.
public class BackendFileManager extends Activity
{

	private final String LOG_TAG = "VPNC_filemanager";

	private Handler handler = new Handler();

	// FIXME: make symlinks for the route/ifconfig to bb portably/sanely.
	// Ideally i'd like to iterate through the list, but can't see an easy way 
	private static String[] files = {"vpnc", "vpnc-script", "bb", "make-tun-device"};

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );
		// setContentView(R.layout.main);

		final ProgressDialog progressDialog = ProgressDialog.show( this, getString( R.string.please_wait ), getString( R.string.installing ) );
		Thread thread = new Thread()
		{

			@Override
			public void run()
			{
				try
				{
					for( int i = 0; i < files.length; i++ )
					{
						copyFromAsset( files[i] );
					}

				}
				catch( Throwable t )
				{
					Log.e( LOG_TAG, "Exception copying asset", t );
				}

				try
				{
					symlinkFile( false, getFilesDir() + "/" + "bb", getFilesDir() + "/" + "ifconfig" );
					symlinkFile( false, getFilesDir() + "/" + "bb", getFilesDir() + "/" + "route" );

				}
				catch( Throwable t )
				{
					Log.e( LOG_TAG, "Exception attempting to create symlink", t );
				}

				try
				{
					if( ( !new File( "/dev/net/tun" ).exists() ) && new File( "/dev/tun" ).exists() )
					{
						if( !new File( "/dev/net" ).exists() )
						{
							createDirectory( true, "/dev/net" );
						}
						symlinkFile( true, "/dev/tun", "/dev/net/tun" );
					}
				}
				catch( IOException e )
				{
					Log.e( LOG_TAG, "Exception attempting to symlink /dev/net/tun", e );
				}

				try
				{
					setFilesExecutable();
				}
				catch( Throwable t )
				{
					Log.e( LOG_TAG, "Exception attempting to set files executable", t );
				}

				Runnable uiTask = new Runnable()
				{

					public void run()
					{
						progressDialog.dismiss();
						finish();
					}
				};
				handler.post( uiTask );
			}
		};
		thread.start();
	}

	void copyFromAsset(String fileName) throws IOException
	{

		File dstFile = new File( getFilesDir() + "/" + fileName );

		if( dstFile.exists() )
		{
			Log.i( LOG_TAG, "File: " + dstFile + "exists, not copying" );
			return;
		}

		Log.i( LOG_TAG, "Copying " + fileName + " to " + getFilesDir() + "/" + fileName );

		InputStream in = this.getAssets().open( fileName );
		OutputStream out = openFileOutput( fileName, MODE_PRIVATE );

		// this should be fine for size ?
		byte[] buf = new byte[10240];
		int len;

		while( ( len = in.read( buf ) ) > 0 )
		{
			out.write( buf, 0, len );
		}

		in.close();
		out.close();
	}

	private void symlinkFile(boolean asRoot, String res, String lnk) throws IOException
	{

		File linkfil = new File( lnk );

		if( linkfil.exists() )
		{
			Log.i( LOG_TAG, "Symbolic link " + lnk + " exists, continuing..." );
			return;
		}

		Process process;
		if( asRoot )
		{
			process = Runtime.getRuntime().exec( "su -c sh" );
		}
		else
		{
			process = Runtime.getRuntime().exec( "sh" );
		}
		DataOutputStream out = new DataOutputStream( process.getOutputStream() );

		Log.i( LOG_TAG, "ln -s " + res + " " + lnk + "\n" );
		out.writeBytes( "ln -s " + res + " " + lnk + "\n" );
		out.writeBytes( "exit\n" );
		out.flush();
		out.close();
		try
		{
			Log.i( LOG_TAG, "Done creating sym link " + lnk + " with return code " + process.waitFor() );
		}
		catch( InterruptedException e )
		{
			throw new RuntimeException( e );
		}
	}

	private void setFilesExecutable() throws IOException
	{

		Process process = Runtime.getRuntime().exec( "sh" );
		DataOutputStream out = new DataOutputStream( process.getOutputStream() );
		out.writeBytes( "chmod 755 " + getFilesDir() + "/*\n" );
		out.writeBytes( "exit\n" );
		out.flush();
		out.close();
		try
		{
			Log.i( LOG_TAG, "Done setting permission with return code " + process.waitFor() );
		}
		catch( InterruptedException e )
		{
			throw new RuntimeException( e );
		}
	}

	private void createDirectory(boolean asRoot, String dir) throws IOException
	{

		Process process;
		if( asRoot )
		{
			process = Runtime.getRuntime().exec( "su -c sh" );
		}
		else
		{
			process = Runtime.getRuntime().exec( "sh" );
		}
		DataOutputStream out = new DataOutputStream( process.getOutputStream() );
		out.writeBytes( "mkdir " + dir + "\n" );
		//			Log.d( LOG_TAG, readString( process.getErrorStream() ) );
		out.writeBytes( "exit\n" );
		out.flush();
		out.close();
		try
		{
			Log.i( LOG_TAG, "Done creating directory " + dir + " with return code " + process.waitFor() );
		}
		catch( InterruptedException e )
		{
			throw new RuntimeException( e );
		}
	}
}
