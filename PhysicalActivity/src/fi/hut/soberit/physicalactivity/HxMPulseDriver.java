package fi.hut.soberit.physicalactivity;

import fi.hut.soberit.sensors.Driver;
import fi.hut.soberit.sensors.hxm.HxMDriver;

public class HxMPulseDriver extends HxMDriver {

	public final static String ACTION = HxMPulseDriver.class.getName();
	
	
	public HxMPulseDriver() {
		super();
		
		provideBluetoothAddressMessage = R.string.provide_bluetooth_address;
	}
	
	@Override
	public String getDriverAction() {
		return ACTION;
	}

	public static class Discover extends HxMDriver.Discover {
		
		@Override
		public Driver getDriver() {
			final Driver driver = super.getDriver();
			driver.setUrl(ACTION);
			
			return driver;
		}
	}

}
