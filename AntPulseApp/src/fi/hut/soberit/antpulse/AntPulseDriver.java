package fi.hut.soberit.antpulse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import android.widget.Toast;

import com.dsi.ant.AntDefine;
import com.dsi.ant.AntInterface;
import com.dsi.ant.AntInterfaceIntent;
import com.dsi.ant.AntMesg;
import com.dsi.ant.exception.AntInterfaceException;

import eu.mobileguild.utils.LittleEndian;
import fi.hut.soberit.sensors.BroadcastingService;
import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.R;
import fi.hut.soberit.sensors.generic.GenericObservation;
import fi.hut.soberit.sensors.generic.ObservationKeyname;
import fi.hut.soberit.sensors.generic.ObservationType;


public class AntPulseDriver extends BroadcastingService {

	public static final String TAG = AntPulseDriver.class.getSimpleName();

	public static final String ACTION = AntPulseDriver.class.getName();
	
	public static final String HEART_BEAT = "pulse";

	public static String UNIT = "bpm";

	private ObservationType typePulse;
	
	/** Pair to any device. */
	private static final short WILDCARD = 0;

	protected byte proximityThreshold = 0;
	
	// ANT Channels
	/** The ANT channel for the HRM. */
	static final byte HRM_CHANNEL = (byte) 0;
	
	private boolean hasClaimedAntInterface;

	private IntentFilter statusIntentFilter;

	private IntentFilter messageIntentFilter;

	private AntInterface antServiceListener;

	private boolean antServiceConnected = true;

	private boolean enabling = true;

	protected short deviceNumberHRM;


	private AntInterface.ServiceListener mAntServiceListener = new AntInterface.ServiceListener() {

		public String TAG = "AntInterface.ServiceListener";

		public void onServiceConnected() {
			Log.d(TAG, "mAntServiceListener onServiceConnected()");

			antServiceConnected = true;

			// Need to enable the ANT radio, now the service is connected
			try {

				Log.d(TAG, "past mEnabling " + enabling);

				hasClaimedAntInterface = antServiceListener
						.hasClaimedInterface();

				if (hasClaimedAntInterface) {
					registerReceiver(mAntMessageReceiver, messageIntentFilter);
				} else {
					hasClaimedAntInterface = antServiceListener
							.claimInterface();
				}

				if (!hasClaimedAntInterface) {
					try {
						antServiceListener
								.requestForceClaimInterface(getResources()
										.getString(R.string.app_name));
					} catch (NotFoundException e) {
						e.printStackTrace();
					} catch (AntInterfaceException e) {
						e.printStackTrace();
					}
				}
			} catch (AntInterfaceException e) {
				Log.d(TAG, "error in communication with ANT radio " + e);

				antError();
			}

			Log.d(TAG,
					"mAntServiceListener Displaying icons only if radio enabled");
		}

		public void onServiceDisconnected() {
			Log.d(TAG, "mAntServiceListener onServiceDisconnected()");

			antServiceConnected = false;
			enabling = false;

			if (hasClaimedAntInterface) {
				unregisterReceiver(mAntMessageReceiver);
			}

		}
	};

	protected boolean startAntConnection = false;

