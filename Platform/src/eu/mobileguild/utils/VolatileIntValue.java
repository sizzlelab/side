package eu.mobileguild.utils;

public class VolatileIntValue {
	
	private long expirationTime;
	public long lastUpdateTime = 0;
	public long value = 0;

	public VolatileIntValue(long expirationTime) {
		this.expirationTime = expirationTime;
	}
	
	public void start() {
		this.lastUpdateTime = System.currentTimeMillis();
	}
	
	public void reset() {
		this.lastUpdateTime = 0;
	}
	
	public void update(long value, long now) {
		this.lastUpdateTime = now;
		this.value = value;
	}
	
	public boolean hasValue(long now) {
		return lastUpdateTime != 0 && now - lastUpdateTime <= expirationTime;
	}
	
	public long value() {
		return value ;
	}
}
