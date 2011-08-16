package fi.hut.soberit.physicalactivity;

import fi.hut.soberit.sensors.Driver;

public class AccelerometerDriver extends fi.hut.soberit.sensors.drivers.AccelerometerDriver {
	
	public static final String ACTION = AccelerometerDriver.class.getName();
	
	@Override
	public String getDriverAction() {
		return ACTION;
	}
	
	public static class Discover extends fi.hut.soberit.sensors.drivers.AccelerometerDriver.Discover {
		
		@Override
		public Driver getDriver() {
			Driver driver = super.getDriver();
			driver.setUrl(ACTION);
			
			return driver;
		}
	}

}