	private final BroadcastReceiver mAntStatusReceiver = new BroadcastReceiver() {
		public String TAG = "mAntStatusReceiver";

		public void onReceive(Context context, Intent intent) {
			String ANTAction = intent.getAction();

			Log.d(TAG, "enter onReceive: " + ANTAction);
			if (ANTAction.equals(AntInterfaceIntent.ANT_ENABLED_ACTION)) {
				Log.i(TAG, "onReceive: ANT ENABLED");

				try {
					Log.d(TAG, "hasClaimedAntInterface? " + hasClaimedAntInterface);
					Log.d(TAG, "isEnabled? " + antServiceListener.isEnabled());
					Log.d(TAG, "startAntConnection? " + startAntConnection);
					
					if (hasClaimedAntInterface && antServiceListener.isEnabled() && startAntConnection) {
						try {
							registerReceiver(mAntMessageReceiver,
									messageIntentFilter);

							Log.d(TAG, "starting connection");
							startAntConnection = false;
							setupAntConnection();
						} catch (AntInterfaceException e) {
							Log.d(TAG, "-", e);
						}
					}
				} catch (AntInterfaceException e) {
					Log.d(TAG, "error - ", e);
				}
				
				enabling = false;
			} else if (ANTAction.equals(AntInterfaceIntent.ANT_DISABLED_ACTION)) {
				Log.i(TAG, "onReceive: ANT DISABLED");

				enabling = false;

			} else if (ANTAction.equals(AntInterfaceIntent.ANT_RESET_ACTION)) {
				Log.d(TAG, "onReceive: ANT RESET");

			} else if (ANTAction.equals(AntInterfaceIntent.ANT_INTERFACE_CLAIMED_ACTION)) {
				Log.i(TAG, "onReceive: ANT INTERFACE CLAIMED");

				boolean wasClaimed = hasClaimedAntInterface;

				// Could also read ANT_INTERFACE_CLAIMED_PID from intent and see
				// if it matches the current process PID.
				try {
					Log.i(TAG, "onReceive: claimed 1");

					hasClaimedAntInterface = antServiceListener
							.hasClaimedInterface();

					Log.i(TAG, "onReceive: claimed 2");

					if (wasClaimed != hasClaimedAntInterface) {
						Log.i(TAG, "onReceive: claimed 3");

						if (!hasClaimedAntInterface) {
							Log.d(TAG, "ANT Interface released");

							unregisterReceiver(mAntMessageReceiver);

						}
					}

					if (hasClaimedAntInterface) {
						registerReceiver(mAntMessageReceiver, messageIntentFilter);
					}
					
					Log.d(TAG, "ready to start connection");

					Log.d(TAG, "hasClaimedAntInterface? " + hasClaimedAntInterface);
					Log.d(TAG, "isEnabled? " + antServiceListener.isEnabled());
					Log.d(TAG, "startAntConnection? " + startAntConnection);

					
					if (hasClaimedAntInterface && antServiceListener.isEnabled() && startAntConnection) {
						try {
							registerReceiver(mAntMessageReceiver,
									messageIntentFilter);
	
							Log.d(TAG, "starting connection");
							Log.d(TAG, "enabled? " + AntPulseDriver.this.antServiceListener.isEnabled());
							Log.d(TAG, "connected? " + AntPulseDriver.this.antServiceListener.isServiceConnected());
							Log.d(TAG, "starting connection");
							startAntConnection = false;
							setupAntConnection();
						} catch (AntInterfaceException e) {
							Log.d(TAG, "-", e);
						}
					}
				} catch (AntInterfaceException e) {
					Log.d(TAG, "error", e);
					antError();
				}
			}
		}
	};

	/**
	 * ANT Channel Configuration.
	 * 
	 * @param networkNumber
	 *            the network number
	 * @param channelNumber
	 *            the channel number
	 * @param deviceNumber
	 *            the device number
	 * @param deviceType
	 *            the device type
	 * @param txType
	 *            the tx type
	 * @param channelPeriod
	 *            the channel period
	 * @param radioFreq
	 *            the radio freq
	 * @param proxSearch
	 *            the prox search
	 * @return true, if successfully configured and opened channel
	 */
	private boolean antChannelSetup(byte networkNumber, byte channelNumber,
			short deviceNumber, byte deviceType, byte txType,
			short channelPeriod, byte radioFreq, byte proxSearch) {
		boolean channelOpen = false;

		try {
			antServiceListener.ANTAssignChannel(channelNumber,
					AntDefine.PARAMETER_RX_NOT_TX, networkNumber); // Assign as
																	// slave
																	// channel
																	// on
																	// selected
																	// network
																	// (0 =
																	// public, 1
																	// = ANT+, 2
																	// = ANTFS)
			antServiceListener.ANTSetChannelId(channelNumber, deviceNumber,
					deviceType, txType);
			antServiceListener
					.ANTSetChannelPeriod(channelNumber, channelPeriod);
			antServiceListener.ANTSetChannelRFFreq(channelNumber, radioFreq);

			antServiceListener.ANTSetChannelSearchTimeout(channelNumber,
					(byte) 0); // Disable high priority search
			antServiceListener.ANTSetLowPriorityChannelSearchTimeout(
					channelNumber, (byte) 12); // Set search timeout to 30
												// seconds (low priority search)

			if (deviceNumber == WILDCARD) {
				antServiceListener.ANTSetProximitySearch(channelNumber,
						proxSearch); // Configure proximity search, if using
										// wild card search
			}

			antServiceListener.ANTOpenChannel(channelNumber);

			channelOpen = true;
		} catch (AntInterfaceException aie) {
			Log.d(TAG, "error", aie);
			antError();
		}

		return channelOpen;
	}



