package com.liiqu.util;

import android.app.Activity;
import android.content.Intent;

public class IntentFactory {

	public static Intent create(String action) {
		final Intent intent = new Intent();
		intent.setAction(action);
		
		return intent;
	}
}
