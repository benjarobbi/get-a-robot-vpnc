package org.codeandroid.vpnc_frontend;

import android.app.Activity;
import android.app.Dialog;
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
    private static final int DIALOG_CONFIRM_DELETE = 10;

    private int id;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );
		id = this.getIntent().getIntExtra(Intent.EXTRA_TITLE, NEW_CONNECTION);
		setContentView(R.layout.edit_network);
		if( id == NEW_CONNECTION )
		{
			setTitle(R.string.add_network);
			findViewById(R.id.addNetworkButtonLayout).setVisibility(View.VISIBLE);
			findViewById(R.id.editNetworkButtonLayout).setVisibility(View.GONE);
			((Button)findViewById(R.id.editNetworkAddButton)).setOnClickListener(saveOnClickListener);
			((Button)findViewById(R.id.editNetworkCancelButton)).setOnClickListener(cancelOnClickListener);
		}
		else
		{
			setTitle(R.string.edit_network);
			findViewById(R.id.addNetworkButtonLayout).setVisibility(View.GONE);
			findViewById(R.id.editNetworkButtonLayout).setVisibility(View.VISIBLE);
			((Button)findViewById(R.id.editNetworkUpdateButton)).setOnClickListener(saveOnClickListener);
			((Button)findViewById(R.id.editNetworkDeleteButton)).setOnClickListener(deleteOnClickListener);
			((Button)findViewById(R.id.editNetworkCancelButton2)).setOnClickListener(cancelOnClickListener);
			NetworkConnectionInfo connectionInfo = NetworkDatabase.getNetworkDatabase(this).singleNetwork(id);
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

	private OnClickListener saveOnClickListener = new OnClickListener()
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
				NetworkDatabase.getNetworkDatabase(EditNetwork.this).createNetwork(connectionInfo);
			}
			else
			{
				connectionInfo.setId(id);
				NetworkDatabase.getNetworkDatabase(EditNetwork.this).updateNetwork(connectionInfo);
			}
			finish();
		}
	};

	private OnClickListener cancelOnClickListener = new OnClickListener()
	{
		public void onClick(View view)
		{
			finish();
		}
	};

	private OnClickListener deleteOnClickListener = new OnClickListener()
	{
		public void onClick(View view)
		{
			showDialog(DIALOG_CONFIRM_DELETE);
		}
	};

	@Override
	protected Dialog onCreateDialog(int dialogId)
	{
		if( dialogId == DIALOG_CONFIRM_DELETE )
		{
			Dialog dialog = new Dialog(this);
			dialog.setContentView(R.layout.delete_confirmation_dialog);
			dialog.setTitle(R.string.confirmation_message);
			Button okButton = (Button)dialog.findViewById(R.id.deleteConfirmationOkButton);
			okButton.setOnClickListener(okDeleteOnClickListener);
			Button cancelButton = (Button)dialog.findViewById(R.id.deleteConfirmationCancelButton);
			cancelButton.setOnClickListener(cancelDeleteDialogListener);
			return dialog;
		}
		else
		{
			throw new IllegalStateException("Creation of dialog id #" + dialogId + " not expected.");
		}
	}
	
	private OnClickListener okDeleteOnClickListener = new OnClickListener()
	{
		public void onClick(View view)
		{
			NetworkDatabase.getNetworkDatabase(EditNetwork.this).deleteNetwork(id);
			dismissDialog(DIALOG_CONFIRM_DELETE);
			finish();
		}
	};

	private OnClickListener cancelDeleteDialogListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			dismissDialog(DIALOG_CONFIRM_DELETE);
		}
	};
}