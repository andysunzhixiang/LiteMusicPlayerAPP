package org.loader.liteplayer.utils;

import org.loader.liteplayer.application.App;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

public class ImageTools {
	/**
	 * scale Bitmap image
	 * @param bmp
	 * @return
	 */
	public static Bitmap scaleBitmap(Bitmap bmp) {
		return scaleBitmap(bmp, (int) (App.sScreenWidth * 0.13));
	}
	
	/**
	 * scale Bitmap image
	 * @param bmp
	 * @param size
	 * @return
	 */
	public static Bitmap scaleBitmap(Bitmap bmp, int size) {
		return Bitmap.createScaledBitmap(bmp, size, size, true);
	}
	
	/**
	 * scale Bitmap image by uri
	 * @param uri
	 * @return
	 */
	public static Bitmap scaleBitmap(String uri, int size) {
		return scaleBitmap(BitmapFactory.decodeFile(uri), size);
	}
	
	/**
	 * scale Bitmap image
	 * @param uri
	 * @return
	 */
	private static Bitmap scaleBitmap(String uri) {
		return scaleBitmap(BitmapFactory.decodeFile(uri));
	}
	
	/**
	 * scale Bitmap image
	 * @param res
	 * @return
	 */
	public static Bitmap scaleBitmap(int res) {
		return scaleBitmap(BitmapFactory.decodeResource(
				App.sContext.getResources(), res));
	}
	
	/**
	 * create Circle Bitmap
	 * @deprecated
	 * @param src
	 * @return
	 */
	private static Bitmap createCircleBitmap(Bitmap src) {
		int size = (int) (App.sScreenWidth * 0.13);
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setARGB(255, 241, 239, 229);
		
		Bitmap target = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(target);
		canvas.drawCircle(size / 2, size / 2, size / 2, paint);
		
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		canvas.drawBitmap(src, 0, 0, paint);
		
		return target;
	}
	
	/**
	 * @deprecated
	 * @param uri
	 * @return
	 */
	private static Bitmap createCircleBitmap(String uri) {
		return createCircleBitmap(BitmapFactory.decodeFile(uri));
	}
	
	/**
	 * @deprecated
	 * @param res
	 * @return
	 */
	private static Bitmap createCircleBitmap(int res) {
		return createCircleBitmap(BitmapFactory.decodeResource(App.sContext.getResources(), res));
	}
}
