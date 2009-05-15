package org.codeandroid.vpnc_frontend;

/* Android Generic Java */ 
import android.app.Activity;
import android.os.Bundle;
import android.widget.*; 
import android.util.Log; 
import android.view.View;
import android.graphics.Color;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
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

	private String TAG = "VPNC";  
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

		/* Get a list of networks for the spinner */ 
		String NetworkList[] = NetworkList();

		ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this,
        		android.R.layout.simple_spinner_item,
			NetworkList 
			
            	);

		NetworkChoice.setAdapter(spinnerArrayAdapter);

        	NetworkChoice.setOnItemSelectedListener(NetworkChoiceListener);
	
	}

	public void ViewTextBlack(View v) {
		EditText tmp = (EditText) v; 
		tmp.setTextColor(Color.BLACK);
	}

	/* Usability improvement */
	public void onClick(View v) {
		Log.i(TAG, "Field Clicked!");
		EditText tmp = (EditText) v; 
		ViewTextBlack(v);
		tmp.selectAll(); 
	}


	private OnItemSelectedListener NetworkChoiceListener = new OnItemSelectedListener() {
		
		public void onItemSelected(AdapterView parent, View v, int position, long id) {

			int pos = NetworkChoice.getSelectedItemPosition();
			Log.i(TAG, pos + " selected and position " + position + "passed"); 
			JSONToCurrent(GetNetworkByIndex( GetNetworkFromConfig(), pos)); 
		}

		public void onNothingSelected(AdapterView arg0) {}

	};

	private JSONArray GetNetworkFromConfig() {

		try {	
			JSONArray networks = new JSONArray(); 
			networks = ConfigurationSettings.getJSONArray("networks");
			return networks; 
		} catch (Exception e) {
			/* If we mess up, return an empty one ! */ 
		}

		return new JSONArray(); 
	}

	/* Pass In a network get its name */
	private String GetNetworkName (JSONObject network) {

		try {
			if (network.has("name")) {
				return network.getString("name").toString(); 
			}
		}
		catch (Exception e) {
			// FIXME: Shouldnt these do something
		}
		return "Unnamed Network";
	}

	/* Pass it the "network" section of the subconfig */  
	private JSONObject GetNetworkByIndex(JSONArray NetworkList, int Index) {
		Log.i(TAG, "Getting single network from List");

		try {
			return NetworkList.getJSONObject(Index);
		}
		catch (Exception e) {
			/* FIXME: handle it */ 
		}
		return new JSONObject(); 	
	}


	/* FIXME: return network list from loaded json here.  */ 
	private String[] NetworkList() {

		String[] NetworkNames = new String[] { "No networks configured" }; 

		try {
			/* The array of networks */
			JSONArray networks = GetNetworkFromConfig();
			
			NetworkNames = new String[networks.length()];

			/* Iterate through each one, find the name setting*/
			for (int i = 0; i < networks.length(); i++) {
				NetworkNames[i] = GetNetworkName ( GetNetworkByIndex(networks, i) );
			}

		} catch (Exception e) {
			Log.i(TAG, "Failed json parsing!");
		}

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

		Log.i(TAG, "Done loading settings"); 
	}

	/* If there is a variable set, we set it, otherwise leave it blank */
	private void SetEditTextWidget(EditText e, JSONObject n, String Attribute) {
		Log.i(TAG, "Configuring widget"); 	

		try {
			if (n.has(Attribute) ) {
				e.setText(n.getString(Attribute)) ;
				ViewTextBlack(e);
			}
			else {
				e.setText("");
			}
		}
		catch (Exception d) {
			Log.i(TAG, "We are bloody useless!"); 
		}

	}

	/* Here we set the attributes of the UI elements from the json settings */
	private void JSONToCurrent(JSONObject network) {

		try {
			SetEditTextWidget(IPSEC_Gateway, network,  "IPSec gateway");
			SetEditTextWidget(IPSEC_ID,      network,  "IPSec ID");
			SetEditTextWidget(IPSEC_Secret,  network , "IPSec secret");
			SetEditTextWidget(IPSEC_Username,network,  "Xauth"); 
			SetEditTextWidget(IPSEC_Password,network,  "Password");
		}
		catch (Exception e)  {
			/* FIXME: better exception handling */ 
			Log.i(TAG, "Problem Converting json to live widgets:"); 
			e.printStackTrace();
		}

		Log.i(TAG,"Done attempting to setup UI!");
	}

	/* We grab the settings that the user has changed/made */
	private JSONObject CurrentToJSON() {

		try {	
			JSONObject CurrentUI = new JSONObject(); 

			CurrentUI.put("IPSec gateway", IPSEC_Gateway.getText() );
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
