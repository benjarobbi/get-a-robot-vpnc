package org.codeandroid.vpnc_frontend;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.RemoteException;


public class MonitorServiceImpl extends Service
{
	private MonitorService service;
	private MonitorThread monitorThread;
	private boolean monitoringEnabled = false;
	private static final int monitorInterval = 60;
	
	public MonitorServiceImpl()
	{
		Util.debug( "MonitorServiceImpl.CTR()" );
		service = getService();
	}

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
		Util.debug( "MonitorServiceImpl.onCreate()" );
		try
		{
			service.startMonitor();
		}
		catch( RemoteException e )
		{
			Util.error( "Failed to start monitor service", e );
		}
	}
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart( intent, startId );
		Util.debug( "MonitorServiceImpl.onStart()" );
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Util.debug( "MonitorServiceImpl.onDestroy()" );
	}
	
	public MonitorService getService()
	{
		return new MonitorService.Stub()
		{
			public void startMonitor() throws RemoteException
			{
				Util.debug( "MonitorService.startMonitor called" );
				if( monitorThread == null )
				{
					if( getVpnId() != -1 )
					{
						Util.debug( "Found a vpn id, will monitor it" );
						monitorThread = new MonitorThread();
						new Thread(monitorThread).start();
					}
					else
					{
						Util.debug( "No vpn id, service will stop" );
						stopSelf();
					}
				}
			}
			public void stopMonitor() throws RemoteException
			{
				Util.debug( "MonitorService.stopMonitor called" );
				monitoringEnabled = false;
				monitorThread = null;
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
					Util.debug( "Monitor service will now stop itself" );
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
		Notification disconnectNotification = new Notification( R.drawable.lost_connection, null, System.currentTimeMillis() );
		disconnectNotification.flags = Notification.FLAG_AUTO_CANCEL;
		
		AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
		int ringerMode = audioManager.getRingerMode();
		switch( ringerMode )
		{
			case AudioManager.RINGER_MODE_VIBRATE:
				disconnectNotification.vibrate = new long[]{100, 250, 100, 500};
				break;
				
			case AudioManager.RINGER_MODE_NORMAL:
				disconnectNotification.defaults = Notification.DEFAULT_SOUND;
				break;
				
			case AudioManager.RINGER_MODE_SILENT:
				disconnectNotification.ledARGB = 0xff0000ff;
				disconnectNotification.ledOnMS = 500;
				disconnectNotification.ledOffMS = 3000;
				disconnectNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
				break;

			default:
				break;
		}

		Intent vpncIntent = new Intent(this, VPNC.class);
		vpncIntent.putExtra( this.getClass().getName() + ".referrer", this.getClass().getName() );
		vpncIntent.putExtra( this.getClass().getName() + ".vpnId", getVpnId() );
		PendingIntent pendingIntent = PendingIntent.getActivity( this, Util.DISCONNECT_NOTIFICATION, vpncIntent, PendingIntent.FLAG_ONE_SHOT );
		disconnectNotification.setLatestEventInfo( this, getString(R.string.connection_dropped), getString(R.string.connection_dropped_detail), pendingIntent );
		notificationManager.notify(1, disconnectNotification);
		monitorThread = null;
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