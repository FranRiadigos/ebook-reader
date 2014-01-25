package com.saibi.ereader.ui;

import com.saibi.ereader.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class Base extends FragmentActivity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.base);
	}
	
	/**
	 * Replace support fragments with null tag
	 * 
	 * @param frag Fragment
	 * @param containerId Integer
	 */
	public void replaceSupportFragment(Fragment frag, int containerId)
	{
		getSupportFragmentManager()
			.beginTransaction()
			.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.replace(containerId, frag).commit();
	}
}
