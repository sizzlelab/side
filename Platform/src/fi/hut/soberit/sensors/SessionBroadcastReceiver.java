package fi.hut.soberit.sensors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import fi.hut.soberit.sensors.generic.ObservationType;
import fi.hut.soberit.sensors.generic.Storage;
import fi.hut.soberit.sensors.generic.Uploader;

public class SessionBroadcastReceiver extends BroadcastReceiver {

	protected final String TAG = this.getClass().getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, intent.getAction());

		if (!DriverInterface.ACTION_SESSION_STARTED.equals(intent.getAction())) {
			return;
		}
		
		final ArrayList<ObservationType> types = new ArrayList<ObservationType>();
		for(Parcelable parcelable: intent.getParcelableArrayListExtra(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES)) {
			final ObservationType type = (ObservationType) parcelable;
			types.add(type);
		}

		startDrivers(context, intent, types);
		startUploaders(context, intent, types);
		
		startStorages(context, intent);
	}

	private void startStorages(Context context, Intent intent) {
		Log.d(TAG, "startStorages");
		ArrayList<Storage> storages = intent.getParcelableArrayListExtra(DriverInterface.INTENT_FIELD_STORAGES);
		
		for(Storage storage: storages) {
			if (isInterestingStorage(storage)) {
				startStorageService(context, intent, storage);
			}
		}
	}

	private void startDrivers(Context context, Intent intent, List<ObservationType> types) {
		Log.d(TAG, "startDrivers");

		HashSet<Driver> drivers = new HashSet<Driver>();
		
		for(ObservationType type: types) {
			final Driver driver = type.getDriver();
			if (isInterestingDriver(driver)) {
				Log.d(TAG, "" + driver);
				drivers.add(driver);
			}
		}
		
		for(Driver driver: drivers) {
			Log.d(TAG, "Starting " + driver.getUrl());
			startDriverService(context, intent, driver);
		}
		
	}

	protected void startDriverService(Context context, Intent broadcastIntent, Driver driver) {
		final Intent startDriver = new Intent();

		startDriver.setAction(driver.getUrl());
		
		context.startService(startDriver);
	}


	protected boolean isInterestingUploader(Uploader uploader) {

		final String uploaderUrl = uploader.getUrl();
		final String packageName = this.getClass().getPackage().getName();
		
		boolean matched = uploaderUrl.matches(String.format("^%s.*", packageName));
		
		Log.d(TAG, String.format("matched ? [%s, %b]", uploaderUrl, matched));
		
		return matched;
	}
	
	protected boolean isInterestingDriver(Driver driver) {
		final String uploaderUrl = driver.getUrl();
		final String packageName = this.getClass().getPackage().getName();
		
		return uploaderUrl.matches(String.format("^%s.*", packageName));
	}

	private void startUploaders(Context context, Intent intent, ArrayList<ObservationType> types) {
		Log.d(TAG, "startUploaders");

		Log.d(TAG, "types: " + types);
		
		for(Parcelable parcelable: intent.getParcelableArrayListExtra(DriverInterface.INTENT_FIELD_UPLOADERS)) {
			final Uploader uploader = (Uploader) parcelable;
			
			if (!isInterestingUploader(uploader)) {
				continue;
			}
			
			if (!uploader.hasAllTypes(types)) {
				continue;
			}
			
			startUploaderService(uploader, context, intent, types);
		}
	}

	protected void startUploaderService(Uploader uploader, Context context, Intent broadcastIntent, ArrayList<ObservationType> types) {
		Log.d(TAG, "startUploaderService");
		
		final Intent startUploader = new Intent();
		startUploader.setAction(uploader.getUrl());
		
		startUploader.putParcelableArrayListExtra(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES, types);
		
		context.startService(startUploader);
	}
	
	private boolean isInterestingStorage(Storage storage) {
		final String url = storage.getUrl();
		final String packageName = this.getClass().getPackage().getName();

		boolean matched = url.matches(String.format("^%s.*", packageName));
		Log.d(TAG, String.format("matched ? [%s, %b]", url, matched));

		return matched;
	}
	
	private void startStorageService(Context context, Intent intent,
			Storage storage) {
		Log.d(TAG, "startStorageService " + storage);
		
		final Intent startStorage = new Intent();
		startStorage.setAction(storage.getUrl());
		
		startStorage.putParcelableArrayListExtra(DriverInterface.INTENT_FIELD_OBSERVATION_TYPES, storage.getTypes());
		startStorage.putExtra(DriverInterface.INTENT_SESSION_ID, 
				intent.getLongExtra(DriverInterface.INTENT_SESSION_ID, -1));
		context.startService(startStorage);
	}

}
