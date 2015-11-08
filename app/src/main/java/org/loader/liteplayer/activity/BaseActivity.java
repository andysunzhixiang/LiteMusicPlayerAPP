package org.loader.liteplayer.activity;

//import org.loader.liteplayer.service.DownloadService;
import org.loader.liteplayer.service.PlayService;
import org.loader.liteplayer.utils.L;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;


public abstract class BaseActivity extends FragmentActivity {
	protected PlayService mPlayService;
    //protected DownloadService mDownloadService;
	private final String TAG = BaseActivity.class.getSimpleName();
	
	private ServiceConnection mPlayServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			L.l(TAG, "play--->onServiceDisconnected");
			mPlayService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mPlayService = ((PlayService.PlayBinder) service).getService();
			mPlayService.setOnMusicEventListener(mMusicEventListener);
			onChange(mPlayService.getPlayingPosition());
		}
	};
	
//	private ServiceConnection mDownloadServiceConnection = new ServiceConnection() {
//		@Override
//		public void onServiceDisconnected(ComponentName name) {
//			L.l(TAG, "download--->onServiceDisconnected");
//			mDownloadService = null;
//		}
//
//		@Override
//		public void onServiceConnected(ComponentName name, IBinder service) {
//			mDownloadService = ((DownloadService.DownloadBinder) service).getService();
//		}
//	};
	
	/**
	 * PlayService callback interface class
	 */
	private PlayService.OnMusicEventListener mMusicEventListener = 
			new PlayService.OnMusicEventListener() {
		@Override
		public void onPublish(int progress) {
			BaseActivity.this.onPublish(progress);
		}

		@Override
		public void onChange(int position) {
			BaseActivity.this.onChange(position);
		}
	};
	
	/**
	 * After loading Fragment's view callback
	 * allowBindService() start Play music service
	 * allowUnbindService()  unbind
	 * start Play music service in the SplashActivity.java startService() firstly
	 */
	public void allowBindService() {
		getApplicationContext().bindService(new Intent(this, PlayService.class),
				mPlayServiceConnection,
				Context.BIND_AUTO_CREATE);
	}
	
	/**
	 * Callback when fragment's view disappear
	 */
	public void allowUnbindService() {
		getApplicationContext().unbindService(mPlayServiceConnection);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * update progress
	 * implemented by the sub-class
	 * service connect with UI Thread
	 * @param progress play progress
	 */
	public abstract void onPublish(int progress);

	/**
	 * change song
	 * implemented by the sub-class
	 * service connect with UI Thread
	 * @param position position in list
	 */
	public abstract void onChange(int position);
}
