package fi.hut.soberit.sensors;

import android.content.Context;
import android.os.Bundle;

public interface DriverConnection {

	public Driver getDriver();
	
	public String getDriverAction();
	
	public void bind(Context context);
	
	public void unbind(Context context);
	
	public boolean isConnected();
	
	public void sendStartConnecting(String address);

}
