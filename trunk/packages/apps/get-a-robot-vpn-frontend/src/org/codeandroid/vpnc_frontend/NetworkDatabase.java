package org.codeandroid.vpnc_frontend;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class NetworkDatabase extends SQLiteOpenHelper {
	
	public final static String DB_NAME = "networks";
	public final static int DB_VERSION = 1;

	public static final String KEY_ROWID = "_id";	
	public final static String TABLE_NETWORKS = "networks";
	public final static String FIELD_NETWORK_GATEWAY = "IPSec_gateway";
	public final static String FIELD_NETWORK_ID = "IPSec_ID";
	public final static String FIELD_NETWORK_SECRET = "IPSec_secret";
	public final static String FIELD_NETWORK_USERNAME = "Xauth";
	public final static String FIELD_NETWORK_PASSWORD = "password";
	public final static String FIELD_NETWORK_LASTCONNECT = "lastconnect";

	public final static String FIELD_NETWORK_NICKNAME = "nickname";
	public final static String FIELD_KEY_NAME = "nickname";
	public final static String FIELD_KEY_PRIVATE = "private";
	
	public static final String LOG_TAG = "VPNC";
	private static final String PREFIX = NetworkDatabase.class.getSimpleName() + ":";
	
	public NetworkDatabase(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(LOG_TAG, PREFIX + "onCreate - Start");
		db.execSQL("CREATE TABLE " + TABLE_NETWORKS
				+ " (_id INTEGER PRIMARY KEY, "
				+ FIELD_NETWORK_NICKNAME + " TEXT, "
				+ FIELD_NETWORK_USERNAME + " TEXT, "
				+ FIELD_NETWORK_PASSWORD + " TEXT, "
				+ FIELD_NETWORK_GATEWAY + " TEXT, "
				+ FIELD_NETWORK_ID + " TEXT, "
				+ FIELD_NETWORK_SECRET + " TEXT, "
				+ FIELD_NETWORK_LASTCONNECT + " INTEGER) ");

		Log.d(LOG_TAG, PREFIX + "onCreate - End");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		Log.d(LOG_TAG, PREFIX + "onUpgrade - Start");
		Log.d(LOG_TAG, PREFIX + "registerLocationListener - Start");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NETWORKS);
		onCreate(db);
		Log.d(LOG_TAG, PREFIX + "onUpgrade - End");
	}

	/* Should only be called for new  networks */	
	public long createNetwork(NetworkConnectionInfo connectionInfo) {

		SQLiteDatabase db = this.getWritableDatabase();
		try
		{
			ContentValues values = new ContentValues();
			values.put(FIELD_NETWORK_NICKNAME, connectionInfo.getNetworkName());
			values.put(FIELD_NETWORK_GATEWAY, connectionInfo.getIpSecGateway());
			values.put(FIELD_NETWORK_ID, connectionInfo.getIpSecId());
			values.put(FIELD_NETWORK_SECRET, connectionInfo.getIpSecSecret());
			values.put(FIELD_NETWORK_USERNAME, connectionInfo.getXauth());
			values.put(FIELD_NETWORK_PASSWORD, connectionInfo.getPassword());
			values.put(FIELD_NETWORK_LASTCONNECT, Integer.MAX_VALUE);
			return db.insert(TABLE_NETWORKS, null, values);
		}
		finally
		{
			db.close();
		}
	}

	/* Updates a  network based on the _id  */
	public long updateNetwork(NetworkConnectionInfo connectionInfo) {

		SQLiteDatabase db = this.getWritableDatabase();
		try
		{
			ContentValues values = new ContentValues();
			values.put(FIELD_NETWORK_NICKNAME, connectionInfo.getNetworkName());
			values.put(FIELD_NETWORK_GATEWAY, connectionInfo.getIpSecGateway());
			values.put(FIELD_NETWORK_ID, connectionInfo.getIpSecId());
			values.put(FIELD_NETWORK_SECRET, connectionInfo.getIpSecSecret());
			values.put(FIELD_NETWORK_USERNAME, connectionInfo.getXauth());
			values.put(FIELD_NETWORK_PASSWORD, connectionInfo.getPassword());
			values.put(FIELD_NETWORK_LASTCONNECT, Integer.MAX_VALUE);
			return db.update(TABLE_NETWORKS, values, KEY_ROWID+"="+connectionInfo.getId(), null);
		}
		finally
		{
			db.close();
		}
	}
	
	public List<NetworkConnectionInfo> allNetworks() {
		Log.d(LOG_TAG, PREFIX + "allNetworks - Start");
		List<NetworkConnectionInfo> connectionInfos = new ArrayList<NetworkConnectionInfo>();
		String sortField =  FIELD_NETWORK_LASTCONNECT;
		
		SQLiteDatabase db = this.getReadableDatabase();
		try
		{
			Cursor cursor = db.query(TABLE_NETWORKS, new String[] { KEY_ROWID, FIELD_NETWORK_NICKNAME, FIELD_NETWORK_USERNAME, 
					FIELD_NETWORK_PASSWORD, FIELD_NETWORK_GATEWAY, FIELD_NETWORK_ID, FIELD_NETWORK_SECRET, FIELD_NETWORK_LASTCONNECT},
					null, null, null, null, sortField + " DESC");
			while( cursor.moveToNext() )
			{
				connectionInfos.add( getNetworkConnectionInfo(cursor) );
			}
			cursor.close();
			return connectionInfos;
		}
		finally
		{
			db.close();
		}
	}

	public NetworkConnectionInfo singleNetwork(int id) {
		Log.d(LOG_TAG, PREFIX + "singleNetwork - Start");
		String where = KEY_ROWID + "=" + id;
		
		SQLiteDatabase db = this.getReadableDatabase();
		try
		{
			Cursor cursor = db.query(TABLE_NETWORKS, new String[] { KEY_ROWID, FIELD_NETWORK_NICKNAME, FIELD_NETWORK_USERNAME, 
									FIELD_NETWORK_PASSWORD, FIELD_NETWORK_GATEWAY, FIELD_NETWORK_ID, FIELD_NETWORK_SECRET, FIELD_NETWORK_LASTCONNECT},
									where, null, null, null, null);
			if( cursor.getCount() == 1 )
			{
				cursor.moveToFirst();
				NetworkConnectionInfo connectionInfo = getNetworkConnectionInfo(cursor);
				cursor.close();
				return connectionInfo;
			}
			else
			{
				IllegalStateException e = new IllegalStateException( "Expected 1 row to be returned but instead found number of rows to be: "
						+ cursor.getCount() );
				Log.e( LOG_TAG, e.getMessage(), e );
				cursor.close();
				throw e;
			}
		}
		finally
		{
			db.close();
		}
	}

	private NetworkConnectionInfo getNetworkConnectionInfo(Cursor cursor) {
		NetworkConnectionInfo info = new NetworkConnectionInfo();
		info.setId( cursor.getInt(0) );
		info.setNetworkName( cursor.getString(1) );
		info.setXauth( cursor.getString(2) );
		info.setPassword( cursor.getString(3) );
		info.setIpSecGateway( cursor.getString(4) );
		info.setIpSecId( cursor.getString(5) );
		info.setIpSecSecret( cursor.getString(6) );
		info.setLastConnect( cursor.getInt(7) );
		return info;
	}

	/* id is the rowid of the network you want to delete
 	 * returns the amount of rows deleted, (should be 1)
 	 */
	public long deleteNetwork(long id) {
		Log.d(LOG_TAG, PREFIX + "Deleting network");
		SQLiteDatabase db = this.getWritableDatabase();
		try
		{
			return db.delete(TABLE_NETWORKS, KEY_ROWID + "=" + id, null);
		}
		finally
		{
			db.close();
		}
	}

}

