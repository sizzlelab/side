package fi.hut.soberit.physicalactivity.legacy;

public class LegacyObservation {

	long sessionId;
	String time;

	enum Type {
		PULSE, ACCELERATION, GLUCOSE, BLOOD_PRESSURE;
	}

	Type type;

	float observation1;
	float observation2;
	float observation3;
	
	public long getSessionId() {
		return sessionId;
	}
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public float getObservation1() {
		return observation1;
	}
	public void setObservation1(float observation1) {
		this.observation1 = observation1;
	}
	public float getObservation2() {
		return observation2;
	}
	public void setObservation2(float observation2) {
		this.observation2 = observation2;
	}
	public float getObservation3() {
		return observation3;
	}
	public void setObservation3(float observation3) {
		this.observation3 = observation3;
	}
}
