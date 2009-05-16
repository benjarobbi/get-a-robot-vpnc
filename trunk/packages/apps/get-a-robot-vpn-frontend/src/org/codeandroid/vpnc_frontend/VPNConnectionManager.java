package org.codeandroid.vpnc_frontend;


import android.view.View.OnClickListener;
import android.view.View;
import android.util.Log; 

public class VPNConnectionManager implements OnClickListener {
	
	private String TAG = "VPNC CONNECTION MANAGER"; 

	public VPNConnectionManager() {
		Log.i(TAG, "Class  instantiated!");
	}
	
	public void onClick(View v) {
		Log.i(TAG, "Connect Button Clicked!");
		
		/* Wonder what I can see from here*/
	}

}
