package org.loader.liteplayer.ui;

import org.loader.liteplayer.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * viewpager indicator
 */
public class Indicator extends LinearLayout {
	private Paint mPaint;
	
	private int mTop;
	private int mLeft;
	private int mWidth;
	private int mHeight;
	private int mColor;
	private int mChildCount;
	
	public Indicator(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.Indicator, 0, 0);
		mColor = ta.getColor(R.styleable.Indicator_color, 0XFF00FF00);
		mHeight = (int) ta.getDimension(R.styleable.Indicator_height, 2);
		ta.recycle();
		
		// init paint
		mPaint = new Paint();
		mPaint.setColor(mColor);
		mPaint.setAntiAlias(true);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mChildCount = getChildCount();  // get number of items
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mTop = getMeasuredHeight(); // indicator top position
		int width = getMeasuredWidth(); // total width
		int height = mTop + mHeight; // height
		mWidth = width / mChildCount; // indicator width
		
		setMeasuredDimension(width, height);
	}
	
	/**
	 * indicator scroll
	 * @param  position  initial position
	 * @param  offset   0 ~ 1
	 */
	public void scroll(int position, float offset) {
		mLeft = (int) ((position + offset) * mWidth);
		invalidate();
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		Rect rect = new Rect(mLeft, mTop, mLeft + mWidth, mTop + mHeight);
		canvas.drawRect(rect, mPaint);
		super.dispatchDraw(canvas);
	}
}