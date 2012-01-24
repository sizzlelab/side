package fi.hut.soberit.sensors.fora;


public interface SensorStatusListener extends fi.hut.soberit.sensors.SensorStatusListener {
	
	public final static int CONNECTING = 10;
	public final static int DOWNLOADING = 11;
	public final static int STAND_BY = 12;
}
