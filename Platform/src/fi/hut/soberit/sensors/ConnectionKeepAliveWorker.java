package fi.hut.soberit.sensors;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class ConnectionKeepAliveWorker implements Runnable {

	private static final long CHECK_FREQUENCY = 5000;
	private List<DriverConnection> connections;
	private Context context;

	private Handler refreshTimeHandler;
	
	public ConnectionKeepAliveWorker(List<DriverConnection> connections, Context context) {
		this.connections = connections;
		this.context = context;
		
		refreshTimeHandler = new Handler();
		 
		refreshTimeHandler.postDelayed(this, CHECK_FREQUENCY);
	}

	@Override
	public void run() {
		try {
		
			for(DriverConnection conn: connections) {
				
				try {
					Log.d((String)context.getClass().getField("TAG").get(String.class), conn.toString());
				} catch (SecurityException e) {

					e.printStackTrace();
				} catch (NoSuchFieldException e) {

					e.printStackTrace();
				} catch (IllegalArgumentException e) {

					e.printStackTrace();
				} catch (IllegalAccessException e) {

					e.printStackTrace();
				}
				
				if (!conn.isServiceConnected()) {
					conn.bind(context);
				}
			}
		} finally {
			refreshTimeHandler.postDelayed(this, CHECK_FREQUENCY);
		}
		
	}
	
	public void stop() {
		refreshTimeHandler.removeCallbacks(this);
	}

	public List<DriverConnection> getConnections() {
		return connections;
	}

	public void setConnections(List<DriverConnection> connections) {
		this.connections = connections;
	}

}
