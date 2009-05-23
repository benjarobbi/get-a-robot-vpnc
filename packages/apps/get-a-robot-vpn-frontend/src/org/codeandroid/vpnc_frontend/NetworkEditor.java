package org.codeandroid.vpnc_frontend;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener; 
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import org.codeandroid.vpnc_frontend.NetworkDatabase;

public class NetworkEditor extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener {
	
    public static final String LOG_TAG = "VPNC";
    private static final String PREFIX = NetworkEditor.class.getSimpleName() + ":";
	
	public class CursorPreferenceHack implements SharedPreferences {
		
		protected final SQLiteDatabase db;
		protected final String table;
		protected final int id;

		protected Map<String, String> values = new HashMap<String, String>();
		
		public CursorPreferenceHack(SQLiteDatabase db, String table, int id) {
			this.db = db;
			this.table = table;
			this.id = id;
			
			this.cacheValues();
			
		}
		
		protected void cacheValues() {
			
			Cursor cursor = db.query(table, null, "_id = ?",
					new String[] { Integer.toString(id) }, null, null, null);

			cursor.moveToFirst();
			
			for(int i = 0; i < cursor.getColumnCount(); i++) {
				String key = cursor.getColumnName(i);
				String value = cursor.getString(i);
				values.put(key, value);
			}
			
			cursor.close();

			Log.d(LOG_TAG, PREFIX + "cacheValues - End");
			
		}
		
		public boolean contains(String key) {
			return values.containsKey(key);
		}
		
		public class Editor implements SharedPreferences.Editor {
			
			public ContentValues update = new ContentValues();
			
			public SharedPreferences.Editor clear() {
				Log.d(this.getClass().toString(), "clear()");
				update = new ContentValues();
				Log.d(LOG_TAG, PREFIX + "SharedPreferences.Editor - End");
				return this;
			}

			public boolean commit() {
				Log.d(this.getClass().toString(), "commit() changes back to database");
				db.update(table, update, "_id = ?", new String[] { Integer.toString(id) });
				
				// make sure we refresh the parent cached values
				cacheValues();
				
				// and update any listeners
				for(OnSharedPreferenceChangeListener listener : listeners) {
					listener.onSharedPreferenceChanged(CursorPreferenceHack.this, null);
				}
				Log.d(LOG_TAG, PREFIX + "commit()- End");
				return true;
			}

			public android.content.SharedPreferences.Editor putBoolean(String key, boolean value) {
				return this.putString(key, Boolean.toString(value));
			}

			public android.content.SharedPreferences.Editor putFloat(String key, float value) {
				return this.putString(key, Float.toString(value));
			}

			public android.content.SharedPreferences.Editor putInt(String key, int value) {
				return this.putString(key, Integer.toString(value));
			}

			public android.content.SharedPreferences.Editor putLong(String key, long value) {
				return this.putString(key, Long.toString(value));
			}

			public android.content.SharedPreferences.Editor putString(String key, String value) {
				Log.d(this.getClass().toString(), String.format("Editor.putString(key=%s, value=%s)", key, value));
				update.put(key, value);
				Preference a = (Preference) findPreference(key);
				a.setSummary(value);
				Log.d(LOG_TAG, PREFIX + "android.content.SharedPreferences.Editor putString - End");
				return this;
			}

			public android.content.SharedPreferences.Editor remove(String key) {
				Log.d(this.getClass().toString(), String.format("Editor.remove(key=%s)", key));
				update.remove(key);
				return this;
			}
			
		}


		public Editor edit() {
			Log.d(LOG_TAG, PREFIX + "edit()");
			Log.d(this.getClass().toString(), "edit()");
			return new Editor();
		}

		public Map<String, ?> getAll() {
			Log.d(LOG_TAG, PREFIX + "getAll");
			return values;
		}

		public boolean getBoolean(String key, boolean defValue) {
			Log.d(LOG_TAG, PREFIX + "getBoolean");
			return Boolean.valueOf(this.getString(key, Boolean.toString(defValue)));
		}

