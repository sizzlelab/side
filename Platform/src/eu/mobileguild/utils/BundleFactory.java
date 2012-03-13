package eu.mobileguild.utils;

import android.os.Bundle;

public class BundleFactory {
	public static Bundle create(String param, String value) {
		final Bundle b = new Bundle();
		
		b.putString(param, value);
		
		return b;
	}
}
