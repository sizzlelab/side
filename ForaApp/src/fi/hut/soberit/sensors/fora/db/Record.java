package fi.hut.soberit.sensors.fora.db;

public class Record {

	long time;

	protected Record(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}

}
