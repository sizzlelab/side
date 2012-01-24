package fi.hut.soberit.sensors.fora.db;



public class Temperature extends Record
{
    private long id;

    private float temperature;
    
    public Temperature(long timestamp, float temperature) {
    	super(timestamp);
    	this.temperature = temperature;
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
     * Convert to a plain string.
     * 
     * @return an understandable string.
     */
    public String toString()
    {
        return "Measure (" + id + ") = '" + temperature  + "' @ " + time;
    }
    
    
    public float getTemperature() {
    	return temperature;
    }
    
    public void setTemperature(float temperature) {
    	this.temperature = temperature;
    }
}