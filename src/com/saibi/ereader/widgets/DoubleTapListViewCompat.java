package com.saibi.ereader.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Double Tap List View Support Version Compatible
 */
public class DoubleTapListViewCompat extends ListView {

	private GestureDetector mDetector;
	private OnItemDoubleClickListener mDoubleClickListener;

	public interface OnItemDoubleClickListener {
		public void OnItemDoubleClick(AdapterView<?> parent, View view,
				int position, long id);
	}

	public DoubleTapListViewCompat(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mDetector = new GestureDetector(context,
				new DoubleClickGestureListener());
	}

	public DoubleTapListViewCompat(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDetector = new GestureDetector(context,
				new DoubleClickGestureListener());
	}

	public DoubleTapListViewCompat(Context context) {
		super(context);
		mDetector = new GestureDetector(context,
				new DoubleClickGestureListener());
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (!mDetector.onTouchEvent(ev))
			return super.onTouchEvent(ev);
		return true;
	}

	public void setOnItemDoubleClickListener(OnItemDoubleClickListener listener) {
		mDoubleClickListener = listener;
	}

	public OnItemDoubleClickListener getOnItemDoubleClickListener() {
		return mDoubleClickListener;
	}

	private class DoubleClickGestureListener extends
			GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (mDoubleClickListener != null) {

				int position = DoubleTapListViewCompat.this.pointToPosition(
						(int) e.getX(), (int) e.getY());

				if (position != INVALID_POSITION) {
					mDoubleClickListener.OnItemDoubleClick(
							DoubleTapListViewCompat.this,
							DoubleTapListViewCompat.this.getChildAt(position),
							position, DoubleTapListViewCompat.this.getAdapter()
									.getItemId(position));
					return true;
				}
			}
			return false;
		}
	}
}
