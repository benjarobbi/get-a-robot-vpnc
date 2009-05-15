package org.codeandroid.vpnc_frontend;

/* Android Generic Java */ 
import android.app.Activity;
import android.os.Bundle;
import android.widget.*; 
import android.util.Log; 
import android.view.View;
import android.graphics.Color;
import android.view.View.OnClickListener;
import android.content.Context;

/* For reading/writing settings to disk */
import org.json.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


/* For managing and setting up the connection */
import org.codeandroid.vpnc_frontend.VPNConnectionManager;


public class VPNC extends Activity implements OnClickListener
{

	private String APPNAME = "VPNC";  
	private String DATADIR = "/data/data/org.codeandroid.vpnc_frontend/"; 
	public static final String SETTINGS_FILE = "networks.json";

	/* UI Elements*/
	Spinner NetworkChoice;
	EditText IPSEC_Gateway;
	EditText IPSEC_ID;
	EditText IPSEC_Secret;
	EditText IPSEC_Username;
	EditText IPSEC_Password; 
	Button ConnectButton;	

	/* Not sure what you call this */
	VPNConnectionManager ConnectionManager = new VPNConnectionManager(); 
	
	/* Configuration State */
	JSONObject ConfigurationSettings;

    	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

    		/* Not sure if we should do this first or last, or where */ 
		LoadJSONSettings();

		NetworkChoice = (Spinner) findViewById(R.id.network_chooser); 

		IPSEC_Gateway = (EditText) findViewById(R.id.IPSEC_Gateway); 
		IPSEC_Gateway.setOnClickListener(this);

		IPSEC_ID = (EditText) findViewById(R.id.IPSEC_ID); 
		IPSEC_ID.setOnClickListener(this);

		IPSEC_Secret = (EditText) findViewById(R.id.IPSEC_Secret); 
		IPSEC_Secret.setOnClickListener(this);

		IPSEC_Username = (EditText) findViewById(R.id.IPSEC_Username); 
		IPSEC_Username.setOnClickListener(this);

		IPSEC_Password = (EditText) findViewById(R.id.IPSEC_Password); 
		IPSEC_Password.setOnClickListener(this);

		ConnectButton = (Button) findViewById(R.id.Connect);
		ConnectButton.setOnClickListener(ConnectionManager); 

		/* Open the JSON file, lets get a list of networks */ 

		String NetworkList[] = NetworkList();

		ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this,
        		android.R.layout.simple_spinner_item,
			NetworkList 
			
            	);

		NetworkChoice.setAdapter(spinnerArrayAdapter);
	}

	/* Usability improvement */
	public void onClick(View v) {

		Log.i(APPNAME, "Field Clicked!");
		EditText tmp = (EditText) v; 
		tmp.setTextColor(Color.BLACK);
		tmp.selectAll(); 

		Log.i(APPNAME ,"Current JSON data is " + getCurrentToJSON() );

	}

	/* FIXME: return network list from loaded json here.  */ 
	private String[] NetworkList() {

		String[] NetworkNames = new String[] { "No networks configured" }; 

		try {
			Log.i(APPNAME, "Config Names: " + ConfigurationSettings.names() ) ;
			/* The array of networks */
			JSONArray networks = ConfigurationSettings.getJSONArray("networks");
			JSONObject o;
			
			NetworkNames = new String[networks.length()];

			/* Iterate through each one, find the name setting*/
			for (int i = 0; i < networks.length(); i++) {
				o =  networks.getJSONObject(i);
				if (o.has("name")) {
					NetworkNames[i] = o.getString("name").toString(); 
				}
			}

		} catch (Exception e) {
			Log.i(APPNAME, "Failed json parsing!");
		}

		// return new String[] { "Network 1", "Network 2", "Create New Network" };
		return NetworkNames;
		
	}

	/* FIXME: Actually load JSON from disk */ 
	private void LoadJSONSettings() {

		FileInputStream fin = null; 
		InputStreamReader irdr = null;

		try {			

			fin = openFileInput(SETTINGS_FILE);

			irdr = new InputStreamReader(fin); // promote

			int size = (int) fin.getChannel().size();
			char[] data = new char[size]; // allocate char array of right

			irdr.read(data, 0, size); // read into char array
			irdr.close();

			String contents = new String(data);
			
			ConfigurationSettings = new JSONObject(contents); // WORKING  
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (irdr != null) {
					irdr.close();
				}
				if (fin != null) {
					fin.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Log.i(APPNAME, "Done loading settings"); 
	}

	/* We grab the settings that the user has changed/made */
	private JSONObject getCurrentToJSON() {

		try {	
			JSONObject CurrentUI = new JSONObject(); 

			CurrentUI.put("IPSec Gateway", IPSEC_Gateway.getText() );
			CurrentUI.put("IPSec ID", IPSEC_ID.getText() );
			CurrentUI.put("IPSec secret", IPSEC_Secret.getText() );
			CurrentUI.put("Xauth", IPSEC_Username.getText() );

			return CurrentUI;
		}
		catch (Exception e) {
			/* FIXME: buggered up somehow , show user let them deal with it */
			return new JSONObject();
		}
	}

}
