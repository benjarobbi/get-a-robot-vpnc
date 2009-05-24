package org.codeandroid.vpnc_frontend;

import android.preference.PreferenceActivity; 
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.content.Context;

import android.os.Bundle;
import android.view.View;
import android.util.Log; 
import android.content.Intent;

import android.widget.ListView;
import android.database.Cursor;

import org.codeandroid.vpnc_frontend.NetworkPreference;
import org.codeandroid.vpnc_frontend.ProgressCategory;
import android.preference.Preference.OnPreferenceClickListener;

public class VPNC extends PreferenceActivity implements OnPreferenceClickListener
{

	private final String TAG = "VPNC";
	private ProgressCategory NetworkList;
	private CheckBoxPreference VPNEnabled;

	public NetworkDatabase ndb;
	public Cursor networks;
	private final int SUB_ACTIVITY_REQUEST_CODE = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.vpnc_settings);

		NetworkList = (ProgressCategory ) findPreference("network_list");
		NetworkList.setOnPreferenceClickListener(this);

		VPNEnabled = (CheckBoxPreference ) findPreference("VPN");

		ShowNetworks();

		Preference AddNew = (Preference) findPreference("add_another_network");
		AddNew.setOnPreferenceClickListener(this);
	}


	/* I've been told that i should change this to use an adapter */ 
	private void ShowNetworks() {

		/* Remove all the networks, this is a full refresh */
		NetworkList.removeAll();

		NetworkDatabase n = new NetworkDatabase(this);
		Cursor cursor = n.allNetworks();

		int idColumn = cursor.getColumnIndex("_id");
		int nicknameColumn = cursor.getColumnIndex("nickname");

		while (cursor.moveToNext() ){ 
			String nickname = cursor.getString(nicknameColumn);
			int _id = cursor.getInt(idColumn);

			NetworkPreference pref = new NetworkPreference(this, null );
			pref.setSummary(R.string.never_connected);
			pref.setTitle(nickname);
			pref.setEnabled(true);
			pref._id = _id;
			pref.setOnPreferenceClickListener(this);
			NetworkList.addPreference(pref);
		}
	
		cursor.close();
	}

	@Override
 	public boolean onPreferenceClick(Preference p ){

		Log.i(TAG, "IN VPNC.java onpreferenceclick");
		Log.i(TAG, "key is: " + p.getKey());

		int id = 0;

		Intent intent = new Intent(this, NetworkEditor.class);

		if ("add_another_network".equals( p.getKey() )) {
			// We set a different WORKING.
			Log.i(TAG, "Setting Intent for new network");
			intent.putExtra(Intent.EXTRA_TITLE , -1 );
		}
		else {
			Log.i(TAG, "Not add_another_network");
			NetworkPreference n = (NetworkPreference) p;
			id = n.getid();
			intent.putExtra(Intent.EXTRA_TITLE , n.getid() );
		}
		startActivityForResult(intent, SUB_ACTIVITY_REQUEST_CODE);
		return true; 
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "On Activity Result: resultcode" + resultCode);
		ShowNetworks();
	}

}
