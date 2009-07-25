package org.codeandroid.vpnc_frontend;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;


public class MonitorServiceImpl extends Service
{
	private MonitorService service = getService();
	private boolean monitoringEnabled = false;
	private static final int monitorInterval = 60;
	

	@Override
	public IBinder onBind(Intent intent)
	{
		Util.debug( "onBind called on MonitorService for intent with action of " + intent.getAction() );
		if( MonitorServiceImpl.class.getName().equals( intent.getAction() ) )
		{
			return (IBinder)service;
		}
		else
		{
			return null;
		}
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		Util.debug( "New instance of MonitorService created" );
		if( getVpnId() != -1 )
		{
			Util.debug( "Found a vpn id, will monitor it" );
			new Thread( new MonitorThread() ).start();
		}
		else
		{
			Util.debug( "No vpn id, service will stop" );
			stopSelf();
		}
	}
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart( intent, startId );
		Util.debug( "onStart of service" );
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Util.debug( "onDestroy of service" );
	}
	
	public MonitorService getService()
	{
		return new MonitorService.Stub()
		{
			public void stopMonitor() throws RemoteException
			{
				Util.debug( "MonitorService.stopMonitor called" );
				monitoringEnabled = false;
				stopSelf();
			}
		};
	}
	
	private void monitor()
	{
		monitoringEnabled = true;
		try
		{
			while( getVpnId() != -1 && monitoringEnabled )
			{
				if( Util.getProcessId() > 0 )
				{
					Util.debug( "Monitor service: We're still connected" );
					Thread.sleep( monitorInterval * 1000 );
				}
				else
				{
					notifyDisconnect();
					stopSelf();
					return;
				}
			}
		}
		catch( InterruptedException e )
		{
			Util.error( "Interrupted while monitoring vpn process", e );
		}
	}

	private int getVpnId()
	{
		return getSharedPreferences( "vpnc", MODE_PRIVATE ).getInt( "connectedVpnId", -1 );
	}
	
	private void notifyDisconnect()
	{
		Util.info( "Monitor service: We've lost connection" );
		NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		Notification disconnectNotification = new Notification();
		disconnectNotification.icon = R.drawable.lost_connection;
		disconnectNotification.flags = Notification.FLAG_AUTO_CANCEL;
		disconnectNotification.vibrate = new long[]{100, 250, 100, 500}; 
		PendingIntent pendingIntent = PendingIntent.getActivity( this, Util.DISCONNECT_NOTIFICATION, new Intent(this, VPNC.class), PendingIntent.FLAG_ONE_SHOT );
		disconnectNotification.setLatestEventInfo( this, getString(R.string.connection_dropped), getString(R.string.connection_dropped_detail), pendingIntent );
		notificationManager.notify(12345, disconnectNotification);
	}
	
	private class MonitorThread implements Runnable
	{
		public void run()
		{
			monitor();
			Util.debug( "Monitor thread will stop running" );
		}
	}
}