package fi.hut.soberit.sensors.fora.db;

public class Record implements Comparable<Record> {

	long time;

	protected Record(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}

	@Override
	public int compareTo(Record that) {
		int res =  (int) (this.time - that.time);
		
		if (res == 0) {
			return this.getClass().getName().compareTo(that.getClass().getName());
		}
		
		return res;
	}

}
