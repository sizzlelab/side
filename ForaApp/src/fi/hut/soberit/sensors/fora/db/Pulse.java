package fi.hut.soberit.sensors.fora.db;

public class Pulse extends Record {

	long id;
	int pulse;

	public Pulse(long time, int pulse) {
		super(time);
		
		this.pulse = pulse;
	}

	
	public int getPulse() {
		return pulse;
	}

	public void setPulse(int pulse) {
		this.pulse = pulse;
	}


	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}


}
