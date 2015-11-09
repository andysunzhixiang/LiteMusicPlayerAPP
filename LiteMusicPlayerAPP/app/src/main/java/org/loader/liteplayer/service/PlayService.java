package org.loader.liteplayer.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.loader.liteplayer.R;
import org.loader.liteplayer.activity.PlayActivity;
import org.loader.liteplayer.utils.Constants;
import org.loader.liteplayer.utils.ImageTools;
import org.loader.liteplayer.utils.L;
import org.loader.liteplayer.utils.MusicIconLoader;
import org.loader.liteplayer.utils.MusicUtils;
import org.loader.liteplayer.utils.SpUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.widget.RemoteViews;


public class PlayService extends Service implements
		MediaPlayer.OnCompletionListener {

	private static final String TAG =
			PlayService.class.getSimpleName();

	private SensorManager mSensorManager;

	private MediaPlayer mPlayer;
	private OnMusicEventListener mListener;
	private int mPlayingPosition;
	private WakeLock mWakeLock = null;//Prevent service stopping from screen lock
	private boolean isShaking;
	private Notification notification;
	private RemoteViews remoteViews;
	private NotificationManager notificationManager;

	private ExecutorService mProgressUpdatedListener = Executors
			.newSingleThreadExecutor();

	private MyBroadCastReceiver receiver;

	public class PlayBinder extends Binder {
		public PlayService getService() {
			return PlayService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		mSensorManager.registerListener(mSensorEventListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
		return new PlayBinder();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate() {
		super.onCreate();
		acquireWakeLock();
		mSensorManager = (SensorManager)
				getSystemService(Context.SENSOR_SERVICE);
		
		MusicUtils.initMusicList();
		mPlayingPosition = (Integer)
				SpUtils.get(this, Constants.PLAY_POS, 0);

		Uri uri = Uri.parse(MusicUtils.sMusicList.get(
				getPlayingPosition()).getUri());
		mPlayer = MediaPlayer.create(PlayService.this,uri);
		mPlayer.setOnCompletionListener(this);

		mProgressUpdatedListener.execute(mPublishProgressRunnable);
		

		PendingIntent pendingIntent = PendingIntent
				.getActivity(PlayService.this,
				0, new Intent(PlayService.this, PlayActivity.class), 0);
		remoteViews = new RemoteViews(getPackageName(),
				R.layout.play_notification);
		notification = new Notification(R.drawable.ic_launcher,
				"Music is playing", System.currentTimeMillis());
		notification.contentIntent = pendingIntent;
		notification.contentView = remoteViews;
		//Make the notification always disappear
		notification.flags =Notification.FLAG_ONGOING_EVENT;
		
		Intent intent = new Intent(PlayService.class.getSimpleName());
		intent.putExtra("BUTTON_NOTI", 1);
		PendingIntent preIntent = PendingIntent.getBroadcast(
				PlayService.this,
				1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(
				R.id.music_play_pre, preIntent);
		
		intent.putExtra("BUTTON_NOTI", 2);
		PendingIntent pauseIntent = PendingIntent.getBroadcast(
				PlayService.this,
				2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(
				R.id.music_play_pause, pauseIntent);
		
		intent.putExtra("BUTTON_NOTI", 3);
		PendingIntent nextIntent = PendingIntent.getBroadcast
				(PlayService.this,
				3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(
				R.id.music_play_next, nextIntent);
		
		notificationManager = (NotificationManager)
				getSystemService(NOTIFICATION_SERVICE);
		setRemoteViews();
		
		/**
		 *register Broadcast Receiver
		 * Listening button press event of notification
		 */
		IntentFilter filter = new IntentFilter(
				PlayService.class.getSimpleName());
		receiver = new MyBroadCastReceiver();
		registerReceiver(receiver, filter);
	}
	
	public void setRemoteViews(){
		L.l(TAG, "Notification start - setRemoteViews()");
		remoteViews.setTextViewText(R.id.music_name,
				MusicUtils.sMusicList.get(
						getPlayingPosition()).getTitle());
		remoteViews.setTextViewText(R.id.music_author,
				MusicUtils.sMusicList.get(
						getPlayingPosition()).getArtist());
		Bitmap icon = MusicIconLoader.getInstance().load(
				MusicUtils.sMusicList.get(
						getPlayingPosition()).getImage());
		remoteViews.setImageViewBitmap(R.id.music_icon,icon == null
				? ImageTools.scaleBitmap(R.drawable.ic_launcher)
						: ImageTools
				.scaleBitmap(icon));
		if (isPlaying()) {
			remoteViews.setImageViewResource(R.id.music_play_pause,
					R.drawable.btn_notification_player_stop_normal);
		}else {
			remoteViews.setImageViewResource(R.id.music_play_pause,
					R.drawable.btn_notification_player_play_normal);
		}
		//Update notification
		notificationManager.notify(5, notification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startForeground(0, notification);//
		return Service.START_STICKY;
	}

	/**
	 * Accelerometer sensor to change song.
	 */
	private SensorEventListener mSensorEventListener =
			new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (isShaking)
				return;

			if (Sensor.TYPE_ACCELEROMETER == event.sensor.getType()) {
				float[] values = event.values;
				/**
				 * Shake the cellphone to change to change song.
				 */
				if (Math.abs(values[0]) > 8 && Math.abs(values[1]) > 8
						&& Math.abs(values[2]) > 8) {
					isShaking = true;
					next();

					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							isShaking = false;
						}
					}, 200);
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	/**
	 * Update Progress thread
	 */
	private Runnable mPublishProgressRunnable = new Runnable() {
		@Override
		public void run() {
			while (true) {
				if (mPlayer != null && mPlayer.isPlaying()
						&& mListener != null) {
					mListener.onPublish(mPlayer.getCurrentPosition());
				}
			SystemClock.sleep(200);
			}
		}
	};

	/**
	 * setup callback
	 * 
	 * @param
	 */
	public void setOnMusicEventListener(OnMusicEventListener l) {
		mListener = l;
	}

	/**
	 * Player
	 * 
	 * @param position
	 *            position of music in the list
	 * @return current position
	 */
	public int play(int position) {
		L.l(TAG, "play(int position) method");
		if (position < 0)
			position = 0;
		if (position >= MusicUtils.sMusicList.size())
			position = MusicUtils.sMusicList.size() - 1;

		try {
			mPlayer.reset();
			mPlayer.setDataSource(MusicUtils
					.sMusicList.get(position).getUri());
			mPlayer.prepare();

			start();
			if (mListener != null)
				mListener.onChange(position);
		} catch (Exception e) {
			e.printStackTrace();
		}

		mPlayingPosition = position;
		SpUtils.put(Constants.PLAY_POS, mPlayingPosition);
		setRemoteViews();
		return mPlayingPosition;
	}

	/**
	 * Continue play
	 * 
	 * @return current playing position default is 0
	 */
	public int resume() {
		if (isPlaying())
			return -1;
		mPlayer.start();
		setRemoteViews();
		return mPlayingPosition;
	}

	/**
	 * pause
	 * 
	 * @return current playing position
	 */
	public int pause() {
		if (!isPlaying())
			return -1;
		mPlayer.pause();
		setRemoteViews();
		return mPlayingPosition;
	}

	/**
	 * pause and cancel Notification
	 *
	 * @return current playing position
	 */
	public void cancelNotification() {
		notificationManager.cancel(5);
	}

	/**
	 * next
	 * 
	 * @return current playing position
	 */
	public int next() {
		if (mPlayingPosition >= MusicUtils.sMusicList.size() - 1) {
			return play(0);
		}
		return play(mPlayingPosition + 1);
	}

	/**
	 * previous
	 * 
	 * @return current playing position
	 */
	public int pre() {
		if (mPlayingPosition <= 0) {
			return play(MusicUtils.sMusicList.size() - 1);
		}
		return play(mPlayingPosition - 1);
	}

	/**
	 * Whether is playing.
	 * 
	 * @return
	 */
	public boolean isPlaying() {
		return null != mPlayer && mPlayer.isPlaying();
	}

	/**
	 * Get the position in the list of the playing music
	 * 
	 * @return
	 */
	public int getPlayingPosition() {
		return mPlayingPosition;
	}

	/**
	 * Get the playing music duration
	 * 
	 * @return
	 */
	public int getDuration() {
		if (!isPlaying())
			return 0;
		return mPlayer.getDuration();
	}

	/**
	 * pull somewhere to play
	 *
	 * @param msec
	 */
	public void seek(int msec) {
		if (!isPlaying())
			return;
		mPlayer.seekTo(msec);
	}

	/**
	 * start to play
	 */
	private void start() {
		mPlayer.start();
	}

	/**
	 * Auto next song
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {
		next();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		L.l("play service", "unbind");
		mSensorManager.unregisterListener(mSensorEventListener);
		return true;
	}

	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		if (mListener != null)
			mListener.onChange(mPlayingPosition);
	}

	@Override
	public void onDestroy() {
		L.l(TAG, "PlayService.java onDestroy() be called. ");
		release();
		stopForeground(true);
		mSensorManager.unregisterListener(mSensorEventListener);
		unregisterReceiver(receiver);
		cancelNotification();
		super.onDestroy();
	}

	/**
	 * Release components when service is destroyed
	 */
	private void release() {
		if (!mProgressUpdatedListener.isShutdown())
			mProgressUpdatedListener.shutdownNow();
		mProgressUpdatedListener = null;
		releaseWakeLock();
		if (mPlayer != null)
			mPlayer.release();
		mPlayer = null;
	}

	// acquire the Power wake lock
	private void acquireWakeLock() {
		L.l(TAG, "acquiring the wake lock");
		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) this
					.getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
					| PowerManager.ON_AFTER_RELEASE, "");
			if (null != mWakeLock) {
				mWakeLock.acquire();
				L.l(TAG, "acquire wake lock success");
			}
		}
	}

	// Release the Power lock of device.
	private void releaseWakeLock() {
		L.l(TAG, "Releasing the Power lock");
		if (null != mWakeLock) {
			mWakeLock.release();
			mWakeLock = null;
			L.l(TAG, "Finishing releasing the Power lock");
		}
	}

	private class MyBroadCastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					PlayService.class.getSimpleName())) {
				L.l(TAG, "MyBroadCastReceiver class——>onReceive（）");
				L.l(TAG, "button_noti-->"
				+intent.getIntExtra("BUTTON_NOTI", 0));
				switch (intent.getIntExtra("BUTTON_NOTI", 0)) {
				case 1:
					pre();
					break;
				case 2:
					if (isPlaying()) {
						pause(); // pause
					} else {
						resume(); // play
					}
					break;
				case 3:
					next();
					break;
				case 4:
					if(isPlaying()){
						mPlayer.pause();
					}
					cancelNotification();
					break;
				default:
					break;
				}
			}
			if (mListener != null) {
				mListener.onChange(getPlayingPosition());
			}
		}
	}
	
	/**
	 * music play callback interface
	 */
	public interface OnMusicEventListener {
		 void onPublish(int percent);

		 void onChange(int position);
	}
}