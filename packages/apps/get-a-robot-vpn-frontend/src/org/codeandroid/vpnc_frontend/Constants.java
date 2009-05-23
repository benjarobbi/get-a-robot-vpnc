
package org.codeandroid.vpnc_frontend;

import android.provider.BaseColumns;

public interface Constants extends BaseColumns {
   public static final String TABLE_NAME = "networks";

   // Columns in the Events database
   public static final String NETWORK_NAME = "nickname";
   public static final String LAST_CONNECTION = "lastconnect";

}
