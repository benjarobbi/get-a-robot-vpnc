package org.codeandroid.vpnc_frontend;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View; 
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.view.View.OnClickListener; 

public class NetworkDialog extends AlertDialog implements DialogInterface.OnClickListener,
        AdapterView.OnItemSelectedListener, View.OnClickListener {

	private final String TAG = "VPNC";
	private final String SUB = "Network Dialog BOX";

	private View mView;

	Button connect;
	Button disconnect;
	Button edit;
	int _id;

    	public NetworkDialog(Context context, int id) {
		super(context);
		_id = id;
	}

	public void configureButtons() {

		connect = (Button) mView.findViewById(R.id.connect);

		disconnect = (Button) findViewById(R.id.disconnect);
		edit = (Button) findViewById(R.id.edit);

		connect.setOnClickListener( 
			new Button.OnClickListener() {
				@Override
           			public void onClick(View v) {
					Log.i(TAG, "Connect Clicked");
                 	 	}
			} 
		);

		disconnect.setOnClickListener( 
			new Button.OnClickListener() {
				@Override
           			public void onClick(View v) {
					Log.i(TAG, "Disconnect Clicked");
                 	 	}
			} 
		);

		edit.setOnClickListener( 
			new Button.OnClickListener() {
				@Override
           			public void onClick(View v) {
					Log.i(TAG, "Edit Clicked");
                 	 	}
			} 
		);




	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	setTitle("Options");
	onLayout();
        super.onCreate(savedInstanceState);
	configureButtons();
	
    }

    public void onLayout() {
        setInverseBackgroundForced(true);
        setView(mView = getLayoutInflater().inflate(R.layout.vpn_network_configure, null));
    }

    public void onClick(View v) {
    	Log.i(TAG, SUB + "In onclick for views");
    }

    public void onClick(DialogInterface dialog, int which) {
    	Log.i(TAG, SUB + "In onclick for dialogs");
    }

    public void onNothingSelected(AdapterView parent) {
    	Log.i(TAG, SUB + "In nothing selected");
    }

    public void onItemSelected(AdapterView parent, View view, int position, long id) {
    	Log.i(TAG, SUB + "In item selected");
    }


}
