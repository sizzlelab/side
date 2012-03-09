package com.liiqu;

import com.viewpagerindicator.TabPageIndicator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class DetailedView extends FragmentActivity {

	private static final String TAG = DetailedView.class.getSimpleName();
	private static final int REQUEST_CHOOSE_PARTICIPATION = 1;
	private static final String TAB_INDEX = "tab index";
	
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;
	private TabPageIndicator mIndicator;

	@Override
	public void onCreate(Bundle sis) {
		super.onCreate(sis);
	
		setContentView(R.layout.actionbar_tabs_pager);
		

        
		final ActionBar actionBar = getSupportActionBar();
		ActionBar.Tab tab1 = actionBar.newTab().setText(
				R.string.info_tab);
		ActionBar.Tab tab2 = actionBar.newTab().setText(
				R.string.participants_tab);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mTabsAdapter = new TabsAdapter(this, mViewPager);

		final Bundle eventArgs = new Bundle();
		eventArgs.putLong(EventInfromationFragment.EVENT_ID, 1091);

		mTabsAdapter.addTab(tab1, EventInfromationFragment.class, eventArgs);
		mTabsAdapter.addTab(tab2, EventInfromationFragment.class, eventArgs);

		mIndicator = (TabPageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mViewPager);

		if (sis != null) {
			mTabsAdapter.setTabSelected(sis.getInt(TAB_INDEX));
	        
//			if (sis == null) {
//
//				final EventInfromationFragment eventInfo = new EventInfromationFragment();
//				
//				eventInfo.setArguments(eventArgs);
//				
//				getSupportFragmentManager()
//			        .beginTransaction()
//			        .add(android.R.id.content, eventInfo)
//			        .commit();
//	        }
		}
        		
		actionBar.setTitle("");

	}
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == REQUEST_CHOOSE_PARTICIPATION && resultCode == Activity.RESULT_OK) {
    		final FragmentManager manager = getSupportFragmentManager();
    		
    		final String choice = data.getStringExtra(ChooseParticipation.USER_CHOICE);
    		final String userId = data.getStringExtra(ChooseParticipation.USER_ID);

    		
    		((EventInfromationFragment) manager.findFragmentById(android.R.id.content)).onChangeParticipation(userId, choice);
    		
    	}
    }

    @Override
    public void onSaveInstanceState(Bundle sis) {
    	super.onSaveInstanceState(sis);
    	
    	sis.putInt(TAB_INDEX, getSupportActionBar().getSelectedNavigationIndex());
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
