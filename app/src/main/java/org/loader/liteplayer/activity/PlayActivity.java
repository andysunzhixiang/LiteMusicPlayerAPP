package org.loader.liteplayer.activity;

import java.util.ArrayList;

import org.loader.liteplayer.R;
import org.loader.liteplayer.application.App;
import org.loader.liteplayer.pojo.Music;
import org.loader.liteplayer.ui.CDView;
import org.loader.liteplayer.ui.LrcView;
import org.loader.liteplayer.ui.PagerIndicator;
import org.loader.liteplayer.utils.ImageTools;
import org.loader.liteplayer.utils.MusicIconLoader;
import org.loader.liteplayer.utils.MusicUtils;
import org.loader.liteplayer.utils.PlayPageTransformer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;


public class PlayActivity extends BaseActivity implements OnClickListener {

	private LinearLayout mPlayContainer;
	private ImageView mPlayBackImageView; // back button
	private TextView mMusicTitle; // music title
	private ViewPager mViewPager; // cd or lrc
	private CDView mCdView; // cd
	private SeekBar mPlaySeekBar; // seekbar
	private ImageButton mStartPlayButton; // start or pause
	private TextView mSingerTextView; // singer
	private LrcView mLrcViewOnFirstPage; // single line lrc
	private LrcView mLrcViewOnSecondPage; // 7 lines lrc
	private PagerIndicator mPagerIndicator; // indicator

	// cd view and lrc view
	private ArrayList<View> mViewPagerContent = new ArrayList<View>(2);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.play_activity_layout);
		setupViews();
	}

	/**
	 * Init view
	 */
	private void setupViews() {
		mPlayContainer = (LinearLayout) findViewById(R.id.ll_play_container);
		mPlayBackImageView = (ImageView) findViewById(R.id.iv_play_back);
		mMusicTitle = (TextView) findViewById(R.id.tv_music_title);
		mViewPager = (ViewPager) findViewById(R.id.vp_play_container);
		mPlaySeekBar = (SeekBar) findViewById(R.id.sb_play_progress);
		mStartPlayButton = (ImageButton) findViewById(R.id.ib_play_start);
		mPagerIndicator = (PagerIndicator) findViewById(R.id.pi_play_indicator);

		MarginLayoutParams p = (MarginLayoutParams) mPlaySeekBar
				.getLayoutParams();
		p.leftMargin = (int) (App.sScreenWidth * 0.1);
		p.rightMargin = (int) (App.sScreenWidth * 0.1);

		mPlaySeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);

		initViewPagerContent();

		mViewPager.setPageTransformer(true, new PlayPageTransformer());
		mPagerIndicator.create(mViewPagerContent.size());
		mViewPager.setOnPageChangeListener(mPageChangeListener);
		mViewPager.setAdapter(mPagerAdapter);

		mPlayBackImageView.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		allowBindService();
	}

	@Override
	protected void onPause() {
		allowUnbindService();
		super.onPause();
	}

	private OnPageChangeListener mPageChangeListener =
			new OnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			if (position == 0) {
				if (mPlayService.isPlaying())
					mCdView.start();
			} else {
				mCdView.pause();
			}
			mPagerIndicator.current(position);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	};

	/**
	 * Pull seek bar
	 */
	private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener =
			new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			int progress = seekBar.getProgress();
			mPlayService.seek(progress);
			mLrcViewOnFirstPage.onDrag(progress);
			mLrcViewOnSecondPage.onDrag(progress);
		}
	};

	private PagerAdapter mPagerAdapter = new PagerAdapter() {
		@Override
		public int getCount() {
			return mViewPagerContent.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

		/**
		 * PagerAdapter Preload Item.
		 */
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mViewPagerContent.get(position));
			return mViewPagerContent.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}
	};

	/**
	 * Init viewpager content
	 */
	private void initViewPagerContent() {
		View cd = View.inflate(this, R.layout.play_pager_item_1, null);
		mCdView = (CDView) cd.findViewById(R.id.play_cdview);
		mSingerTextView = (TextView) cd.findViewById(R.id.play_singer);
		mLrcViewOnFirstPage = (LrcView) cd.findViewById(R.id.play_first_lrc);

		View lrcView = View.inflate(this, R.layout.play_pager_item_2, null);
		mLrcViewOnSecondPage = (LrcView) lrcView
				.findViewById(R.id.play_first_lrc_2);

		mViewPagerContent.add(cd);
		mViewPagerContent.add(lrcView);
	}



	/**
	 *
	 * @param view
	 */
	public void pre(View view) {
		mPlayService.pre();
	}

	/**
	 *
	 * @param view
	 */
	public void play(View view) {
		if (mPlayService.isPlaying()) {
			mPlayService.pause(); // Pause
			mCdView.pause();
			mStartPlayButton
					.setImageResource(R.drawable.player_btn_play_normal);
		} else {
			onPlay(mPlayService.resume()); // Play
		}
	}

	/**
	 *
	 * @param view
	 */
	public void next(View view) {
		mPlayService.next();
	}

	/**
	 *
	 * @param position
	 */
	private void onPlay(int position) {
		Music music = MusicUtils.sMusicList.get(position);

		mMusicTitle.setText(music.getTitle());
		mSingerTextView.setText(music.getArtist());
		mPlaySeekBar.setMax(music.getLength());
		Bitmap bmp = MusicIconLoader.getInstance().load(music.getImage());
		if (bmp == null)
			bmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_launcher);
		mCdView.setImage(ImageTools.scaleBitmap(bmp,
				(int) (App.sScreenWidth * 0.8)));

		if (mPlayService.isPlaying()) {
			mCdView.start();
			mStartPlayButton
					.setImageResource(R.drawable.player_btn_pause_normal);
		} else {
			mCdView.pause();
			mStartPlayButton
					.setImageResource(R.drawable.player_btn_play_normal);
		}
	}

	private void setLrc(int position) {
		Music music = MusicUtils.sMusicList.get(position);
		String lrcPath = MusicUtils.getLrcDir() + music.getTitle() + ".lrc";
		mLrcViewOnFirstPage.setLrcPath(lrcPath);
		mLrcViewOnSecondPage.setLrcPath(lrcPath);
	}

	@Override
	public void onPublish(int progress) {
		mPlaySeekBar.setProgress(progress);
		if (mLrcViewOnFirstPage.hasLrc())
			mLrcViewOnFirstPage.changeCurrent(progress);
		if (mLrcViewOnSecondPage.hasLrc())
			mLrcViewOnSecondPage.changeCurrent(progress);
	}

	@Override
	public void onChange(int position) {
		onPlay(position);
		setLrc(position);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_play_back:
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}