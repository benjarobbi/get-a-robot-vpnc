package org.codeandroid.vpnc_frontend;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;

public class NetworkPreference extends Preference implements OnPreferenceClickListener
{

	public int _id = -1;

	private Context mycontext;
	private final String PREFIX = "Preference Window: ";
	private long lastConnect;
	
	public static DateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy hh:mm");

	public NetworkPreference(Context context, AttributeSet attrs)
	{
		super( context, attrs );
		mycontext = context;
		setOnPreferenceClickListener( this );
		Util.info( PREFIX + "Creating new NetworkPreference" );
	}

	public NetworkPreference(Context context, AttributeSet attrs, NetworkConnectionInfo connectionInfo)
	{
		this( context, attrs );
		this.setTitle( connectionInfo.getNetworkName() );
		this.setEnabled( true );
		this._id = connectionInfo.getId();
		lastConnect = connectionInfo.getLastConnect();
		refreshNetworkState();
	}

	public int getid()
	{
		return _id;
	}
	
	public void setLastConnect(long lastConnect)
	{
		this.lastConnect = lastConnect;
	}

	public void refreshNetworkState()
	{
		if( lastConnect == Long.MAX_VALUE )
		{
			this.setSummary( R.string.never_connected );
		}
		else
		{
			this.setSummary( dateFormat.format( new Date(lastConnect) ) );
		}
	}

	public boolean onPreferenceClick(Preference preference)
	{
		Intent myIntent = new Intent( mycontext, EditNetwork.class );
		myIntent.putExtra( Intent.EXTRA_TITLE, _id );
		mycontext.startActivity( myIntent );
		return true;
	}
}
