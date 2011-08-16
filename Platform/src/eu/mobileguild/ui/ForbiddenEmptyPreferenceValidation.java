package eu.mobileguild.ui;

import fi.hut.soberit.sensors.R;
import android.content.Context;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;

public class ForbiddenEmptyPreferenceValidation implements OnPreferenceChangeListener {

	private Context context;

	public ForbiddenEmptyPreferenceValidation(Context context) {
		this.context = context;
	}
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		final String text = (String) newValue;
		
		boolean empty = text.trim().length() == 0; 
		
		if (empty) {
			final String warningString = context.getString(R.string.empty_preference);
			Toast.makeText(context, warningString, Toast.LENGTH_SHORT).show();
		}
		return !empty;
	}
}

