package com.liiqu.util.ui;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.ActionBar.Tab;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.viewpagerindicator.TitleProvider;

/**
 * This is a helper class that implements the management of tabs and all
 * details of connecting a ViewPager with associated TabHost.  It relies on a
 * trick.  Normally a tab host has a simple API for supplying a View or
 * Intent that each tab will show.  This is not sufficient for switching
 * between pages.  So instead we make the content part of the tab host
 * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
 * view to show as the tab content.  It listens to changes in tabs, and takes
 * care of switch to the correct paged in the ViewPager whenever the selected
 * tab changes.
 */
public class TabsAdapter extends FragmentPagerAdapter 
	implements ViewPager.OnPageChangeListener, ActionBar.TabListener, TitleProvider {
    private static final String TAG = TabsAdapter.class.getSimpleName();
	private final FragmentActivity mContext;
    private final ViewPager mViewPager;
    private final ArrayList<String> mTabs = new ArrayList<String>();
    private final ArrayList<String> mTabTitles = new ArrayList<String>();
    private final ArrayList<Bundle> mBundles = new ArrayList<Bundle>();
	private int selected;
    
    public TabsAdapter(FragmentActivity activity, ViewPager pager) {
        super(activity.getSupportFragmentManager());
        
        mContext = activity;
        mViewPager = pager;
        mViewPager.setAdapter(this);
        mViewPager.setOnPageChangeListener(this);
    }

    public void addTab(ActionBar.Tab tab, Class<?> clss) {
    	addTab(tab, clss, null);
    }

    public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle bundle) {
    	mTabs.add(clss.getName());
        mBundles.add(bundle);
        mTabTitles.add(tab.getText().toString());
        
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    @Override
    public Fragment getItem(int position) {
        final Fragment instance = Fragment.instantiate(mContext, mTabs.get(position), mBundles.get(position));
        return instance;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    	Log.d(TAG, String.format("onPageScrolled(%d, %f, %d)", position, positionOffset, positionOffsetPixels));
    }

    @Override
    public void onPageSelected(int position) {
    	/* Is this really the place to fix that this position was selected?*/
    	selected = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	public void setTabSelected(int position) {
		mViewPager.setCurrentItem(position);
	}
	
	
	public int getSelected() {
		
		// see comment next to onPageSelected
		return selected;
	}
	
	@Override
	public String getTitle(int position) {
		return mTabTitles.get(position);
	}
	
}