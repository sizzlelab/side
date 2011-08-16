package eu.mobileguild.utils;

public class VolatileLongValue {
	
	private long expirationTime;
	public long lastUpdateTime = 0;
	public int value = 0;

	public VolatileLongValue(long expirationTime) {
		this.expirationTime = expirationTime;
	}
	
	public void start() {
		this.lastUpdateTime = System.currentTimeMillis();
	}
	
	public void reset() {
		this.lastUpdateTime = 0;
	}
	
	public void update(int value, long now) {
		this.lastUpdateTime = now;
		this.value = value;
	}
	
	public boolean hasValue(long now) {
		return lastUpdateTime != 0 && now - lastUpdateTime <= expirationTime;
	}
}
