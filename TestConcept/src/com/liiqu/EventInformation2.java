package com.liiqu;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class EventInformation2 extends FragmentActivity {

	private static final String TAG = EventInformation2.class.getSimpleName();
	private static final int REQUEST_CHOOSE_PARTICIPATION = 1;

	@Override
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
	
        if (sis == null) {

			final EventInfromationFragment eventInfo = new EventInfromationFragment();
			
			final Bundle eventArgs = new Bundle();
			eventArgs.putLong(EventInfromationFragment.EVENT_ID, 1091);
			eventInfo.setArguments(eventArgs);
			
			getSupportFragmentManager()
		        .beginTransaction()
		        .add(android.R.id.content, eventInfo)
		        .commit();
        }
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == REQUEST_CHOOSE_PARTICIPATION && resultCode == Activity.RESULT_OK) {
    		final FragmentManager manager = getSupportFragmentManager();
    		
    		final String choice = data.getStringExtra(ChooseParticipation.USER_CHOICE);
    		final String userId = data.getStringExtra(ChooseParticipation.USER_ID);

    		
    		((EventInfromationFragment) manager.findFragmentById(android.R.id.content)).onChangeParticipation(userId, choice);
    		
    	}
    }

	
	public void openMap(String uri) {
    	Log.d(TAG, "openMap " + uri);
    	
    	final Intent intent = new Intent();
    	intent.setAction(Intent.ACTION_VIEW);
    	intent.setData(Uri.parse(uri));
    	
    	startActivity(intent);
    }
    
    public void startRsvpActivity(String uid, String name, String pic) {
    	final Intent intent = new Intent(this, ChooseParticipation.class);
    	intent.putExtra(ChooseParticipation.USER_ID, uid);
    	intent.putExtra(ChooseParticipation.USER_NAME, name);
    	intent.putExtra(ChooseParticipation.USER_PICTURE, pic);
    	
    	startActivityForResult(intent, REQUEST_CHOOSE_PARTICIPATION);    	
    }
}
