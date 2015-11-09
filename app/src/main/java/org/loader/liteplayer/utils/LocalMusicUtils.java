package org.loader.liteplayer.utils;

import java.util.ArrayList;

import org.loader.liteplayer.application.App;
import org.loader.liteplayer.pojo.Music;
import org.loader.liteplayer.pojo.SearchResult;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;


public class LocalMusicUtils {
	/**
	 * Get uri of music by id
	 * @deprecated
	 * @param musicId
	 * @return
	 */
	public static String queryMusicById(int musicId) {
		String result = null;
		Cursor cursor = App.sContext.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media.DATA },
				MediaStore.Audio.Media._ID + "=?",
				new String[] { String.valueOf(musicId) }, null);

		for (cursor.moveToFirst(); !cursor.isAfterLast();) {
			result = cursor.getString(0);
			break;
		}

		cursor.close();
		return result;
	}

	public static ArrayList<SearchResult> queryResult(String dirName) {
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		Cursor cursor = App.sContext.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
				MediaStore.Audio.Media.DATA + " like ?",
				new String[] { dirName + "%" },
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if(cursor == null) return results;

		// id title singer data time image
		SearchResult result;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			// If is not music
			String isMusic = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC));
			if (isMusic != null && isMusic.equals("")) continue;

			String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
			String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
			String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
			if(isRepeat(title, artist)) continue;

			result = new SearchResult();
			result.setMusicName(title);
			result.setArtist(artist);
			result.setAlbum(album);
			results.add(result);
		}

		cursor.close();
		return results;
	}

	/**
	 * Get all musics under dir
	 * @param dirName
	 */
	public static ArrayList<Music> queryMusic(String dirName) {
		ArrayList<Music> results = new ArrayList<Music>();
		Cursor cursor = App.sContext.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
				MediaStore.Audio.Media.DATA + " like ?",
				new String[] { dirName + "%" },
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if(cursor == null) return results;
		
		// id title singer data time image
		Music music;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			// If is not music
			String isMusic = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC));
			if (isMusic != null && isMusic.equals("")) continue;
			
			String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
			String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
			
			if(isRepeat(title, artist)) continue;
			
			music = new Music();
			music.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
			music.setTitle(title);
			music.setArtist(artist);
			music.setUri(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
			music.setLength(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
			music.setImage(getAlbumImage(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))));
			results.add(music);
		}

		cursor.close();
		return results;
	}
	
	/**
	 * Judge whether is repeat by Name and Artist
	 * @param title
	 * @param artist
	 * @return
	 */
	public static boolean isRepeat(String title, String artist) {
		for(Music music : MusicUtils.sMusicList) {
			if(title.equals(music.getTitle()) && artist.equals(music.getArtist())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get Image by id
	 * @param albumId
	 * @return
	 */
	private static String getAlbumImage(int albumId) {
		String result = "";
		Cursor cursor = null;
		try {
			cursor = App.sContext.getContentResolver().query(
					Uri.parse("content://media/external/audio/albums/"
							+ albumId), new String[] { "album_art" }, null,
					null, null);
			for (cursor.moveToFirst(); !cursor.isAfterLast();) {
				result = cursor.getString(0);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != cursor) {
				cursor.close();
			}
		}
		return null == result ? null : result;
	}
}
