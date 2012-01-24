/*******************************************************************************
c * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package fi.hut.soberit.sensors;

public class DriverInterface {

	public static final String ACTION_START_DISCOVERY = "fi.hut.soberit.sensors.action.DRIVER_DISCOVERY";
	public static final String ACTION_DISCOVERED = "fi.hut.soberit.sensors.action.DRIVER_DISCOVERED";

	public static final String ACTION_START_UPLOADER_DISCOVERY = "fi.hut.soberit.sensors.action.UPLOADER_DISCOVERY";
	
	public static final String ACTION_UPLOADER_DISCOVERED = "fi.hut.soberit.sensors.action.UPLOADER_DISCOVERED";


	public static final String ACTION_DRIVERS_STARTED = "fi.hut.soberit.sensors.action.DRIVERS_STARTED";
	
	public static final String ACTION_START_STORAGE_DISCOVERY = "fi.hut.soberit.sensors.action.STORAGE_DISCOVERY";
	
	public static final String ACTION_STORAGE_DISCOVERED = "fi.hut.soberit.sensors.action.STORAGE_DISCOVERED";

	public static final String ACTION_SESSION_STARTED = "fi.hut.soberit.sensors.action.SESSION_STARTED";

	public static final String ACTION_SESSION_STOP = "fi.hut.soberit.sensors.action.SESSION_STOP";
	
	public static final String DISPLAY_INTENT = "fi.hut.soberit.sensors.action.DISPLAY_DATA";
	
	public static final String TYPE_GLUCOSE = "application/vnd.sensor.bloodglucose";
	public static final String TYPE_PULSE = "application/vnd.sensor.pulse";
	public static final String TYPE_ACCELEROMETER = "application/vnd.sensor.accelerometer";
	public static final String TYPE_BLOOD_PRESSURE = "application/vnd.sensor.bloodpressure";
	public static final String TYPE_STRIDES = "application/vnd.sensor.strides";
	public static final String TYPE_TEMPERATURE = "application/vnd.sensor.temperature";
	public static final String TYPE_AMBIENT_TEMPERATURE = "application/vnd.sensor.ambient_temperature";
	public static final String TYPE_RESPIRATION = "application/vnd.sensor.respiration";
	public static final String TYPE_SKIN_CONDUCTIVITY = "application/vnd.sensor.skin_conductivity";
	
	public static final Long TYPE_INDEX_GLUCOSE = 131159569600l;
	public static final Long TYPE_INDEX_PULSE = 131159569500l;
//	public static final Long TYPE_INDEX_ACCELEROMETER = "application/vnd.sensor.accelerometer";
	public static final Long TYPE_INDEX_BLOOD_PRESSURE = 131159569400l;
	public static final Long TYPE_INDEX_STRIDES = 1312111478000l;
	public static final Long TYPE_INDEX_TEMPERATURE = 1325763611000l;
	public static final Long TYPE_INDEX_AMBIENT_TEMPERATURE = 1325763611030l;
	public static final Long TYPE_INDEX_RESPIRATION = 1312111477999l;
//	public static final Long TYPE_INDEX_SKIN_CONDUCTIVITY = "application/vnd.sensor.skin_conductivity";
	
	
	/**
	 * Rename messages according to one naming convention. 
	 * For instance, all request messages prefixed with MSG_READ
	 * All response messages prefixed with MSG_REPLY
	 */
    public static final int MSG_REGISTER_CLIENT = 1;

    public static final int MSG_UNREGISTER_CLIENT = 2;

    public static final int MSG_OBSERVATION = 3;
    
	public static final int MSG_REGISTER_DATA_TYPES = 4;

    public static final int MSG_SHUTTING_DOWN = 5;

    public static final int MSG_SENSOR_CONNECTED = 6;
    
	public static final int MSG_SENSOR_DISCONNECTED = 7;
	
	public static final int MSG_READ_SINK_OBJECTS_NUM = 8;
	
	public static final int MSG_SINK_OBJECTS_NUM = 9;
	
	public static final int MSG_READ_SINK_OBJECTS = 10;
	
	public static final int MSG_READ_SENSOR_CONNECTVITY = 11;

	public static final int MSG_SENSOR_CONNECTIVITY = 12;

	public static final String INTENT_FIELD_DEVICE_ID = "device id";
	public static final String INTENT_FIELD_STORAGES = "storages";

	public static final String MSG_FIELD_OBSERVATIONS = "observations";
	public static final String MSG_REGISTER_SESSION_ID = "sesion_id";
	
    public static final String INTENT_FIELD_DATA_TYPE = "data types";

	public static final String INTENT_FIELD_OBSERVATION_TYPES = "observation types";
	public static final String INTENT_FIELD_UPLOADER = "uploader";
	public static final String INTENT_FIELD_UPLOADERS = "uploaders";
	
	public static final String INTENT_FIELD_DRIVER = "url";
	
	public static final String INTENT_SESSION_ID = "session id";
	
	public static final String KEYNAME_DATATYPE_FLOAT = "float";
	public static final String KEYNAME_DATATYPE_INTEGER = "integer";

    public static final String MSG_FIELD_DATA_TYPES = "data types";
	public static final String MSG_FIELD_CLIENT_ID = "client id";
	public static final String MSG_FIELD_REPLY_TO = "reply to";
}
