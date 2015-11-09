package org.loader.liteplayer.utils;

import org.loader.liteplayer.application.App;

import android.support.v4.view.ViewPager.PageTransformer;
import android.view.View;

/**
 * Viewpager transform animation
 */
public class PlayPageTransformer implements PageTransformer {

	@Override
	public void transformPage(View view, float position) {
		if(position < -1) { // [-Infinity,-1) left disappear
			view.setAlpha(0.0f);
		}else if(position <= 0) { // [-1,0] left and middle
			view.setAlpha(1 + position);
			view.setTranslationX(App.sScreenWidth * (-position));
		}else if(position <= 1) { // (0,1] right and middle
			view.setAlpha(1 + position);
		}else if(position > 1) { // (1,+Infinity] right disappear
			view.setAlpha(1.0f);
		}
	}
}
