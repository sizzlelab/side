package fi.hut.soberit.sensors.hxm;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

//import HxMDriver;
import eu.mobileguild.bluetooth.BluetoothConnectionRunnable;
import eu.mobileguild.bluetooth.BluetoothPairingActivity;
import eu.mobileguild.utils.DataTypes;
import eu.mobileguild.utils.VolatileLongValue;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.hxm.R;
//import HxMDriver.HxMStreamParser;
//import HxMDriver.SynchronisationRunnable;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class Test extends Activity{

	private static final int REQUEST_ENABLE_BT = 13;
	private static final int REQUEST_FIND_HXM = 14;
	public final String TAG =  this.getClass().getSimpleName();
	public final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
	public String address;
	private int timeout;
	private Handler reconnectHandler;
	private Runnable reconnectRunnable;
	private ObservationType timeStampType;
	private BluetoothConnectionRunnable synchronisationRunnable;
	protected Thread connectivityThread;
	public Button button;
	public TextView tv;
	public ConnectedThread connectThread;
	public BluetoothDevice device = null;
	public BluetoothSocket socket;
	  @Override
	    public void onCreate(Bundle savedInstanceState) {
	         super.onCreate(savedInstanceState);
	         setContentView(R.layout.main);
	         final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
	         if (adapter == null) {
	 			Toast.makeText(this, "No bluetooth", Toast.LENGTH_LONG);
	 			
	 		}
	         if (!adapter.isEnabled()) {
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				}
			 tv= (TextView) findViewById(R.id.tv);
	         button = (Button) findViewById(R.id.update);
	         
	         Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
	      // If there are paired devices
	      if (pairedDevices.size() > 0) {
	          // Loop through paired devices
	          for (BluetoothDevice device : pairedDevices) {
	              // Add the name and address to an array adapter to show in a ListView
	             String s= device.getName() + "\n" + device.getAddress();           
	             
	          }
	          
	      }
	      try {
	      device = adapter.getRemoteDevice(address);
          socket = device.createRfcommSocketToServiceRecord(MY_UUID);
	      connectThread = new ConnectedThread(socket);
	      
	      }catch(IOException e) {
	    	  Log.d(TAG, "Fail connecting");
			} 
	      Toast.makeText(getApplicationContext(), address,Toast.LENGTH_SHORT).show();
	         button.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					//findHxMDevice();
					reconnectHandler = new Handler();
					reconnectRunnable = new Runnable() {

						@Override
						public void run() {
							synchronisationRunnable = new SynchronisationRunnable(
									address, timeout,
									timeStampType != null
									);
							Toast.makeText(getApplicationContext(), address,Toast.LENGTH_SHORT).show();
							connectivityThread = new Thread(synchronisationRunnable);
							connectivityThread.start();
					
							//reconnectHandler.postDelayed(this, 1000);
							}
					};
					reconnectHandler.postDelayed(reconnectRunnable, 1000);
				}
			});		
			
			

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
		
private class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
 
    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
 
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }
 
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }
 
    public void run() {
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()
 
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                // Send the obtained bytes to the UI Activity
                reconnectHandler.obtainMessage(1, bytes, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                break;
            }
        }
    }
 
    /* Call this from the main Activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }
 
    /* Call this from the main Activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
		}	
	  

