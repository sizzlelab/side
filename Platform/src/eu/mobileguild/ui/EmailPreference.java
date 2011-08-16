package eu.mobileguild.ui;

import fi.hut.soberit.sensors.R;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.widget.Toast;

public class EmailPreference extends EditTextPreference {
	
    private Context context;

	public EmailPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.context = context;
	}

    public void onClick (DialogInterface dialog, int which) {
    	if (which == DialogInterface.BUTTON_POSITIVE) {
			final String email = getText();
			
			boolean matches = email.matches("\\w+(\\.\\w+)*@(\\w+\\.)+\\w+");
			
			if (!matches) {
				Toast.makeText(context, context.getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
				return;
			} 
    	}
    	
    	onClick(dialog, which);    	
    }


}
