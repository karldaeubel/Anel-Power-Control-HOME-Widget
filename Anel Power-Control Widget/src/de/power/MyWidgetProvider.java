package de.power;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

import de.control.UDPSender;
import de.model.UDP;
import de.model.User;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {

	public final static String ACTION_WIDGET_RECIEVER = "ACTION_WIDGET_RECIEVER";
	public final static String ACTION_WIDGET_UPDATE = "ACTION_WIDGET_UPDATE";
	
	private final String outlet_number = "outlet_number";
	
	private UDP udp;
	
	private boolean[] outlets;
	
	public class UDPReciever extends Thread{

		UDP udp;
		
		DatagramSocket recieveSocket;
		DatagramPacket recieve;
		
		public UDPReciever() {
			udp = new UDP();
			try {
				recieveSocket = new DatagramSocket(udp.getPortInput());
				recieveSocket.setSoTimeout(500);
			}catch (SocketException e) {
				e.printStackTrace();
			}
		}
		
		public UDPReciever(UDP p) {
			udp = p;
			try {
				recieveSocket = new DatagramSocket(udp.getPortInput());
				recieveSocket.setSoTimeout(500);
			}catch (SocketException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			byte[] in = null;
			recieve = null;
			in = new byte[512];
			recieve = new DatagramPacket(in, in.length);
			while(!isInterrupted()) {
		        	
				try {
					//Log.i("recieve!°°°°°!!", "läuft immer noch?");
					recieveSocket.receive(recieve);
					// display response
			        String ausgabe = new String(recieve.getData());
			        final String[] alles = ausgabe.split(":");

			        if(alles.length >= 8) {
						outlets[0] = alles[6].endsWith("1") ? true : false;
						outlets[1] = alles[7].endsWith("1") ? true : false;
						outlets[2] = alles[8].endsWith("1") ? true : false;
		        	}			        
				}catch (IOException e) {
				}
			}
			recieveSocket.close();
		}
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		outlets = new boolean[3];
		
		InetAddress i = null;
		
		try {
			i = InetAddress.getByName("192.168.1.5");
		}catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		udp = new UDP(new User("admin", "anel"), 4444, 4445, i);
		
		Log.i("my massage", "lol");
		for (int widgetId : appWidgetIds) {

			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
			
			Intent outlet1 = new Intent(context, MyWidgetProvider.class);
			outlet1.setAction(ACTION_WIDGET_RECIEVER);
			outlet1.putExtra(outlet_number, 1);
			
			PendingIntent outlet1PendingIntent1 = PendingIntent.getBroadcast(context, 0, outlet1, 0);
			
			Intent outlet2 = new Intent(context, MyWidgetProvider.class);
			outlet2.setAction(ACTION_WIDGET_RECIEVER);
			outlet2.putExtra(outlet_number, 2);
			
			PendingIntent outlet1PendingIntent2 = PendingIntent.getBroadcast(context, 0, outlet2, 0);
						
			Intent outlet3 = new Intent(context, MyWidgetProvider.class);
			outlet3.setAction(ACTION_WIDGET_RECIEVER);
			outlet3.putExtra(outlet_number, 3);
			
			PendingIntent outlet1PendingIntent3 = PendingIntent.getBroadcast(context, 0, outlet3, 0);
			
			Intent update = new Intent(context, MyWidgetProvider.class);
			update.setAction(ACTION_WIDGET_UPDATE);
			
			PendingIntent updatePendingIntent = PendingIntent.getBroadcast(context, 0, update, 0);
			
			views.setOnClickPendingIntent(R.id.outlet1, outlet1PendingIntent1);
			views.setOnClickPendingIntent(R.id.outlet2, outlet1PendingIntent2);
			views.setOnClickPendingIntent(R.id.outlet3, outlet1PendingIntent3);
			views.setOnClickPendingIntent(R.id.update, updatePendingIntent);
			
			appWidgetManager.updateAppWidget(widgetId, views);
		}
	}
	
	//TODO korrekte übernahme der UDP daten
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equalsIgnoreCase(ACTION_WIDGET_RECIEVER)) {
			int outlet = intent.getIntExtra(outlet_number, 0);
			
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
			
			UDPReciever r = new UDPReciever(udp);
			r.start();
			UDPSender s = new UDPSender(udp);
			
			switch(outlet) { 
				case 0:
					
					break;
				case 1:
					outlets[0] = !outlets[0];
					s.switchOutlet(1, outlets[0]);
					break;
				case 2:
					outlets[1] = !outlets[1];
					s.switchOutlet(2, outlets[1]);
					break;
				case 3:
					outlets[2] = !outlets[2];
					s.switchOutlet(3, outlets[2]);
					break;
			}
			r.interrupt();
			while(!r.isInterrupted()) {
				
			}
		}else if(intent.getAction().equalsIgnoreCase(ACTION_WIDGET_UPDATE)) {
			UDPSender s = new UDPSender(udp);
			
		}
		super.onReceive(context, intent);
	}
}

