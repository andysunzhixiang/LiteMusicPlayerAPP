package org.loader.liteplayer.application;

import org.loader.liteplayer.service.PlayService;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.WindowManager;


public class App extends Application {
	public static Context sContext;
	public static int sScreenWidth;
	public static int sScreenHeight;
	
	@Override
	public void onCreate() {
		super.onCreate();
		sContext = getApplicationContext();
		
		startService(new Intent(this, PlayService.class));

		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		sScreenWidth = dm.widthPixels;
		sScreenHeight = dm.heightPixels;
	}
	
}
