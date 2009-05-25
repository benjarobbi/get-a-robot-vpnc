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
import android.widget.AdapterView;

import org.codeandroid.vpnc_frontend.NetworkPreference;
import org.codeandroid.vpnc_frontend.ProgressCategory;
import org.codeandroid.vpnc_frontend.NetworkDialog;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;

public class VPNC extends PreferenceActivity implements OnPreferenceClickListener, OnItemLongClickListener {

	private final String TAG = "VPNC";
	private ProgressCategory NetworkList;
	private CheckBoxPreference VPNEnabled;

	public NetworkDatabase ndb;
	public Cursor networks;

	private final int SUB_ACTIVITY_REQUEST_CODE = 1;

	private ListView lv;

	private final int VPN_ENABLE = 1;
	private final int VPN_NOTIFICATIONS = 2;
	private final int ADD_NETWORK = 4;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.vpnc_settings);

		VPNEnabled = (CheckBoxPreference ) findPreference("VPN");

		NetworkList = (ProgressCategory ) findPreference("network_list");
		NetworkList.setOnPreferenceClickListener(this);
		
		Preference AddNew = (Preference) findPreference("add_another_network");
		AddNew.setOnPreferenceClickListener(this);

			lv = getListView();
			lv.setOnItemLongClickListener (this);

		ShowNetworks();

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
	public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id)
	{
		Log.i(TAG, "LONG PRESS ID IS " +  " Pos: " + pos + " ID: " + id );
		Log.i(TAG, "Switching on: " + id ) ;

		switch((int)id) {
			case VPN_ENABLE:
				Log.i(TAG, "long press handler skipping vpn checkbox");
				break;
			case VPN_NOTIFICATIONS:
				Log.i(TAG, "long press handler skipping notifications checkbox");
				break;

			case ADD_NETWORK:
				Log.i(TAG, "long press handler skipping add button");
				break; 

			default:
				Log.i(TAG, "long press handler, handling the choice");
				NetworkPreference p = (NetworkPreference)av.getItemAtPosition(pos);
				Log.i(TAG, "Loading networkpreference " + p.getid() ) ;
				NetworkDialog dialog = new NetworkDialog(this, p.getid() );
				dialog.show();
				return true;
		}

		// Should be handled by single clicks.
		return false;
	}

	@Override
 	public boolean onPreferenceClick(Preference p ){

		int id = 0;
		Intent intent = new Intent(this, NetworkEditor.class);

		if ("add_another_network".equals( p.getKey() )) {
			Log.i(TAG, "Setting Intent for new network");
			intent.putExtra(Intent.EXTRA_TITLE , -1 );
		}
		else {
			Log.i(TAG, "Settings Intent for Existing network");
			NetworkPreference n = (NetworkPreference) p;
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
