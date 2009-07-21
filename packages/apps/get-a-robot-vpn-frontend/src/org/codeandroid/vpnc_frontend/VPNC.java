package org.codeandroid.vpnc_frontend;

import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class VPNC extends PreferenceActivity implements OnPreferenceClickListener
{

	private final String TAG = "VPNC";
	private ProgressCategory networkList;
	private CheckBoxPreference vpnEnabled;

	private VpncProcessHandler vpncHandler;
	private Handler handler = new Handler();
	private ProgressDialog progressDialog;
	private Dialog passwordDialog;
	private int connectedVpnId;

	private final int SUB_ACTIVITY_REQUEST_CODE = 1;

	private final int VPN_ENABLE = 1;
	private final int VPN_NOTIFICATIONS = 2;
	private final int ADD_NETWORK = 4;

	private final String VPN_ENABLE_KEY = "VPN";
	private final String VPN_NOTIFICATIONS_KEY = "NOTIFICATION";
	private final String ADD_NETWORK_KEY = "ADD_NETWORK";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );
		passwordDialog = new Dialog(this);
		connectedVpnId = getPreferences(MODE_PRIVATE).getInt( "connectedVpnId", -1 );
		vpncHandler = new VpncProcessHandler();
		if( vpncHandler.isConnected() == false )
		{
			//maybe the phone has been restarted or the vpnc process killed some other how
			connectedVpnId = -1;
		}
		if( connectedVpnId > 0 && vpncHandler.isConnected() )
		{
			Log.d( TAG, "Last saved state indicates that we're connected to connection #" + connectedVpnId );
		}

		addPreferencesFromResource( R.xml.vpnc_settings );

		// Copy files to their locations, we should perhaps do it on the first run, of this version.
		final Intent intent = new Intent( this, BackendFileManager.class );
		startActivityForResult( intent, SUB_ACTIVITY_REQUEST_CODE );

		vpnEnabled = (CheckBoxPreference)findPreference( "VPN" );
		vpnEnabled.setOnPreferenceChangeListener( getVPNActivationListener() );

		networkList = (ProgressCategory)findPreference( "network_list" );
		networkList.setOnPreferenceClickListener( this );

		Preference addNew = (Preference)findPreference( "ADD_NETWORK" );
		addNew.setOnPreferenceClickListener( this );

		getListView().setOnCreateContextMenuListener( createContextMenuListener );
		ShowNetworks();
	}
	
	@Override
	protected void onPause()
	{
		Editor editor = getPreferences(MODE_PRIVATE).edit();
		editor.putInt( "connectedVpnId", connectedVpnId );
		editor.commit();
		super.onPause();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		Log.d( TAG, "Configuration Changed. I'd rather handle it myself (and ignore it for now) than have the OS blow up all I've got in the middle of a connection!" );
		super.onConfigurationChanged( newConfig );
	}

	private OnCreateContextMenuListener createContextMenuListener = new OnCreateContextMenuListener()
	{

		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
		{
			if( menuInfo instanceof AdapterContextMenuInfo )
			{
				AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo)menuInfo;

				switch( (int)adapterInfo.id )
				{
					case VPN_ENABLE:
						Log.i( TAG, "long press handler skipping vpn checkbox" );
						break;
					case VPN_NOTIFICATIONS:
						Log.i( TAG, "long press handler skipping notifications checkbox" );
						break;

					case ADD_NETWORK:
						Log.i( TAG, "long press handler skipping add button" );
						break;

					default:
						Log.i( TAG, "long press handler, handling the choice" );
						menu.add( Menu.NONE, 0, 0, R.string.connect );
						menu.add( Menu.NONE, 1, 1, R.string.disconnect );
						menu.add( Menu.NONE, 2, 2, R.string.edit );
				}
			}
		}
	};

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		if( item.getMenuInfo() instanceof AdapterContextMenuInfo )
		{
			AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo();
			final NetworkPreference networkPreference = (NetworkPreference)networkList.getPreference( menuInfo.position - 3 );

			switch( item.getItemId() )
			{
				case 0:
					final NetworkConnectionInfo info = NetworkDatabase.getNetworkDatabase( this ).singleNetwork( networkPreference._id );
					progressDialog = ProgressDialog.show( this, getString(R.string.please_wait), getString(R.string.connecting) );
					Thread thread = new Thread()
					{
						@Override
						public void run()
						{
							vpncHandler.connect( VPNC.this, info );
						}
					};
					thread.start();
					break;

				case 1:
					//if( connectedVpnId == networkPreference._id )
					final ProgressDialog disconnectProgressDialog = ProgressDialog.show( this, getString(R.string.please_wait), getString(R.string.disconnecting) );
					Thread disconnectThread = new Thread()
					{
						@Override
						public void run()
						{
							vpncHandler.disconnect();
							Runnable uiTask = new Runnable()
							{
								public void run()
								{
									connectedVpnId = -1;
									networkPreference.refreshNetworkState();
									disconnectProgressDialog.dismiss();
								}
							};
							handler.post( uiTask );
						}
					};
					disconnectThread.start();
					break;

				case 2:
					Intent intent = new Intent( this, EditNetwork.class );
					intent.putExtra( Intent.EXTRA_TITLE, networkPreference.getid() );
					startActivityForResult( intent, SUB_ACTIVITY_REQUEST_CODE );

				default:
					break;
			}
		}
		return false;
	}

	public void setConnected(final boolean connected, final NetworkConnectionInfo info)
	{
		NetworkPreference networkPreferenceFound = null;
		for( int index = 0; index < networkList.getPreferenceCount() && networkPreferenceFound == null; index++ )
		{
			NetworkPreference candidate = (NetworkPreference)networkList.getPreference(index);
			if( candidate._id == info.getId() )
			{
				networkPreferenceFound = candidate;
			}
		}
		final NetworkPreference networkPreference = networkPreferenceFound;
		
		//The network list shouldn't have changed so I'm banking on networkPreference not being null now
		Runnable uiTask = new Runnable()
		{

			public void run()
			{
				if( connected )
				{
					long timestamp = System.currentTimeMillis();
					info.setLastConnect( timestamp );
					NetworkDatabase.getNetworkDatabase( VPNC.this ).updateNetwork( info );
					connectedVpnId = info.getId();
					networkPreference.setLastConnect( timestamp );
					networkPreference.setSummary( R.string.connected );
				}
				else
				{
					networkPreference.setSummary( R.string.failed_connect );
				}
				progressDialog.dismiss();
			}
		};
		handler.post( uiTask );
	}
	
	public void getPassword()
	{
		passwordDialog.setContentView(R.layout.password_dialog);
		passwordDialog.setTitle(R.string.please_type_password);
		Button okButton = (Button)passwordDialog.findViewById( R.id.okPasswordButton );
		okButton.setOnClickListener(okPasswordOnClickListener);
		passwordDialog.show();
	}
	
	private OnClickListener okPasswordOnClickListener = new OnClickListener()
	{
		public void onClick(View view)
		{
			String password = ((EditText)view.getRootView().findViewById(R.id.passwordEditText)).getText().toString();
			vpncHandler.continueConnection( VPNC.this, password );
			passwordDialog.dismiss();
		}
	};

	/* I've been told that i should change this to use an adapter */
	private void ShowNetworks()
	{

		/* Remove all the networks, this is a full refresh */
		networkList.removeAll();

		NetworkDatabase n = NetworkDatabase.getNetworkDatabase( this );
		List<NetworkConnectionInfo> connectionInfos = n.allNetworks();
		for( NetworkConnectionInfo connectionInfo : connectionInfos )
		{
			NetworkPreference pref = new NetworkPreference( this, null, connectionInfo );
			Log.i( TAG, "Adding NetworkPreference with ID:" + connectionInfo.getId() );
			networkList.addPreference( pref );
			if( pref._id == connectedVpnId )
			{
				pref.setSummary( R.string.connected );
			}
		}
	}

	public boolean onPreferenceClick(Preference preference)
	{

		String key = preference.getKey();

		if( key.equals( VPN_ENABLE_KEY ) )
		{
			Log.i( TAG, "on preference click handling vpn checkbox" );
		}
		else if( key.equals( VPN_NOTIFICATIONS_KEY ) )
		{
			Log.i( TAG, "on preference click handling notifications checkbox" );
		}
		else if( key.equals( ADD_NETWORK_KEY ) )
		{
			Log.i( TAG, "on preference click handling add button" );
			Intent intent = new Intent( this, EditNetwork.class );
			intent.putExtra( Intent.EXTRA_TITLE, -1 );
			startActivityForResult( intent, SUB_ACTIVITY_REQUEST_CODE );
			Log.i( TAG, "ROAAAAAAAAAAAR!" );
		}
		else
		{
			Log.i( TAG, "dont care about the other preferences" );
		}

		// We should only handle a few cases here, not everything.
		return true;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult( requestCode, resultCode, data );
		Log.i( TAG, "On Activity Result: resultcode" + resultCode );
		ShowNetworks();
	}

	private OnPreferenceChangeListener getVPNActivationListener()
	{
		return new OnPreferenceChangeListener()
		{

			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				if( Boolean.TRUE.equals( newValue ) )
				{
					//nothing to do
				}
				else
				{
					vpncHandler.disconnect();
					connectedVpnId = -1;
				}
				return true;
			}
		};
	}
	
	
	public Handler getHandler()
	{
		return handler;
	}
}
