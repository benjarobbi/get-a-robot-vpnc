package org.codeandroid.vpnc_frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class EditNetwork extends Activity
{
	private static final String LOG_TAG = "VPNC";
    private static final String PREFIX = EditNetwork.class.getSimpleName() + ":";
    private static final int NEW_CONNECTION = -1;

    private int id;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );
		id = this.getIntent().getIntExtra(Intent.EXTRA_TITLE, NEW_CONNECTION);
		setContentView(R.layout.edit_network);
		((Button)findViewById(R.id.editNetworkSaveButton)).setOnClickListener( getSaveOnClickListener() );
		((Button)findViewById(R.id.editNetworkCancelButton)).setOnClickListener( getCancelOnClickListener() );
		if( id == NEW_CONNECTION )
		{
			setTitle(R.string.add_network);
		}
		else
		{
			setTitle(R.string.edit_network);
			NetworkConnectionInfo connectionInfo = new NetworkDatabase(this).singleNetwork(id);
			((EditText)findViewById(R.id.nicknameEditText)).setText( connectionInfo.getNetworkName() );
			((EditText)findViewById(R.id.IPSecGatewayEditText)).setText( connectionInfo.getIpSecGateway() );
			((EditText)findViewById(R.id.IPSecIDEditText)).setText( connectionInfo.getIpSecId() );
			((EditText)findViewById(R.id.IPSecSecretEditText)).setText( connectionInfo.getIpSecSecret() );
			((EditText)findViewById(R.id.xauthEditText)).setText( connectionInfo.getXauth() );
			((EditText)findViewById(R.id.passwordEditText)).setText( connectionInfo.getPassword() );
		}
		Log.d(LOG_TAG, PREFIX + "onCreate - End");
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
	}

	private OnClickListener getSaveOnClickListener()
	{
		return new OnClickListener()
		{
			public void onClick(View v)
			{
				NetworkConnectionInfo connectionInfo = new NetworkConnectionInfo();
				
				connectionInfo.setNetworkName( ((EditText)findViewById(R.id.nicknameEditText)).getText().toString().toString() );
				connectionInfo.setIpSecGateway( ((EditText)findViewById(R.id.IPSecGatewayEditText)).getText().toString() );
				connectionInfo.setIpSecId( ((EditText)findViewById(R.id.IPSecIDEditText)).getText().toString() );
				connectionInfo.setIpSecSecret( ((EditText)findViewById(R.id.IPSecSecretEditText)).getText().toString() );
				connectionInfo.setXauth( ((EditText)findViewById(R.id.xauthEditText)).getText().toString() );
				connectionInfo.setPassword( ((EditText)findViewById(R.id.passwordEditText)).getText().toString() );
				if( id == NEW_CONNECTION )
				{
					new NetworkDatabase(EditNetwork.this).createNetwork(connectionInfo);
				}
				else
				{
					connectionInfo.setId(id);
					new NetworkDatabase(EditNetwork.this).updateNetwork(connectionInfo);
				}
				finish();
			}
		};
	}

	private OnClickListener getCancelOnClickListener()
	{
		return new OnClickListener()
		{
			public void onClick(View view)
			{
				finish();
			}
		};
	}
}