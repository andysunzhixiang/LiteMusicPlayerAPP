package org.loader.liteplayer.utils;

import java.io.File;
import java.util.ArrayList;

import org.loader.liteplayer.application.App;
import org.loader.liteplayer.pojo.Music;
import org.loader.liteplayer.pojo.SearchResult;

import android.os.Environment;

public class MusicUtils {
	// Music　List
	public static ArrayList<Music> sMusicList = new ArrayList<Music>();

	public static ArrayList<SearchResult> sSearchResultList = new ArrayList<SearchResult>();

	public static void initMusicList() {
		// Get Music List
		sMusicList.clear();
		sMusicList.addAll(LocalMusicUtils.queryMusic(getBaseDir()));
	}



	/**
	 * get root path of SD card
	 * @return
	 */
	public static String getBaseDir() {
		String dir = null;
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
			dir = Environment.getExternalStorageDirectory() + File.separator;
		} else {
			dir = App.sContext.getFilesDir() + File.separator;
		}

		return dir;
	}

	/**
	 * Get App local dir
	 * @return
	 */
	public static String getAppLocalDir() {
		String dir = null;

		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
			dir = Environment.getExternalStorageDirectory() + File.separator
					+ "liteplayer" + File.separator;
		} else {
			dir = App.sContext.getFilesDir() + File.separator + "liteplayer" + File.separator;
		}

		return mkdir(dir);
	}

	/**
	 * Get Music Dir
	 * @return
	 */
	public static String getMusicDir() {
		String musicDir = getAppLocalDir() + "music" + File.separator;
		return mkdir(musicDir);
	}

	/**
	 * Get Lrc Dir
	 * 
	 * @return
	 */
	public static String getLrcDir() {
		String lrcDir = getAppLocalDir() + "lrc" + File.separator;
		return mkdir(lrcDir);
	}

	/**
	 * Make a dir
	 * @param dir
	 * @return
	 */
	public static String mkdir(String dir) {
		File f = new File(dir);
		if (!f.exists()) {
			for (int i = 0; i < 5; i++) {
				if(f.mkdirs()) return dir;
			}
			return null;
		}
		
		return dir;
	}
}
