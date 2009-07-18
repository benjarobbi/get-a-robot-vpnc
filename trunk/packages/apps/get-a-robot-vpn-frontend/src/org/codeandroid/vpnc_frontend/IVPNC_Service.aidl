// AIDL interface to the VPNC service that wraps around the native process

package org.codeandroid.vpnc_frontend;

import java.util.Map;

interface IVPNC_Service {
    
    boolean connect(in String gateway, in String id, in String secret, in String xauth, in String password );
    boolean disconnect();
}