	/**
	 * Display to user that an error has occured communicating with ANT Radio.
	 */
	private void antError() {
		Toast.makeText(this, "error in communication with ANT radio",
				Toast.LENGTH_LONG).show();
	}

	/**
	 * Receives all of the ANT message intents and dispatches to the proper
	 * handler.
	 */
	private final BroadcastReceiver mAntMessageReceiver = new BroadcastReceiver() {

		public String TAG = "mAntMessageReceiver";

		public void onReceive(Context context, Intent intent) {
			String ANTAction = intent.getAction();

			Log.d(TAG, "enter onReceive: " + ANTAction);
			if (ANTAction.equals(AntInterfaceIntent.ANT_RX_MESSAGE_ACTION)) {
				Log.d(TAG, "onReceive: ANT RX MESSAGE");

				byte[] ANTRxMessage = intent
						.getByteArrayExtra(AntInterfaceIntent.ANT_MESSAGE);
				String text = "Rx:";

				for (int i = 0; i < ANTRxMessage.length; i++)
					text += "["
							+ Integer.toHexString((int) ANTRxMessage[i] & 0xFF)
							+ "]";

				Log.d(TAG, text);

				byte rxChannelNumber = (byte) (ANTRxMessage[AntMesg.MESG_DATA_OFFSET] & AntDefine.CHANNEL_NUMBER_MASK);
				switch (rxChannelNumber) // Parse channel number
				{
				case HRM_CHANNEL:
					antDecodeHRM(ANTRxMessage);
					break;
				default:
					Log.i(TAG, "onReceive: Message for different channel ("
							+ rxChannelNumber + ")");
					break;
				}
			}
		}

		/**
		 * Decode ANT+ HRM messages.
		 * 
		 * @param ANTRxMessage the received ANT message.
		 */
		private void antDecodeHRM(byte[] ANTRxMessage) {
			Log.d(TAG, "antDecodeHRM start");

			if (ANTRxMessage[AntMesg.MESG_ID_OFFSET] == AntMesg.MESG_BROADCAST_DATA_ID) // message
																						// ID
																						// ==
																						// broadcast
			{
				if (deviceNumberHRM == WILDCARD) {
					try {
						Log.i(TAG, "antDecodeHRM: Requesting device number");
						antServiceListener.ANTRequestMessage(HRM_CHANNEL, AntMesg.MESG_CHANNEL_ID_ID);
					} catch (AntInterfaceException e) {
						Log.d(TAG, "error", e);
						antError();
					}
				}
				
				// Heart rate available in all pages and regardless of toggle bit            
				int bpm = ((int) ANTRxMessage[10] & 0xFF);
				Log.i(TAG, "Heart rate " + bpm + " BPM"); 

				byte data[] = new byte[4];
				LittleEndian.writeInt(bpm, data, 0);

				if (!hasRegisteredDataTypes) {
					return;
				}
				
				final GenericObservation observation = new GenericObservation(
						typePulse.getId(), System.currentTimeMillis(), data);
				addObservation(observation);

			} else if (ANTRxMessage[AntMesg.MESG_ID_OFFSET] == AntMesg.MESG_RESPONSE_EVENT_ID
					&& ANTRxMessage[3] == AntMesg.MESG_EVENT_ID
					&& ANTRxMessage[4] == AntDefine.EVENT_RX_SEARCH_TIMEOUT) // Search
																			 // timeout
			{
				try {
					Log.i(TAG, "Received search timeout");

					antServiceListener.ANTUnassignChannel((byte) 0);
				} catch (AntInterfaceException e) {
					Log.d(TAG, "error", e);

					antError();
				}
			} else if (ANTRxMessage[AntMesg.MESG_ID_OFFSET] == AntMesg.MESG_CHANNEL_ID_ID) // Store
																							// requested
																							// Channel
																							// Id
			{
				Log.i(TAG, "antDecodeHRM: Received device number");

				deviceNumberHRM = (short) (((int) ANTRxMessage[3] & 0xFF | ((int) (ANTRxMessage[4] & 0xFF) << 8)) & 0xFFFF);
			}

			Log.d(TAG, "antDecodeHRM end");
		}
	};

	private boolean hasRegisteredDataTypes = false;
	
