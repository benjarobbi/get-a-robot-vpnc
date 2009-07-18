package org.codeandroid.vpnc_frontend;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
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
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class VPNC extends PreferenceActivity implements OnPreferenceClickListener
{

	private final String TAG = "VPNC";
	private ProgressCategory networkList;
	private CheckBoxPreference vpnEnabled;

	private ServiceConnection serviceConnection = getServiceConnection();
	private Intent vpncIntent = new Intent( IVPNC_Service.class.getName() );
	private IVPNC_Service vpncService;
	private int connectedVpnId = -1;

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
		addPreferencesFromResource( R.xml.vpnc_settings );

		// Copy files to their locations, we should perhaps do it on the first run, of this version.
		Intent intent = new Intent( this, BackendFileManager.class );
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
	protected void onStart()
	{
		super.onStart();
		if( vpnEnabled.isChecked() && vpncService == null )
		{
			connectToService();
		}
	}

	@Override
	protected void onStop()
	{
		if( vpncService != null )
		{
			disconnectFromService();
		}
		super.onStop();
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
			NetworkPreference networkPreference = (NetworkPreference)networkList.getPreference( menuInfo.position - 3 );

			switch( item.getItemId() )
			{
				case 0:
					NetworkConnectionInfo info = NetworkDatabase.getNetworkDatabase( this ).singleNetwork( networkPreference._id );
					try
					{
						vpncService.connect( info.getIpSecGateway(), info.getIpSecId(), info.getIpSecSecret(), info.getXauth(), info.getPassword() );
						connectedVpnId = info.getId();
					}
					catch( RemoteException e )
					{
						e.printStackTrace();
					}
					break;

				case 1:
					if( connectedVpnId == networkPreference._id )
					{
						try
						{
							vpncService.disconnect();
							connectedVpnId = -1;
						}
						catch( RemoteException e )
						{
							e.printStackTrace();
						}
					}
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

	private ServiceConnection getServiceConnection()
	{
		return new ServiceConnection()
		{

			public void onServiceConnected(ComponentName name, IBinder service)
			{
				System.out.println( "yay, connected!" );
				vpncService = IVPNC_Service.Stub.asInterface( service );
			}

			public void onServiceDisconnected(ComponentName name)
			{
				System.out.println( "oops, disconnected!" );
				vpncService = null;
			}
		};
	}

	private void connectToService()
	{
		System.out.println( "Will connect to service" );
		// Call start service first so the service lifecycle isn't tied to this activity
		startService( vpncIntent );
		bindService( vpncIntent, serviceConnection, Context.BIND_AUTO_CREATE );
	}

	private void disconnectFromService()
	{
		System.out.println( "Will disconnect service" );
		unbindService( serviceConnection );
		stopService( vpncIntent );
	}

	private OnPreferenceChangeListener getVPNActivationListener()
	{
		return new OnPreferenceChangeListener()
		{

			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				if( Boolean.TRUE.equals( newValue ) )
				{
					connectToService();
				}
				else if( vpncService != null )
				{
					if( connectedVpnId != -1 )
					{
						try
						{
							vpncService.disconnect();
							connectedVpnId = -1;
						}
						catch( RemoteException e )
						{
							e.printStackTrace();
						}
					}
					disconnectFromService();
				}
				return true;
			}
		};
	}
}
