package fi.hut.soberit.sensors.fora.db;


public class BloodPressure extends Record
{
    private long id;
    private int systolic;
    private int diastolic;
	private String comment;
    
    // public API
    public BloodPressure(long time, int systolic, int diastolic) {
		super(time);
		
		this.systolic = systolic;
		this.diastolic = diastolic;
	}
    
    /**
     * @return the id
     */
    public long getId() {
        return id;
    }
    
    
    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }
    
    /**
     * @return the value
     */
    public int getSystolic()
    {
        return this.systolic;
    }
    
    
    /**
     * @param set the value
     */
    public void setSystolic(int v)
    {
        this.systolic = v;
    }
    
    
    /**
     * @return the time
     */
    public long getTime()
    {
        return time;
    }
    
    
    /**
     * Set the Unix time when the message was created.
     * 
     * @param time the time to set
     */
    public void setTime(long time)
    {
        this.time    = time;

    }
    
    /**
     * Convert to a plain string.
     * 
     * @return an understandable string.
     */
    public String toString()
    {
        return "Measure (" + id + ") = '" + systolic + "|" + diastolic + "' @ " + time;
    }

	public int getDiastolic() {
		return diastolic;
	}

	public void setDiastolic(int diastolic) {
		this.diastolic = diastolic;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}
}