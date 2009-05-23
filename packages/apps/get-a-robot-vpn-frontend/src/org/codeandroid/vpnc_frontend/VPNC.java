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

public class VPNC extends PreferenceActivity 
{

	PreferenceGroup NetworkList;
	private final String TAG = "VPNC";
	private ProgressCategory NL;
	private CheckBoxPreference VPNEnabled;

	public NetworkDatabase ndb;
	public Cursor networks;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.vpnc_settings);

		NL = (ProgressCategory ) findPreference("network_list");
		VPNEnabled = (CheckBoxPreference ) findPreference("VPN");

		ShowNetworks();

		Preference AddNew = (Preference) findPreference("add_another_network");
		Intent i = new Intent(new Intent(this, NetworkEditor.class));

		/* Add new is always -1 */
		i.putExtra( Intent.EXTRA_TITLE , -1 );
        AddNew.setIntent(i);

	}


	private void ShowNetworks() {

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
			/*
          	Intent i = new Intent(new Intent(this, NetworkEditor.class));
           	i.putExtra( Intent.EXTRA_TITLE , _id );
           	pref.setIntent(i);
			*/
			NL.addPreference(pref);
		}
	
		cursor.close();
	}
}
