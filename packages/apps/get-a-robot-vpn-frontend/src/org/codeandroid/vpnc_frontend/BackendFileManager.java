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

// This class manages the creation of files on the disk in the /data/data/ directory.
public class BackendFileManager extends Activity
{

	private final static String PREFIX = "filemanager: ";
	private static String[] files = {"vpnc", "vpnc-script"};

	private Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );

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
					Util.error( PREFIX + "Exception copying asset", t );
				}

				try
				{
					if( !new File( "/dev/net/tun" ).exists() )
					{
						if( !new File("/dev/tun").exists() )
						{
							//Try a modprobe, what's the downside?
							loadModule( "tun" );
						}
						if( new File( "/dev/tun" ).exists() )
						{
							if( !new File( "/dev/net" ).exists() )
							{
								createDirectory( true, "/dev/net" );
							}
							symlinkFile( true, "/dev/tun", "/dev/net/tun" );
						}
						else
						{
							Util.error( PREFIX + "There is no tun either at /dev/net or /dev" );
						}
					}
					else
					{
						Util.debug( PREFIX + "Found /dev/net/tun in place" );
					}
				}
				catch( IOException e )
				{
					Util.error( PREFIX + "Exception attempting to symlink /dev/net/tun", e );
				}

				try
				{
					setFilesExecutable();
				}
				catch( Throwable t )
				{
					Util.error( PREFIX + "Exception attempting to set files executable", t );
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
			Util.info( PREFIX + "File: " + dstFile + "exists, not copying" );
			return;
		}

		Util.info( PREFIX + "Copying " + fileName + " to " + getFilesDir() + "/" + fileName );

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
			Util.info( PREFIX + "Symbolic link " + lnk + " exists, continuing..." );
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

		Util.info( PREFIX + "ln -s " + res + " " + lnk + "\n" );
		out.writeBytes( "ln -s " + res + " " + lnk + "\n" );
		out.writeBytes( "exit\n" );
		out.flush();
		out.close();
		try
		{
			Util.info( PREFIX + "Done creating sym link " + lnk + " with return code " + process.waitFor() );
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
			Util.info( PREFIX + "Done setting permission with return code " + process.waitFor() );
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
		//			Util.debug( PREFIX + readString( process.getErrorStream() ) );
		out.writeBytes( "exit\n" );
		out.flush();
		out.close();
		try
		{
			Util.info( PREFIX + "Done creating directory " + dir + " with return code " + process.waitFor() );
		}
		catch( InterruptedException e )
		{
			throw new RuntimeException( e );
		}
	}

	private void loadModule(String module) throws IOException
	{
		Process process = Runtime.getRuntime().exec( "su -c sh" );
		DataOutputStream out = new DataOutputStream( process.getOutputStream() );
		out.writeBytes( "modprobe " + module + "\n" );
		out.writeBytes( "exit\n" );
		out.flush();
		out.close();
		try
		{
			Util.info( PREFIX + "Loaded module " + module + " with return code " + process.waitFor() );
		}
		catch( InterruptedException e )
		{
			throw new RuntimeException( e );
		}
	}
}