	protected void onRegisterDataTypes() {
		Log.d(TAG, "onRegisterDataTypes");
		
		hasRegisteredDataTypes = true;
		
		typePulse = typesMap.get(DriverInterface.TYPE_PULSE);
		
		try {
			antServiceListener.claimInterface();
		} catch (AntInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");

		startAntConnection = true;
		
		return super.onStartCommand(intent, flags, startId);
	}

	private void setupAntConnection() throws AntInterfaceException {
		antServiceListener.ANTResetSystem();

		try {
			antServiceListener.ANTDisableEventBuffering();
		} catch (AntInterfaceException e) {
			Log.e(TAG, "Could not configure event buffering", e);
		}

		if (!antChannelSetup((byte) 0x01, // Network: 1 (ANT+)
				HRM_CHANNEL, deviceNumberHRM, (byte) 0x78, // Device Type: 120 (HRM)
				(byte) 0x00, // Transmission Type: wild card
				(short) 0x1F86, // Channel period: 8070 (~4Hz)
				(byte) 0x39, // RF Frequency: 57 (ANT+)
				proximityThreshold)) {
			Log.w(TAG, "onClick Open channel: Channel Setup failed");
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.d(TAG, "onCreate");

		hasClaimedAntInterface = false;

		// ANT intent broadcasts.
		statusIntentFilter = new IntentFilter();
		statusIntentFilter.addAction(AntInterfaceIntent.ANT_ENABLED_ACTION);
		statusIntentFilter.addAction(AntInterfaceIntent.ANT_DISABLED_ACTION);
		statusIntentFilter.addAction(AntInterfaceIntent.ANT_RESET_ACTION);
		statusIntentFilter
				.addAction(AntInterfaceIntent.ANT_INTERFACE_CLAIMED_ACTION);

		messageIntentFilter = new IntentFilter();
		messageIntentFilter.addAction(AntInterfaceIntent.ANT_RX_MESSAGE_ACTION);

		Context context = this.getApplicationContext();

		antServiceListener = AntInterface.getInstance(context,
				mAntServiceListener);

		if (null == antServiceListener) {
			throw new RuntimeException("No ant support or no service installed");
		}

		antServiceConnected = antServiceListener.isServiceConnected();

		registerReceiver(mAntStatusReceiver, statusIntentFilter);

		if (antServiceConnected) {
			try {
				hasClaimedAntInterface = antServiceListener
						.hasClaimedInterface();
				if (hasClaimedAntInterface) {
					Log.i(TAG, "receiveAntRxMessages: START");

					registerReceiver(mAntMessageReceiver, messageIntentFilter);
				}				
				
			} catch (AntInterfaceException e) {
				Log.d(TAG, "error", e);
				antError();
			}
		}
		
		try {
			if (!antServiceListener.isEnabled()) {
				Log.d(TAG, "enabling radio");
				antServiceListener.enable();
			}
		} catch (AntInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		
		try {
			if (antServiceConnected) {
				if (hasClaimedAntInterface) {
					Log.d(TAG, "onDestroy: Releasing interface");
					antServiceListener.releaseInterface();

				}

				antServiceListener.stopRequestForceClaimInterface();

				antServiceListener.destroy();
			}
		} catch (AntInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			unregisterReceiver(mAntStatusReceiver);
			
			unregisterReceiver(mAntMessageReceiver);
		} catch(IllegalArgumentException e) {
			
		}
	}
	
	@Override
	public String getDriverAction() {
		return ACTION;
	}
	
	public static class Discover extends BroadcastingService.Discover { 
		@Override
		public ObservationType[] getObservationTypes(Context context) {
			final Intent response = new Intent();
			response.setAction(DriverInterface.ACTION_DISCOVERED);
			
			final ObservationKeyname [] keynames = new ObservationKeyname [] {
				new ObservationKeyname(AntPulseDriver.HEART_BEAT, AntPulseDriver.UNIT, DriverInterface.KEYNAME_DATATYPE_INTEGER),
			};
			
			final ObservationType[] types = new ObservationType[1];
			
			types[0] = new ObservationType(
					"Ant Heart beat information", 
					DriverInterface.TYPE_PULSE,
					"External Garmin ANT+ sensor", 
					keynames);
			types[0].setId(1311060563000l);
	
			types[0].setDriver(getDriver());
			
			return types;
		}
		
		public Driver getDriver() {
			final Driver driver = new Driver(AntPulseDriver.class.getName());
			driver.setId(/* temporary id*/ 1311060562000l);
			
			return driver;
		}
	}
}
