package com.saibi.ereader.utils;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class ImageLazyLoader {

	public interface OnImageLoaded {
		public void onImageLoaded();
	}

	public static void loadFromInputStream(ImageView imageView, InputStream is,
			OnImageLoaded callback) {
		(new ImageLoaderTask(imageView, callback)).execute(is);
	}

	public static class ImageLoaderTask extends
			AsyncTask<InputStream, Void, Bitmap> {

		private OnImageLoaded mCallback;

		private final WeakReference<ImageView> imageViewReference;

		public ImageLoaderTask(ImageView imageView, OnImageLoaded callback) {
			mCallback = callback;
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected Bitmap doInBackground(InputStream... params) {

			InputStream input = params[0];
			return BitmapFactory.decodeStream(input);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (imageViewReference != null && result != null) {
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(result);
				}
			}
			mCallback.onImageLoaded();
		}

	}
}
