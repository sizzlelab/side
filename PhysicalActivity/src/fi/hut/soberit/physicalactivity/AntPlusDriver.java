package fi.hut.soberit.physicalactivity;

import fi.hut.soberit.antpulse.AntPulseDriver;
import fi.hut.soberit.sensors.Driver;

public class AntPlusDriver extends AntPulseDriver {

	public final static String ACTION = AntPlusDriver.class.getName();
	
	@Override
	public String getDriverAction() {
		return ACTION;
	}

	public static class Discover extends AntPulseDriver.Discover {
		
		@Override
		public Driver getDriver() {
			final Driver driver = super.getDriver();
			driver.setUrl(ACTION);
			
			return driver;
		}
	}

}
