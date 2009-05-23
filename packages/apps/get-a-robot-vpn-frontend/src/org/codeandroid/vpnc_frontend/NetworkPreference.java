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

public class NetworkPreference extends Preference implements OnPreferenceClickListener {

	public int _id = -1;

	private Context mycontext;
	private final String TAG = "VPNC - PREFERENCE WINDOW";
	private final int SUB_ACTIVITY_REQUEST_CODE = 1;

	public NetworkPreference(Context context, AttributeSet attrs) {
		super(context,attrs);
		this.setOnPreferenceClickListener(this);
		Log.i(TAG, "Creating new NetworkPreference");
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
	}

	public void refreshNetworkState() {
		// FIXME: refresh self state
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {
		Log.i(TAG, "On pref clicked id is " + _id );
		Context c = getContext();
               	Intent i = new Intent(c, NetworkEditor.class);
              	i.putExtra( Intent.EXTRA_TITLE , _id );
		c.startActivity(i);
		return true;
	}

}
