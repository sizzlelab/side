package fi.hut.soberit.sensors.fora.db;

public class Glucose extends Record
{
	// private attributes
    private long        id;
    private int        value;
    private int type;
	private String comment;
    
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_BEFORE_MEAL = 1;
    public static final int TYPE_AFTER_MEAL = 2;   
    public static final int TYPE_CTL_MODE = 3;

    // public API
    public Glucose(long time, int value, int type) {
		super(time);
		this.value = value;
		this.type = type;
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
    public int getGlucose()
    {
        return this.value;
    }
    
    
    /**
     * @param set the value
     */
    public void setValue(int v)
    {
        this.value = v;
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
        return "Measure (" + id + ") = '" + value + "' @ " + time;
    }


	public int getType() {
		return type;
	}


	public void setType(int type) {
		this.type = type;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}
}