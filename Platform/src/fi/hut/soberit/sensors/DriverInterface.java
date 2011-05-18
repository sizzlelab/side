package fi.hut.soberit.sensors;

public class DriverInterface {

	public static final String ACTION_START_DISCOVERY = "fi.hut.soberit.sensors.action.DRIVER_DISCOVERY";
	public static final String ACTION_DISCOVERED = "fi.hut.soberit.sensors.action.DRIVER_DISCOVERED";
	public static final String INTENT_DRIVER_SERVICE_URL = "driver service";
	
	public static final String INTENT_DATA_TYPE = "data type";
	public static final String INTENT_DEVICE_ID = "device id";

	
	public static final String TYPE_GLUCOSE = "application/vnd.sensor.bloodglucose";
	public static final String TYPE_PULSE = "application/vnd.sensor.pulse";
	public static final String TYPE_ACCELEROMETER = "application/vnd.sensor.accelerometer";


    static final int MSG_REGISTER_CLIENT = 1;

    static final int MSG_UNREGISTER_CLIENT = 2;

    static final int MSG_OBSERVATION = 3;
    
    public static final String MSG_FIELD_DATA_TYPES = "data types";
	public static final String MSG_FIELD_DATE_TYPE_IDS = "data type id";
	public static final String MSG_FIELD_OBSERVATIONS = "observations";
}
