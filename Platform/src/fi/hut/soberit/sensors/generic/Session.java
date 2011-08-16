package fi.hut.soberit.sensors.generic;

import java.util.Date;

public class Session {
	private Date start;
	
	private Date end;
	
	private long id;

	public Session(long id, Date start, Date end) {
		super();
		this.id = id;
		this.start = start;
		this.end = end;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	
}