		public float getFloat(String key, float defValue) {
			Log.d(LOG_TAG, PREFIX + "getFloat");
			return Float.valueOf(this.getString(key, Float.toString(defValue)));
		}

		public int getInt(String key, int defValue) {
			Log.d(LOG_TAG, PREFIX + "getInt");
			return Integer.valueOf(this.getString(key, Integer.toString(defValue)));
		}

		public long getLong(String key, long defValue) {
			return Long.valueOf(this.getString(key, Long.toString(defValue)));
		}

		public String getString(String key, String defValue) {
			Log.d(this.getClass().toString(), String.format("getString(key=%s, defValue=%s)", key, defValue));
			if(!values.containsKey(key)) return defValue;
			return values.get(key);
		}
		
		public List<OnSharedPreferenceChangeListener> listeners = new LinkedList<OnSharedPreferenceChangeListener>();

		public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
			listeners.add(listener);
			Log.d(LOG_TAG, PREFIX + "registerOnSharedPreferenceChangeListener - End");
		}

		public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
			listeners.remove(listener);
			Log.d(LOG_TAG, PREFIX + "unregisterOnSharedPreferenceChangeListener - End");
		}
		
	}
	
	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		Log.d(this.getClass().toString(), String.format("getSharedPreferences(name=%s)", name));
		Log.d(LOG_TAG, PREFIX + "getSharedPreferences - End");
		return this.pref;
	}
	
	public CursorPreferenceHack pref;
	public int _id = -1;
	NetworkDatabase db;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		db = new NetworkDatabase(this);
		_id = this.getIntent().getIntExtra(Intent.EXTRA_TITLE, -1);
		
		this.pref = new CursorPreferenceHack(db.getWritableDatabase(), db.TABLE_NETWORKS, _id);
		this.pref.registerOnSharedPreferenceChangeListener(this);
		this.addPreferencesFromResource(R.xml.vpnc_profile);

		/* Hook up save */ 
		Preference p = (Preference) findPreference("save");
		p.setOnPreferenceClickListener(this);
	
		this.updateSummaries();
		Log.d(LOG_TAG, PREFIX + "onCreate - End");
	}
	
	public void updateSummaries() {
		// for all text preferences, set hint as current database value
		for(String key : this.pref.values.keySet()) {
			Preference pref = this.findPreference(key);
			if(pref == null) continue;
			if(pref instanceof CheckBoxPreference) continue;
			pref.setSummary(this.pref.getString(key, ""));
		}
		Log.d(LOG_TAG, PREFIX + "updateSummaries - End");
	}

	/* Whenever anything is changed, we update , no save button */
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.i("TAG", "Shared pref changed, does anyone care");
	}

	private String GetSummary(String key) { 
		Log.d(LOG_TAG, PREFIX + "GetSummary");
		String S = ((Preference) findPreference(key)).getSummary().toString();
		return S; 
	}

	@Override
  	public boolean onPreferenceClick  (Preference preference)  {
			String nickname = GetSummary("nickname");
			String IPSec_gateway = GetSummary("IPSec_gateway");
			String IPSec_ID = GetSummary("IPSec_ID");
			String IPSec_secret = GetSummary("IPSec_secret");
			String Xauth = GetSummary("Xauth");
			String password = GetSummary("password");

			if (_id == -1) {
			db.createNetwork(this.pref.db, nickname, IPSec_gateway, 
							IPSec_ID, IPSec_secret, Xauth, password);
			}
			else {
				db.updateNetwork(this.pref.db, _id, nickname, IPSec_gateway, 
							IPSec_ID, IPSec_secret, Xauth, password);
			}
			
			// NetworkEditor.this.setResult(1, "done updating");
  			finish();
  			Log.d(LOG_TAG, PREFIX + "onPreferenceClick - End");
			return true; 
	}

}

