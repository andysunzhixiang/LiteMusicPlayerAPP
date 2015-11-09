package org.loader.liteplayer.activity;

import java.util.ArrayList;

import org.loader.liteplayer.R;
import org.loader.liteplayer.fragment.LocalFragment;
import org.loader.liteplayer.fragment.NetSearchFragment;
import org.loader.liteplayer.service.PlayService;
import org.loader.liteplayer.ui.Indicator;
import org.loader.liteplayer.ui.ScrollRelativeLayout;
import org.loader.liteplayer.utils.L;
import org.loader.liteplayer.utils.MusicUtils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import android.view.View;
import android.view.View.OnClickListener;

import android.widget.TextView;




public class MainActivity extends BaseActivity implements OnClickListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	private ScrollRelativeLayout mMainContainer;
	private Indicator mIndicator;
	
	private TextView mLocalTextView;
	private TextView mSearchTextView;
	private ViewPager mViewPager;

	private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		registerReceiver();
		initFragments();
		setupViews();
	}
	/**
	 * Register broadcast Receiver
	 * Update Music list when delete song
	 */
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter( Intent.ACTION_MEDIA_SCANNER_STARTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		filter.addDataScheme("file");
		registerReceiver(mScanSDCardReceiver, filter);
	}
	
	private void setupViews() {
		mMainContainer = (ScrollRelativeLayout) findViewById(R.id.rl_main_container);
		mIndicator = (Indicator) findViewById(R.id.main_indicator);
		mLocalTextView = (TextView) findViewById(R.id.tv_main_local);
		mSearchTextView = (TextView) findViewById(R.id.tv_main_favourite);
		mViewPager = (ViewPager) findViewById(R.id.vp_main_container);

		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(mPageChangeListener);
		
		mLocalTextView.setOnClickListener(this);
		mSearchTextView.setOnClickListener(this);
		
		selectTab(0);
	}
	
	private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			selectTab(position);
			mMainContainer.showIndicator();
		}
		
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			mIndicator.scroll(position, positionOffset);
		}
		
		@Override
		public void onPageScrollStateChanged(int position) {
			
		}
	};
	
	private FragmentPagerAdapter mPagerAdapter = 
			new FragmentPagerAdapter(getSupportFragmentManager()) {
		@Override
		public int getCount() {
			return mFragments.size();
		}
		
		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}
	};
	
	/**
	 * indicator
	 * @param index
	 */
	private void selectTab(int index) {
		switch (index) {
		case 0:
			mLocalTextView.setTextColor(getResources().getColor(R.color.main));
			mSearchTextView.setTextColor(getResources().getColor(R.color.main_dark));
			break;
		case 1:
			mLocalTextView.setTextColor(getResources().getColor(R.color.main_dark));
			mSearchTextView.setTextColor(getResources().getColor(R.color.main));
			break;
		}
	}
	
	private void initFragments() {
		LocalFragment localFragment = new LocalFragment();
		NetSearchFragment netSearchFragment = new NetSearchFragment();

		mFragments.add(localFragment);
		mFragments.add(netSearchFragment);
	}
	
	/**
	 * Get play music service
	 * @return
	 */
	public PlayService getPlayService() {
		return mPlayService;
	}
	
	public void hideIndicator() {
		mMainContainer.hideIndicator();
	}
	
	public void showIndicator() {
		mMainContainer.showIndicator();
	}


	
	@Override
	public void onPublish(int progress) {

		// if current fragment is local fragment
		// call fragment's setProgress() method.
		if(mViewPager.getCurrentItem() == 0) {
			((LocalFragment)mFragments.get(0)).setProgress(progress);
		}
	}

	@Override
	public void onChange(int position) {
		// if current fragment is local fragment
		// call fragment's setProgress() method.
		if(mViewPager.getCurrentItem() == 0) {
			((LocalFragment)mFragments.get(0)).onPlay(position);
		}
	}
	

	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_main_local:
			mViewPager.setCurrentItem(0);
			break;
		case R.id.tv_main_favourite:
			mViewPager.setCurrentItem(1);
			break;
		}
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mScanSDCardReceiver);
		super.onDestroy();
	}
	
	private BroadcastReceiver mScanSDCardReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			L.l(TAG, "mScanSDCardReceiver---->onReceive()");
			if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
				MusicUtils.initMusicList();
				((LocalFragment)mFragments.get(0)).onMusicListChanged();
			}
		}
	};
}