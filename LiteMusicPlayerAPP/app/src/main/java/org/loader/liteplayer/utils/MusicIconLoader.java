package org.loader.liteplayer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;


public class MusicIconLoader {
	private static MusicIconLoader sInstance;

	private LruCache<String, Bitmap> mCache;

	// return MusicIconLoader instance
	public synchronized static MusicIconLoader getInstance() {
		if (sInstance == null)
			sInstance = new MusicIconLoader();
		return sInstance;
	}

	// Constructor Init LruCache
	private MusicIconLoader() {
		int maxSize = (int) (Runtime.getRuntime().maxMemory() / 8);
		mCache = new LruCache<String, Bitmap>(maxSize) {
			protected int sizeOf(String key, Bitmap value) {
				return value.getByteCount();
			}
		};
	}

	//Get Image by Url
	public Bitmap load(final String uri) {
		if (uri == null)
			return null;

		final String key = Encrypt.md5(uri);
		Bitmap bmp = getFromCache(key);

		if (bmp != null)
			return bmp;

		bmp = BitmapFactory.decodeFile(uri);
		addToCache(key, bmp);
		return bmp;
	}

	private Bitmap getFromCache(final String key) {
		return mCache.get(key);
	}

	private void addToCache(final String key, final Bitmap bmp) {
		if (getFromCache(key) == null && key != null && bmp != null)
			mCache.put(key, bmp);
	}
}