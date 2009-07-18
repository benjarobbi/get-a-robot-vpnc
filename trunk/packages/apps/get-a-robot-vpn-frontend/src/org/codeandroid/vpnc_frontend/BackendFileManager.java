package org.codeandroid.vpnc_frontend;

import android.app.Activity; 

import java.io.File;

import android.os.Bundle;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// This class manages the creation of files on the disk in the /data/data/ direcotry.
public class BackendFileManager extends Activity {

	private final String LOG_TAG = "VPNC_filemanager";

	private static double version = 0.01;
	
	// FIXME: make symlinks for the route/ifconfig to bb portably/sanely.
	// Ideally i'd like to iterate through the list, but can't see an easy way 
	private static String[] files = {	"vpnc", 
										"vpnc-script", 
										"bb",
										"make-tun-device"};

	private static String targetDirectory = "/data/data/org.codeandroid.vpnc_frontend/files/";
	    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.main);
		
		try {
			
			for (int i = 0; i < files.length; i++) {
					copyFromAsset(files[i], new File(targetDirectory + files[i]));	
			}			
			
		}
		catch (Throwable t) {
			Log.e(LOG_TAG , "Exception copying asset", t);
		}
		
		finish(); 
	}
	
	void copyFromAsset(String srcName, File dst)
		throws IOException {
		
		Log.i(LOG_TAG, "Copying " + srcName + " to " +  dst);
		
		InputStream in= this.getAssets().open(srcName);
		OutputStream out=new FileOutputStream(dst);

		// this should be fine for size ?
		byte[] buf=new byte[10240];
		int len;
		
		while ((len=in.read(buf))>0) {
			out.write(buf, 0, len);
		}
		
		in.close();
		out.close();
	}
}

