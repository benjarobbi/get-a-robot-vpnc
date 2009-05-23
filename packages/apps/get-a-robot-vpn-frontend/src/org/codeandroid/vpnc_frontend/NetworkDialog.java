package org.codeandroid.vpnc_frontend;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

public class NetworkDialog extends AlertDialog implements DialogInterface.OnClickListener,
        AdapterView.OnItemSelectedListener, View.OnClickListener {

	private final String TAG = "VPNC";
	private final String SUB = "Network Dialog BOX";

	private View mView;

    	public NetworkDialog(Context context, int _id) {

		super(context);
		if (_id == -1) {
			this.setTitle("New Network: ");
		}
		else {
			NetworkDatabase n = new NetworkDatabase(context);
			// FIXME: Load from ID here. 
		}

	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	onLayout();
        super.onCreate(savedInstanceState);
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
