package org.codeandroid.vpnc_frontend;

import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.View;
import android.widget.ImageView;
import android.util.Log;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.content.Intent;
import android.app.Activity;

import org.codeandroid.vpnc_frontend.NetworkDialog;
import org.codeandroid.vpnc_frontend.NetworkDatabase;

public class NetworkPreference extends Preference  {

	public int _id = -1;

	private Context mycontext;
	private final String TAG = "VPNC - PREFERENCE WINDOW";
	private final int SUB_ACTIVITY_REQUEST_CODE = 1;

	public NetworkPreference(Context context, AttributeSet attrs) {
		super(context,attrs);
		Log.i(TAG, "Creating new NetworkPreference");
	}

	public int getid() {
		return _id;
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
	}

	public void refreshNetworkState() {
		// FIXME: refresh self state
	}

}
