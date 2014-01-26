package com.saibi.ereader.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.android.AuthActivity;
import com.saibi.ereader.EReaderManager;
import com.saibi.ereader.R;
import com.saibi.ereader.fragments.EbookListFragment;

public class MainActivity extends Base {
	private static final String TAG = "MainActivity";

	EReaderManager mEReaderManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		checkAppKeySetup();

		mEReaderManager = EReaderManager.getInstance();

		/*
		 * Despues de la primera vez que nos logeamos guardamos el token de
		 * sesion en las SharedPreferences. Por lo tanto, no necesitamos de
		 * nuevo lanzar la vista de dropbox para logearnos.
		 */
		if (mEReaderManager.isLoggedIn())
			showEbookList();
		else
			mEReaderManager.doLogin(MainActivity.this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!mEReaderManager.isLoggedIn()) {
			try {
				mEReaderManager.finishAuthentication();

				showEbookList();

			} catch (IllegalStateException e) {
				showToast("Couldn't authenticate with Dropbox:"
						+ e.getLocalizedMessage());
				Log.i(TAG, "Error authenticating", e);
			}
		}
	}

	private void showEbookList() {
		// Vamos a usar ListFragment para aprovechar los metodos utiles.
		replaceSupportFragment(EbookListFragment.newInstance(), R.id.base);
	}

	private void checkAppKeySetup() {
		// Check to make sure that we have a valid app key
		if (EReaderManager.APP_KEY.startsWith("CHANGE")
				|| EReaderManager.APP_SECRET.startsWith("CHANGE")) {
			showToast("You must apply for an app key and secret from developers.dropbox.com, "
					+ "and add them to the DBRoulette ap before trying it.");
			finish();
			return;
		}

		// Check if the app has set up its manifest properly.
		Intent testIntent = new Intent(Intent.ACTION_VIEW);
		String scheme = "db-" + EReaderManager.APP_KEY;
		String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
		testIntent.setData(Uri.parse(uri));
		PackageManager pm = getPackageManager();
		if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
			showToast("URL scheme in your app's "
					+ "manifest is not set up correctly. You should have a "
					+ "com.dropbox.client2.android.AuthActivity with the "
					+ "scheme: " + scheme);
			finish();
		}
	}

	private void showToast(String msg) {
		Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		error.show();
	}

}
