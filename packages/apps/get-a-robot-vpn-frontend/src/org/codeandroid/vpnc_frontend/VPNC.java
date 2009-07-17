package org.codeandroid.vpnc_frontend;

import java.util.List;

import android.preference.PreferenceActivity; 
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.content.Context;
import android.widget.Button;

import android.os.Bundle;
import android.view.View;
import android.util.Log; 
import android.content.Intent;

import android.widget.ListView;
import android.database.Cursor;
import android.widget.AdapterView;

import org.codeandroid.vpnc_frontend.NetworkPreference;
import org.codeandroid.vpnc_frontend.NetworkManager;
import org.codeandroid.vpnc_frontend.ProgressCategory;
import org.codeandroid.vpnc_frontend.NetworkDialog;
import android.widget.AdapterView.OnItemLongClickListener;
import android.preference.Preference.OnPreferenceClickListener;

public class VPNC extends PreferenceActivity implements OnItemLongClickListener,OnPreferenceClickListener {

	private final String TAG = "VPNC";
	private ProgressCategory NetworkList;
	private CheckBoxPreference VPNEnabled;
	private ListView lv; 

	public NetworkDatabase ndb;
	public Cursor networks;

	private final int SUB_ACTIVITY_REQUEST_CODE = 1;

	private final int VPN_ENABLE = 1;
	private final int VPN_NOTIFICATIONS = 2;
	private final int ADD_NETWORK = 4;

	private final String VPN_ENABLE_KEY= "VPN";
	private final String VPN_NOTIFICATIONS_KEY= "NOTIFICATION";
	private final String ADD_NETWORK_KEY="ADD_NETWORK";



    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.vpnc_settings);

		VPNEnabled = (CheckBoxPreference ) findPreference("VPN");

		NetworkList = (ProgressCategory ) findPreference("network_list");
		NetworkList.setOnPreferenceClickListener(this);
		
		Preference AddNew = (Preference) findPreference("ADD_NETWORK");
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
		List<NetworkConnectionInfo> connectionInfos = n.allNetworks();
		for( NetworkConnectionInfo connectionInfo : connectionInfos )
		{
			NetworkPreference pref = new NetworkPreference(this, null, connectionInfo );
			Log.i(TAG, "Adding NetworkPreference with ID:" +  connectionInfo.getId());
			NetworkList.addPreference(pref);
		}
	}

	public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id)
	{
		Log.i(TAG, "LONG PRESS Pos: " + pos + " ID: " + id );
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
				NetworkDialog dialog = new NetworkDialog(this, p.getid());
				dialog.show();	
				return true;
		}

		// Should be handled by single clicks.
		return false;
	}

	public boolean onPreferenceClick(Preference preference) {
		
		String key = preference.getKey();

		if (key.equals(VPN_ENABLE_KEY)) {
				Log.i(TAG, "on preference click handling vpn checkbox");
		}
		else if (key.equals(VPN_NOTIFICATIONS_KEY)) {
				Log.i(TAG, "on preference click handling notifications checkbox");
		}
		else if (key.equals(ADD_NETWORK_KEY)) {
			Log.i(TAG, "on preference click handling add button");
			Intent intent = new Intent(this, NetworkEditor.class);
			intent.putExtra(Intent.EXTRA_TITLE , -1 );
	                startActivityForResult(intent, SUB_ACTIVITY_REQUEST_CODE);
			Log.i(TAG, "ROAAAAAAAAAAAR!");
		}
		else {
				Log.i(TAG, "dont care about the other preferences");
		}
		
		// We should only handle a few cases here, not everything.
		return true;
	}	


	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "On Activity Result: resultcode" + resultCode);
		ShowNetworks();
	}
}
