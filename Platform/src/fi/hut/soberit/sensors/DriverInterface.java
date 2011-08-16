package fi.hut.soberit.sensors;

public class DriverInterface {

	public static final String ACTION_START_DISCOVERY = "fi.hut.soberit.sensors.action.DRIVER_DISCOVERY";
	public static final String ACTION_DISCOVERED = "fi.hut.soberit.sensors.action.DRIVER_DISCOVERED";
//	public static final String INTENT_DRIVER_SERVICE_URL = "driver service";

	public static final String ACTION_START_UPLOADER_DISCOVERY = "fi.hut.soberit.sensors.action.UPLOADER_DISCOVERY";
	
	public static final String ACTION_UPLOADER_DISCOVERED = "fi.hut.soberit.sensors.action.UPLOADER_DISCOVERED";


	public static final String ACTION_DRIVERS_STARTED = "fi.hut.soberit.sensors.action.DRIVERS_STARTED";
	
	public static final String ACTION_START_STORAGE_DISCOVERY = "fi.hut.soberit.sensors.action.STORAGE_DISCOVERY";
	
	public static final String ACTION_STORAGE_DISCOVERED = "fi.hut.soberit.sensors.action.STORAGE_DISCOVERED";

	public static final String ACTION_SESSION_STARTED = "fi.hut.soberit.sensors.action.SESSION_STARTED";

	public static final String ACTION_SESSION_STOP = "fi.hut.soberit.sensors.action.SESSION_STOP";
		
//	public static final String ACTION_PING = "fi.hut.soberit.sensors.action.PING";
//	
//	public static final String ACTION_PONG = "fi.hut.soberit.sensors.action.PONG";
//	
	
	public static final String DISPLAY_INTENT = "fi.hut.soberit.sensors.action.DISPLAY_DATA";
	
	public static final String TYPE_GLUCOSE = "application/vnd.sensor.bloodglucose";
	public static final String TYPE_PULSE = "application/vnd.sensor.pulse";
	public static final String TYPE_ACCELEROMETER = "application/vnd.sensor.accelerometer";
	public static final String TYPE_BLOOD_PRESSURE = "application/vnd.sensor.bloodpressure";
	public static final String TYPE_STRIDES = "application/vnd.sensor.strides";

    public static final int MSG_REGISTER_CLIENT = 1;

    public static final int MSG_UNREGISTER_CLIENT = 2;

    public static final int MSG_OBSERVATION = 3;
    
	public static final int MSG_REGISTER_DATA_TYPES = 4;

    public static final int MSG_SHUTTING_DOWN = 5;
	
    public static final String MSG_FIELD_DATA_TYPES = "data types";
	//public static final String MSG_FIELD_DATA_TYPE_IDS = "data type id";
	public static final String INTENT_FIELD_DEVICE_ID = "device id";
	public static final String INTENT_FIELD_STORAGES = "storages";

	public static final String MSG_FIELD_OBSERVATIONS = "observations";
	public static final String MSG_REGISTER_SESSION_ID = "sesion_id";
	
    public static final String INTENT_FIELD_DATA_TYPE = "data types";

//	public static final String INTENT_FIELD_DATA_TYPE_IDS = "data type id";
	//public static final String INTENT_FIELD_DRIVERS = "drivers";
	public static final String INTENT_FIELD_OBSERVATION_TYPES = "observation types";
	public static final String INTENT_FIELD_UPLOADER = "uploader";
	public static final String INTENT_FIELD_UPLOADERS = "uploaders";
	
	public static final String INTENT_FIELD_DRIVER = "url";
	
	public static final String INTENT_SESSION_ID = "session id";
	
	public static final String KEYNAME_DATATYPE_FLOAT = "float";
	public static final String KEYNAME_DATATYPE_INTEGER = "integer";

	


}
