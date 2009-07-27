// AIDL interface to MonitorService which monitors the VPN connection

package org.codeandroid.vpnc_frontend;

interface MonitorService
{
    void startMonitor();
    void stopMonitor();
}
