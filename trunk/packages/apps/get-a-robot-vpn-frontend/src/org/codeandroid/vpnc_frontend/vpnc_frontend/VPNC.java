package org.codeandroid.vpnc_frontend;

/* Android Generic Java */ 
import android.app.Activity;
import android.os.Bundle;
import android.widget.*; 
import android.util.Log; 
import android.view.View;
import android.graphics.Color;
import android.view.View.OnClickListener;

/* For writing settings to disk */
import java.io.FileOutputStream; 
import java.io.PrintStream; 
import org.json.JSONObject; 

/* For managing and setting up the connection */
import org.codeandroid.vpnc_frontend.VPNConnectionManager;


public class VPNC extends Activity implements OnClickListener
{

	private String APPNAME = "VPNC";  
	private String DATADIR = "/data/data/org.codeandroid.vpnc_frontend/"; 

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

    	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
    	
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

		ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this,
        		android.R.layout.simple_spinner_item,
            	new String[] { "Network 1", "Network 2", "Dog" });

		NetworkChoice.setAdapter(spinnerArrayAdapter);

	}

	/* Usability improvement */
	public void onClick(View v) {

		Log.i(APPNAME, "Field Clicked!");
		EditText tmp = (EditText) v; 
		tmp.setTextColor(Color.BLACK);
		tmp.selectAll(); 

		getCurrentToJSON(); 

	}

	/* We grab the settings that the user has changed/made */
	private void getCurrentToJSON() {
		/* default path is /data/data/org.codeandroid.something */
	
		Log.i(APPNAME, "Writing settings to JSON"); 
		try {		


			FileOutputStream out; // declare a file output object
			PrintStream p; // declare a print stream object

			/* FIXME:  There has to be a better way, pathbuilder or something */
			String SettingsFile = DATADIR.concat("/conf/something.json"); 
			Log.i(APPNAME, "Attempting to write to " + SettingsFile ); 
			out = new FileOutputStream(SettingsFile);
			p = new PrintStream( out );

			JSONObject JSONSettings = new JSONObject().put("N", "M"); 

			JSONSettings.put("IPSec Gateway", IPSEC_Gateway.getText() );
			JSONSettings.put("IPSec ID", IPSEC_ID.getText() );
			JSONSettings.put("IPSec secret", IPSEC_Secret.getText() );
			JSONSettings.put("Xauth", IPSEC_Username.getText() );

			p.print( JSONSettings.toString() );
			p.close(); 

		} catch (Exception e) {
			Log.i(APPNAME, "Can't write to json file"); 

		}

	}

}
