package org.codeandroid.vpnc_frontend;


import android.view.View.OnClickListener;
import android.view.View;
import android.util.Log; 

public class VPNConnectionManager implements OnClickListener {
	
	private String APPNAME = "VPNC CONNECTION MANAGER"; 

	public VPNConnectionManager() {
		Log.i(APPNAME, "Class  instantiated!");
	}
	
	public void onClick(View v) {
		Log.i(APPNAME, "Connect Button Clicked!");
	}

}
