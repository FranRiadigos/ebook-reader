package com.saibi.ereader;

import android.app.Application;
import android.content.Context;

public class EReaderApplication extends Application {

	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();

		EReaderApplication.context = getApplicationContext();
	}

	public static Context getAppContext() {
		return EReaderApplication.context;
	}
}
