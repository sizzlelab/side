/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
