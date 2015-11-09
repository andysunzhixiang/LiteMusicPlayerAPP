package org.loader.liteplayer.adapter;

import org.loader.liteplayer.R;
import org.loader.liteplayer.application.App;
import org.loader.liteplayer.utils.ImageTools;
import org.loader.liteplayer.utils.MusicIconLoader;
import org.loader.liteplayer.utils.MusicUtils;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 * 歌曲列表适配器
 */
public class MusicListAdapter extends BaseAdapter {
	private int mPlayingPosition;

	public void setPlayingPosition(int position) {
		mPlayingPosition = position;
	}
	
	public int getPlayingPosition() {
		return mPlayingPosition;
	}
	
	@Override
	public int getCount() {
		return MusicUtils.sMusicList.size();
	}

	@Override
	public Object getItem(int position) {
		return MusicUtils.sMusicList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder ;
		
		if(convertView == null) {
			convertView = View.inflate(App.sContext, R.layout.music_list_item, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.tv_music_list_title);
			holder.artist = (TextView) convertView.findViewById(R.id.tv_music_list_artist);
			holder.icon = (ImageView) convertView.findViewById(R.id.music_list_icon);
			holder.mark = convertView.findViewById(R.id.music_list_selected);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if(mPlayingPosition == position) {
			holder.mark.setVisibility(View.VISIBLE);
		}else {
			holder.mark.setVisibility(View.INVISIBLE);
		}
		
		Bitmap icon = MusicIconLoader.getInstance()
				.load(MusicUtils.sMusicList.get(position).getImage());
		holder.icon.setImageBitmap(icon == null ? 
				ImageTools.scaleBitmap(R.drawable.ic_launcher) : ImageTools.scaleBitmap(icon));
		
		holder.title.setText(MusicUtils.sMusicList.get(position).getTitle());
		holder.artist.setText(MusicUtils.sMusicList.get(position).getArtist());
		
		return convertView;
	}
	
	static class ViewHolder {
		ImageView icon;
		TextView title;
		TextView artist;
		View mark;
	}
}
