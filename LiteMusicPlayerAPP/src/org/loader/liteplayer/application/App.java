package org.loader.liteplayer.application;

import org.loader.liteplayer.service.DownloadService;
import org.loader.liteplayer.service.PlayService;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class App extends Application {
	public static Context sContext;
	public static int sScreenWidth;
	public static int sScreenHeight;
	
	@Override
	public void onCreate() {
		super.onCreate();
		sContext = getApplicationContext();
		
		startService(new Intent(this, PlayService.class));
		startService(new Intent(this, DownloadService.class));
		
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		sScreenWidth = dm.widthPixels;
		sScreenHeight = dm.heightPixels;
	}
	
}
