package fi.hut.soberit.sensors.fora;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ActionBar.Tab;
import android.support.v4.view.ViewPager;

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
public class TabsAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener, ActionBar.TabListener {
    private final FragmentActivity mContext;
    private final ActionBar mActionBar;
    private final ViewPager mViewPager;
    private final ArrayList<String> mTabs = new ArrayList<String>();
    private final ArrayList<Bundle> mBundles = new ArrayList<Bundle>();
	private int selected;
    
    public TabsAdapter(FragmentActivity activity, ActionBar actionBar, ViewPager pager) {
        super(activity.getSupportFragmentManager());
        
        mContext = activity;
        mActionBar = actionBar;
        mViewPager = pager;
        mViewPager.setAdapter(this);
        mViewPager.setOnPageChangeListener(this);
    }

    public void addTab(ActionBar.Tab tab, Class<?> clss) {
    	addTab(tab, clss, null);
    }

    public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle bundle) {
        mTabs.add(clss.getName());
        mActionBar.addTab(tab.setTabListener(this));
        mBundles.add(bundle);
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
    }

    @Override
    public void onPageSelected(int position) {
    	selected = position;
        mActionBar.setSelectedNavigationItem(position);
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

	public int getSelected() {
		return selected;
	}
	
	public Fragment getSelectedTab() {
		
		return mContext.getSupportFragmentManager().findFragmentByTag(
				makeFragmentName(mViewPager.getId(), selected));
	}
	
}