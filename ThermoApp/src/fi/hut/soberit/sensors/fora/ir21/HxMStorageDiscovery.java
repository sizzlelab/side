/*******************************************************************************
 * Copyright (c) 2011 Aalto University
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * Authors:
 * Maksim Golivkin <maksim@golivkin.eu>
 ******************************************************************************/
package fi.hut.soberit.sensors.fora.ir21;

import java.util.ArrayList;

import android.content.Context;
import fi.hut.soberit.fora.IR21Sink;
import fi.hut.soberit.sensors.DriverInterface;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.generic.Storage;
import fi.hut.soberit.sensors.services.StorageService.Discover;
import fi.hut.soberit.sensors.storage.GenericObservationStorage;

public class HxMStorageDiscovery extends Discover {

	public Storage[] getStorages(Context context) {
		
		final IR21Sink.Discover discover = new IR21Sink.Discover();
		
		final Storage storage = GenericObservationStorage.STORAGE;
		ArrayList<ObservationType> types = new ArrayList<ObservationType>();
		storage.setTypes(types);
		
		for(ObservationType type: discover.getObservationTypes(context)) {
			if (DriverInterface.TYPE_PULSE.equals(type.getMimeType())) {
				types.add(type);
				break;
			}
		}
		
		return new Storage[] {storage};
	}
}
