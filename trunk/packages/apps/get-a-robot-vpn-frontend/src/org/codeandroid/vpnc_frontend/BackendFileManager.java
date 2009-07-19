package org.codeandroid.vpnc_frontend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.lang.Process; 
import java.io.DataOutputStream; 

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

// This class manages the creation of files on the disk in the /data/data/ direcotry.
public class BackendFileManager extends Activity
{

	private final String LOG_TAG = "VPNC_filemanager";

	// FIXME: make symlinks for the route/ifconfig to bb portably/sanely.
	// Ideally i'd like to iterate through the list, but can't see an easy way 
	private static String[] files = {"vpnc", "vpnc-script", "bb", "make-tun-device"};

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );
		// setContentView(R.layout.main);

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
			symlinkFile("bb", "ifconfig");
			symlinkFile("bb", "route"); 

		}
		catch ( Throwable t ) 
		{
			Log.e(LOG_TAG, "Exception attempting to create symlink", t );
		}


		try {
			setFilesExecutable(); 
		}
		catch (Throwable t) {
			Log.e(LOG_TAG, "Exception attempting to set files executable", t);
		}

		finish();
	}

	void copyFromAsset(String fileName) throws IOException
	{


                File dstfile = new File( getFilesDir() + "/" + fileName ); 
 
                if (dstfile.exists()) { 
                        Log.i(LOG_TAG, "File exists, not copying"); 
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

	private void symlinkFile(String res, String lnk) throws IOException {


		File linkfil = new File(lnk);

		if (linkfil.exists()) {
			Log.i(LOG_TAG, "File exists, ninja");
		}

		try {
			Process process = Runtime.getRuntime().exec("sh");
			DataOutputStream out = new DataOutputStream(process.getOutputStream());

			Log.i(LOG_TAG, "ln -s " + getFilesDir() + "/" +  res + " " + getFilesDir() + "/" +  lnk + "\n");
			out.writeBytes("ln -s " + getFilesDir() + "/" + res + " " + getFilesDir() + "/" +  lnk + "\n");
			out.writeBytes("exit\n");
			out.flush();
		}
		catch (IOException e) {
			Log.i(LOG_TAG, "Problem making symbolic link, what ever will we do"); 
		}
	}

	private void setFilesExecutable() throws IOException {

		try {
			Process process = Runtime.getRuntime().exec("sh");
			DataOutputStream out = new DataOutputStream(process.getOutputStream());
			out.writeBytes("chmod 755 " + getFilesDir() + "/*\n");
			out.writeBytes("exit\n");
			out.flush();
	
		}
		catch (IOException e) {
			Log.i(LOG_TAG, "Problem making symbolic link, what ever will we do"); 
		}	


		Log.i(LOG_TAG, "Done setting permission");

	}

}
