package fi.hut.soberit.sensors;

import android.content.Context;
import android.os.Bundle;

public interface DriverConnection {

	public Driver getDriver();
	
	public void bind(Context context);
	
	public void unbind(Context context);
	
	public boolean isConnected();
	
	
	public void sendMessage(int id);

	public void sendMessage(int id, int arg1);
	
	public void sendMessage(int id, int arg1, int arg2);	
	
	public void sendMessage(int id, int arg1, int arg2, Bundle b);
}
