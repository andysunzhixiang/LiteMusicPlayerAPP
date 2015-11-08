package org.loader.liteplayer.fragment;

import java.io.File;

import org.loader.liteplayer.R;
import org.loader.liteplayer.activity.MainActivity;
import org.loader.liteplayer.activity.PlayActivity;
import org.loader.liteplayer.adapter.MusicListAdapter;
import org.loader.liteplayer.pojo.Music;
import org.loader.liteplayer.service.PlayService;
import org.loader.liteplayer.utils.ImageTools;
import org.loader.liteplayer.utils.L;
import org.loader.liteplayer.utils.MusicIconLoader;
import org.loader.liteplayer.utils.MusicUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;


public class LocalFragment extends Fragment implements OnClickListener {

	private ListView mMusicListView;
	private ImageView mMusicIcon;
	private TextView mMusicTitle;
	private TextView mMusicArtist;

	private ImageView mPreImageView;
	private ImageView mPlayImageView;
	private ImageView mNextImageView;
	private ImageView mExitImageView;

	private SeekBar mMusicProgress;

	private MusicListAdapter mMusicListAdapter = new MusicListAdapter();

	private MainActivity mActivity;

	private boolean isPause;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.local_music_layout, null);
		setupViews(layout);

		return layout;
	}

	/**
	 * After creating view, Callback to notify activity bind Play service.
	 */
	@Override
	public void onStart() {
		super.onStart();
		L.l("fragment", "onViewCreated");
		mActivity.allowBindService();
	}

	@Override
	public void onResume() {
		super.onResume();
		isPause = false;
	}

	@Override
	public void onPause() {
		isPause = true;
		super.onPause();
	}

	/**
	 * When stop， callback notify activity to unbind PlayService
	 */
	@Override
	public void onStop() {
		super.onStop();
		L.l("fragment", "onDestroyView");
		mActivity.allowUnbindService();
	}

	private void setupViews(View layout) {
		mMusicListView = (ListView) layout.findViewById(R.id.lv_music_list);
		mMusicIcon = (ImageView) layout.findViewById(R.id.iv_play_icon);
		mMusicTitle = (TextView) layout.findViewById(R.id.tv_play_title);
		mMusicArtist = (TextView) layout.findViewById(R.id.tv_play_artist);

		mPreImageView = (ImageView) layout.findViewById(R.id.iv_pre);
		mPlayImageView = (ImageView) layout.findViewById(R.id.iv_play);
		mNextImageView = (ImageView) layout.findViewById(R.id.iv_next);
		mExitImageView = (ImageView) layout.findViewById(R.id.iv_exit);

		mMusicProgress = (SeekBar) layout.findViewById(R.id.play_progress);

		mMusicListView.setAdapter(mMusicListAdapter);
		mMusicListView.setOnItemClickListener(mMusicItemClickListener);
		mMusicListView.setOnItemLongClickListener(mItemLongClickListener);

		mMusicIcon.setOnClickListener(this);
		mPreImageView.setOnClickListener(this);
		mPlayImageView.setOnClickListener(this);
		mNextImageView.setOnClickListener(this);
		mExitImageView.setOnClickListener(this);

	}

	/**
	 * Long click to delete music
	 */
	private OnItemLongClickListener mItemLongClickListener = 
			new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			final int pos = position;

			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setTitle("DELETE MUSIC");
			builder.setMessage("Confirm to delete this song?");
			builder.setPositiveButton("Delete",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Music music = MusicUtils.sMusicList.remove(pos);
						mMusicListAdapter.notifyDataSetChanged();
						if (new File(music.getUri()).delete()) {
							scanSDCard();
						}
					}
				});
			builder.setNegativeButton("Cancel", null);
			builder.create().show();
			return true;
		}
	};

	private OnItemClickListener mMusicItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			play(position);
		}
	};

	/**
	 * Send broadcast to notify system to scan music file.
	 */
	private void scanSDCard() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// whether SDK is over 4.4
			String[] paths = new String[]{
					Environment.getExternalStorageDirectory().toString()};
			MediaScannerConnection.scanFile(mActivity, paths, null, null);
		} else {
			Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
			intent.setClassName("com.android.providers.media",
					"com.android.providers.media.MediaScannerReceiver");
			intent.setData(Uri.parse("file://"+ MusicUtils.getMusicDir()));
			mActivity.sendBroadcast(intent);
		}
	}

	/**
	 * Highlight current playing item
	 * Make the playing item can be seen.
	 * @param position
	 */
	private void onItemPlay(int position) {
		//Make the the playing item can be seen.
		mMusicListView.smoothScrollToPosition(position);
		int prePlayingPosition = mMusicListAdapter.getPlayingPosition();
		if (prePlayingPosition >= mMusicListView.getFirstVisiblePosition()
				&& prePlayingPosition <= mMusicListView
						.getLastVisiblePosition()) {
			int preItem = prePlayingPosition
					- mMusicListView.getFirstVisiblePosition();
			((ViewGroup) mMusicListView.getChildAt(preItem)).getChildAt(0)
					.setVisibility(View.INVISIBLE);
		}

		mMusicListAdapter.setPlayingPosition(position);

		if (mMusicListView.getLastVisiblePosition() < position
				|| mMusicListView.getFirstVisiblePosition() > position)
			return;

		int currentItem = position - mMusicListView.getFirstVisiblePosition();
		((ViewGroup) mMusicListView.getChildAt(currentItem)).getChildAt(0)
				.setVisibility(View.VISIBLE);
	}

	/**
	 * play music item
	 * 
	 * @param position
	 */
	private void play(int position) {
		int pos = mActivity.getPlayService().play(position);
		onPlay(pos);
	}

	/**
	 * Play music control panel.
	 * 
	 * @param position
	 */
	public void onPlay(int position) {
		if (MusicUtils.sMusicList.isEmpty() || position < 0)
			return;
		//Set the total length of Progress.
		mMusicProgress.setMax(mActivity.getPlayService().getDuration());
		onItemPlay(position);

		Music music = MusicUtils.sMusicList.get(position);
		Bitmap icon = MusicIconLoader.getInstance().load(music.getImage());
		mMusicIcon.setImageBitmap(icon == null ? ImageTools
				.scaleBitmap(R.drawable.ic_launcher) : ImageTools
				.scaleBitmap(icon));
		mMusicTitle.setText(music.getTitle());
		mMusicArtist.setText(music.getArtist());

		if (mActivity.getPlayService().isPlaying()) {
			mPlayImageView.setImageResource(android.R.drawable.ic_media_pause);
		} else {
			mPlayImageView.setImageResource(android.R.drawable.ic_media_play);
		}
		//   New a thread to update the Notification
		new Thread(){
			@Override
			public void run() {
				super.run();
				mActivity.getPlayService().setRemoteViews();
			}
		}.start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_play_icon:
			//Move to detail play
			startActivity(new Intent(mActivity, PlayActivity.class));
			break;
		case R.id.iv_play:
			if (mActivity.getPlayService().isPlaying()) {
				mActivity.getPlayService().pause(); // pause
				mPlayImageView
						.setImageResource(android.R.drawable.ic_media_play);
			} else {
				onPlay(mActivity.getPlayService().resume()); // play
			}
			break;
		case R.id.iv_next:
			mActivity.getPlayService().next(); // next song
			break;
		case R.id.iv_pre:
			mActivity.getPlayService().pre(); // previous song
			break;
		case R.id.iv_exit:
			mActivity.stopService(new Intent(mActivity, PlayService.class)); // exit APP
			mActivity.finish();
			break;
		}
	}

	/**
	 * set progress bar(SeekBar)
	 * @param progress
	 */
	public void setProgress(int progress) {
		if (isPause)
			return;
		mMusicProgress.setProgress(progress);
	}

	/**
	 * Called by UI Thread MainActivity.java
	 */
	public void onMusicListChanged() {
		mMusicListAdapter.notifyDataSetChanged();
	}
}
