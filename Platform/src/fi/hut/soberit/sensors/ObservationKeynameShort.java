package fi.hut.soberit.sensors;

public class ObservationKeynameShort {
	
	private long id;
	
	public ObservationKeynameShort(long id) {
		this.setId(id);
	}

	void setId(long id) {
		this.id = id;
	}

	long getId() {
		return id;
	}
}
