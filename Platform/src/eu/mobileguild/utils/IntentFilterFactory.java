package eu.mobileguild.utils;

import android.content.IntentFilter;

public class IntentFilterFactory {

	
	public static IntentFilter simpleActionFilter(String action1) {
		final IntentFilter filter = new IntentFilter();
		filter.addAction(action1);
		
		return filter;
	}
}
