package imaboy.cn;

import java.io.IOException;
import java.io.InputStream;

//import HxMDriver;
import eu.mobileguild.bluetooth.BluetoothConnectionRunnable;
import eu.mobileguild.bluetooth.BluetoothPairingActivity;
import eu.mobileguild.utils.DataTypes;
import eu.mobileguild.utils.VolatileLongValue;
import fi.hut.soberit.sensors.generic.ObservationType;
//import HxMDriver.HxMStreamParser;
//import HxMDriver.SynchronisationRunnable;
//import fi.hut.soberit.hxm.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class text extends Activity{

	private static final int REQUEST_ENABLE_BT = 13;
	private static final int REQUEST_FIND_HXM = 14;
	public String address;
	private int timeout;
	private Handler reconnectHandler;
	private Runnable reconnectRunnable;
	private ObservationType timeStampType;
	private BluetoothConnectionRunnable synchronisationRunnable;
	protected Thread connectivityThread;
	public Button button;
	  @Override
	    public void onCreate(Bundle savedInstanceState) {
	         super.onCreate(savedInstanceState);
	         setContentView(R.layout.test);
	         final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
	         if (adapter == null) {
	 			Toast.makeText(this, "No bluetooth", Toast.LENGTH_LONG);
	 			
	 		}
	         if (!adapter.isEnabled()) {
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				}
	        // Intent dialogIntent = new Intent(this, BluetoothPairingActivity.class);
			// startActivityForResult(dialogIntent, REQUEST_FIND_HXM);
			 
	         button = (Button) findViewById(R.id.update);
	         button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					findHxMDevice();
				}
			});		
			reconnectHandler = new Handler();
			reconnectRunnable = new Runnable() {

				@Override
				public void run() {
					
						//start seperate thread work with bluetooth
						synchronisationRunnable = new SynchronisationRunnable(
								address, timeout,
								timeStampType != null
								);
						
						connectivityThread = new Thread(synchronisationRunnable);
						connectivityThread.start();
					}
			};
			

	  }
	    @Override
	    public void onActivityResult(int requestCode, int resultCode, Intent data)  {    	
	    	if (resultCode != Activity.RESULT_OK) {
	    		return;
	    	}
	    	switch(requestCode) {
    		case REQUEST_ENABLE_BT:
    			findHxMDevice();
    			break;
    		case REQUEST_FIND_HXM:
    			this.address = data.getStringExtra(BluetoothPairingActivity.INTENT_DEVICE_ADDRESS);
    			this.timeout = 1000;
    			//final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
    			reconnectHandler.postDelayed(reconnectRunnable, 0);
    			break;
	    	}
	    }	
	    
	
		private void findHxMDevice() {
			Intent dialogIntent = new Intent(this, BluetoothPairingActivity.class);
			startActivityForResult(dialogIntent, REQUEST_FIND_HXM);
		}
		
		class HxMStreamParser {
			
			private static final int RECORD_LENGTH = 60;
			private static final int HXM_PULSE_BYTE = 12;		
			public static final int HXM_MAX_STRIDES = 128;
			public static final int HXM_TIME_STAMP_START = 14;
			public static final int HXM_TIME_STAMP_END = 42;
			private InputStream stream;
			private byte[] buffer = new byte[RECORD_LENGTH];
			private int[] results= new int[HXM_TIME_STAMP_END-HXM_TIME_STAMP_START];
			private int bufferPos = 0;
	
			public HxMStreamParser(InputStream stream) {
				this.stream = stream;
			}
			
			public void read() throws IOException {
				int tmp = stream.read(buffer, bufferPos, RECORD_LENGTH - bufferPos);
				
				bufferPos += tmp;
			}

			public boolean isRecordComplete() {
				return bufferPos == RECORD_LENGTH;
			}
			
			public void reset() {
				bufferPos = 0;
			}
			
			public int getPulse() {
				
				return DataTypes.readUnsignedByte(HXM_PULSE_BYTE, buffer);
			}
			public int[] getTimestamp(){
				for (int i=HXM_TIME_STAMP_START;i<HXM_TIME_STAMP_END;i++){
					
					results[i]=DataTypes.readUnsignedWord(i, buffer);
				}
				return results;
			}
			}
class SynchronisationRunnable extends BluetoothConnectionRunnable {
			private boolean searchStamp;
			private HxMStreamParser parser;
			private long lastCompleteOrderReceived = -1;

			//private VolatileLongValue volatileStrides = new VolatileLongValue(TIMEOUT_FOR_STRIDES_STEP);
			
			public SynchronisationRunnable(String address, int timeout, boolean searchTimestamp) {
				super(address, timeout);
				
				this.address = address;
				this.timeout = timeout;
			
				this.searchStamp = searchTimestamp;
			}

			@Override
			protected void onConnect() throws IOException {
				parser = new HxMStreamParser(socket.getInputStream());
			}

			@Override
			protected void read() throws IOException, InterruptedException {
				
				final long now = System.currentTimeMillis();
				
				if (lastCompleteOrderReceived != -1 && now - lastCompleteOrderReceived  > timeout) {
					Log.d(TAG, String.format("last: %d, timeout: %d", now - lastCompleteOrderReceived, timeout));
					this.setStopFlag(true);
					return;
				}
				parser.read();
				
				if (!parser.isRecordComplete()) {
					return;	
				}
				parser.reset();
				
				lastCompleteOrderReceived = now;
				
				if (searchStamp) {
					Log.d(TAG, "timeStamp" + parser.getTimestamp());
				}
				
			}
			
		}			
			
		}	
	  

