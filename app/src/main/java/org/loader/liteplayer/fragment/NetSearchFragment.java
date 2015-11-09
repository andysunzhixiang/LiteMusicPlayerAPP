package org.loader.liteplayer.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.loader.liteplayer.R;
import org.loader.liteplayer.activity.MainActivity;
import org.loader.liteplayer.adapter.SearchResultAdapter;
import org.loader.liteplayer.engine.SearchMusic;
import org.loader.liteplayer.engine.SongsRecommendation;
import org.loader.liteplayer.pojo.SearchResult;
import org.loader.liteplayer.utils.MobileUtils;

import java.util.ArrayList;


public class NetSearchFragment extends Fragment
					implements OnClickListener {
	protected static final String TAG = NetSearchFragment.class.getSimpleName();

	private MainActivity mActivity;

	private LinearLayout mSearchShowLinearLayout;
	private LinearLayout mSearchLinearLayout;
	private ImageButton mSearchButton;
	private EditText mSearchEditText;
	private ListView mSearchResultListView;
	private ProgressBar mSearchProgressBar;
	private TextView mFooterView;


	private SearchResultAdapter mSearchResultAdapter;
	private ArrayList<SearchResult> mResultData = new ArrayList<SearchResult>();

	private int mPage = 0;
	private int mLastItem;
	private boolean hasMoreData = true;

	private boolean isFirstShown = true;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater,
		ViewGroup container, Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.search_music_layout, null);
		setupViews(layout);

		return layout;
	}


	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		// 当Fragment可见且是第一次加载时
		if (isVisibleToUser && isFirstShown) {
			mSearchProgressBar.setVisibility(View.VISIBLE);
			mSearchResultListView.setVisibility(View.GONE);
			SongsRecommendation
				.getInstance()
				.setListener(
					new SongsRecommendation.OnRecommendationListener() {
						@Override
						public void onRecommend(
							ArrayList<SearchResult> results) {
							if (results == null || results.isEmpty())
								return;
							mSearchProgressBar.setVisibility(View.GONE);
							mSearchResultListView
									.setVisibility(View.VISIBLE);
							mResultData.clear();
							mResultData.addAll(results);
							mSearchResultAdapter.notifyDataSetChanged();
						}
					}).get();
			isFirstShown = false;
		}
	}

	private void setupViews(View layout) {
		mSearchShowLinearLayout = (LinearLayout) layout
				.findViewById(R.id.ll_search_btn_container);
		mSearchLinearLayout = (LinearLayout) layout
				.findViewById(R.id.ll_search_container);
		mSearchButton = (ImageButton) layout.findViewById(R.id.ib_search_btn);
		mSearchEditText = (EditText) layout
				.findViewById(R.id.et_search_content);
		mSearchResultListView = (ListView) layout
				.findViewById(R.id.lv_search_result);
		mSearchProgressBar = (ProgressBar) layout
				.findViewById(R.id.pb_search_wait);
		mFooterView = buildFooterView();

		mSearchShowLinearLayout.setOnClickListener(this);
		mSearchButton.setOnClickListener(this);

		mSearchResultListView.addFooterView(mFooterView);

		mSearchResultAdapter = new SearchResultAdapter(mResultData);
		mSearchResultListView.setAdapter(mSearchResultAdapter);
		mSearchResultListView.setOnScrollListener(mListViewScrollListener);
	}

	private TextView buildFooterView() {
		TextView footerView = new TextView(mActivity);
		footerView.setText("Loaf next page...");
		footerView.setGravity(Gravity.CENTER);
		footerView.setVisibility(View.GONE);
		return footerView;
	}





	private OnScrollListener mListViewScrollListener =
			new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (mLastItem == mSearchResultAdapter.getCount() && hasMoreData
					&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				String searchText = mSearchEditText.getText().toString().trim();
				if (TextUtils.isEmpty(searchText))
					return;

				mFooterView.setVisibility(View.VISIBLE);
				startSearch(searchText);
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			mLastItem = firstVisibleItem + visibleItemCount;
		}
	};

	private void search() {
		MobileUtils.hideInputMethod(mSearchEditText);
		String content = mSearchEditText.getText().toString().trim();
		if (TextUtils.isEmpty(content)) {
			Toast.makeText(mActivity, "Keyword", Toast.LENGTH_SHORT).show();
			return;
		}
		mPage = 0;
		mSearchProgressBar.setVisibility(View.VISIBLE);
		mSearchResultListView.setVisibility(View.GONE);
		startSearch(content);
	}

	private void startSearch(String content) {
		SearchMusic.getInstance()
			.setListener(new SearchMusic.OnSearchResultListener() {
				@Override
				public void onSearchResult(ArrayList<SearchResult> results) {
					if (mPage == 1) {
						hasMoreData = true;
						mSearchProgressBar.setVisibility(View.GONE);
						mSearchResultListView.setVisibility(View.VISIBLE);
					}
					mFooterView.setVisibility(View.GONE);
					if (results == null || results.isEmpty()) {
						hasMoreData = false;
						return;
					}
					if (mPage == 1)
						mResultData.clear();
					mResultData.addAll(results);
					mSearchResultAdapter.notifyDataSetChanged();
				}
			}).search(content, ++mPage);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_search_btn_container:
			mActivity.hideIndicator();
			mSearchShowLinearLayout.setVisibility(View.GONE);
			mSearchLinearLayout.setVisibility(View.VISIBLE);
			break;
		case R.id.ib_search_btn:
			mActivity.showIndicator();
			mSearchShowLinearLayout.setVisibility(View.VISIBLE);
			mSearchLinearLayout.setVisibility(View.GONE);
			search();
			break;
		}
	}
}