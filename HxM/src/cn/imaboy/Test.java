package cn.imaboy;

import fi.hut.soberit.sensors.hxm.R;
//import HxMDriver.HxMStreamParser;
//import HxMDriver.SynchronisationRunnable;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import android.widget.Toast;


public class Test extends Activity{

	private static final int REQUEST_ENABLE_BT = 13;
	public String address;

	//protected Thread connectivityThread;
	public Button button;
	public TextView tv;
	  @Override
	    public void onCreate(Bundle savedInstanceState) {
	         super.onCreate(savedInstanceState);
	         setContentView(R.layout.main);
	         button = (Button) findViewById(R.id.update);
	         tv=(TextView)findViewById(R.id.tv);
	    
	       			
		}			
			
		}	
	  

