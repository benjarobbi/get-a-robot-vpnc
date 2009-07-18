package org.codeandroid.vpnc_frontend;

import android.widget.SimpleCursorAdapter;
import android.app.ListActivity;
import android.database.Cursor;

import static android.provider.BaseColumns._ID;
import static org.codeandroid.vpnc_frontend.Constants.TABLE_NAME;
import static org.codeandroid.vpnc_frontend.Constants.NETWORK_NAME;
import static org.codeandroid.vpnc_frontend.Constants.LAST_CONNECTION;

public class NetworkList extends ListActivity {

	private static int[] TO = { R.id.rowid, R.id.network_name };
	private static String[] FROM = { _ID, NETWORK_NAME };

	private void showNetworks(Cursor cursor) {

		SimpleCursorAdapter adapter = new SimpleCursorAdapter( this,R.layout.item, cursor, FROM, TO);
		setListAdapter(adapter); 
	} 

}
