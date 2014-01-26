package com.saibi.ereader.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
		(new ImageLoaderTask(imageView, callback, false)).execute(is);
	}

	public static void loadFromInputStream(ImageView imageView, InputStream is) {
		(new ImageLoaderTask(imageView, false)).execute(is);
	}

	public static void loadFromInputStream(ImageView imageView, InputStream is,
			OnImageLoaded callback, boolean shrink, Integer... size) {
		(new ImageLoaderTask(imageView, callback, shrink, size)).execute(is);
	}

	public static void loadFromInputStream(ImageView imageView, InputStream is,
			boolean shrink, Integer... size) {
		(new ImageLoaderTask(imageView, shrink, size)).execute(is);
	}

	public static class ImageLoaderTask extends
			AsyncTask<InputStream, Void, Bitmap> {

		private OnImageLoaded mCallback;
		private boolean doShrink;
		private Integer[] imageSize;

		private final WeakReference<ImageView> imageViewReference;

		public ImageLoaderTask(ImageView imageView, boolean shrink,
				Integer... size) {
			doShrink = shrink;
			imageSize = size;
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		public ImageLoaderTask(ImageView imageView, OnImageLoaded callback,
				boolean shrink, Integer... size) {
			mCallback = callback;
			doShrink = shrink;
			imageSize = size;
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected Bitmap doInBackground(InputStream... params) {

			InputStream input = params[0];

			if (doShrink) {
				if (null == imageSize || imageSize.length == 0) {
					return decodeFile(input, 200);
				} else {
					return decodeFile(input, imageSize[0]);
				}
			}

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

			if (null != mCallback)
				mCallback.onImageLoaded();
		}

		private Bitmap decodeFile(InputStream in, int size) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len;
				while ((len = in.read(buffer)) > -1) {
					baos.write(buffer, 0, len);
				}
				baos.flush();
				InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
				InputStream is2 = new ByteArrayInputStream(baos.toByteArray());

				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(is1, null, o);

				final int IMAGE_MAX_SIZE = size;

				System.out.println("h:" + o.outHeight + " w:" + o.outWidth);
				int scale = 1;
				if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
					scale = (int) Math.pow(
							2,
							(int) Math.round(Math.log(IMAGE_MAX_SIZE
									/ (double) Math
											.max(o.outHeight, o.outWidth))
									/ Math.log(0.5)));
				}

				BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = scale;
				return BitmapFactory.decodeStream(is2, null, o2);
			} catch (Exception e) {
				return null;
			}
		}

	}
}
