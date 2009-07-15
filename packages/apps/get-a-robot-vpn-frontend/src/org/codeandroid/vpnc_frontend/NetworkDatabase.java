package org.codeandroid.vpnc_frontend;

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
	public long createNetwork(String nickname, String gateway, String id, String secret, String username, String password) {

		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(FIELD_NETWORK_NICKNAME, nickname);
		values.put(FIELD_NETWORK_GATEWAY, gateway);
		values.put(FIELD_NETWORK_ID, id);
		values.put(FIELD_NETWORK_SECRET, secret);
		values.put(FIELD_NETWORK_USERNAME, username);
		values.put(FIELD_NETWORK_PASSWORD, password);
		values.put(FIELD_NETWORK_LASTCONNECT, Integer.MAX_VALUE);
		return db.insert(TABLE_NETWORKS, null, values);
	}

	/* Updates a  network based on the _id  */
	public long updateNetwork(int _id, String nickname, String gateway, String id, String secret, String username, String password) {

		SQLiteDatabase db = this.getReadableDatabase();

		ContentValues values = new ContentValues();
		values.put(FIELD_NETWORK_NICKNAME, _id);
		values.put(FIELD_NETWORK_NICKNAME, nickname);
		values.put(FIELD_NETWORK_GATEWAY, gateway);
		values.put(FIELD_NETWORK_ID, id);
		values.put(FIELD_NETWORK_SECRET, secret);
		values.put(FIELD_NETWORK_USERNAME, username);
		values.put(FIELD_NETWORK_PASSWORD, password);
		values.put(FIELD_NETWORK_LASTCONNECT, Integer.MAX_VALUE);
		return db.update(TABLE_NETWORKS,values,KEY_ROWID+"="+_id, null);
	}
	
	public Cursor allNetworks() {
		Log.d(LOG_TAG, PREFIX + "allNetworks - Start");
		String sortField =  FIELD_NETWORK_LASTCONNECT;
		
		SQLiteDatabase db = this.getReadableDatabase();
		return db.query(TABLE_NETWORKS, new String[] { "_id", FIELD_NETWORK_NICKNAME,
				FIELD_NETWORK_USERNAME, FIELD_NETWORK_GATEWAY, FIELD_NETWORK_ID,
				FIELD_NETWORK_SECRET, FIELD_NETWORK_LASTCONNECT },
				null, null, null, null, sortField + " DESC");
	}

	public Cursor singleNetwork(int id) {
		Log.d(LOG_TAG, PREFIX + "singleNetwork - Start");
		String sortField =  FIELD_NETWORK_LASTCONNECT;
		String where = KEY_ROWID + "=" + id;
		
		SQLiteDatabase db = this.getReadableDatabase();
		return db.query(TABLE_NETWORKS, new String[] { "_id", FIELD_NETWORK_NICKNAME,
				FIELD_NETWORK_USERNAME, FIELD_NETWORK_PASSWORD,
				FIELD_NETWORK_GATEWAY, FIELD_NETWORK_ID,
				FIELD_NETWORK_SECRET, FIELD_NETWORK_LASTCONNECT },
				where, null, null, null, sortField + " DESC");
	}

	/* id is the rowid of the network you want to delete
 	 * returns the amount of rows deleted, (should be 1)
 	 */
	public long deleteNetwork(long id) {
		Log.d(LOG_TAG, PREFIX + "Deleting network");
		SQLiteDatabase db = this.getReadableDatabase();
		return db.delete(TABLE_NETWORKS, KEY_ROWID + "=" + id, null);
	}

}

