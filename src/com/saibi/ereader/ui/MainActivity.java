package com.saibi.ereader.ui;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

import com.saibi.ereader.R;
import com.saibi.ereader.R.id;

public class MainActivity extends Base {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		replaceSupportFragment(EbookListFragment.newInstance(), R.id.base);
	}
	
	
	public static class EbookListFragment extends ListFragment {
		public static final String TAG = "MainActivity$EbookListFragment";
		
		private static EbookListFragment mInstance;
		
		public static EbookListFragment newInstance() {
			
			if(null == mInstance)
				mInstance = new EbookListFragment();
			
            return mInstance;
        }
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			setListShown(false);
		}
		
	}
	
}
