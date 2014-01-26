package com.saibi.ereader.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.android.AuthActivity;
import com.saibi.ereader.EReaderManager;
import com.saibi.ereader.EReaderManager.OnFilesLoadedCallback;
import com.saibi.ereader.R;
import com.saibi.ereader.adapters.SortedListAdapter;
import com.saibi.ereader.domain.DbxFileInfo;

public class MainActivity extends Base {
	private static final String TAG = "MainActivity";
    
    EReaderManager mDropboxManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        checkAppKeySetup();
        
        mDropboxManager = EReaderManager.getInstance();
        
        if(mDropboxManager.isLoggedIn())
        	showEbookList();
        else
        	mDropboxManager.doLogin(MainActivity.this);
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        
        if(!mDropboxManager.isLoggedIn())
        {
        	try {
        		mDropboxManager.finishAuthentication();
        		
        		showEbookList();
        		
            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.i(TAG, "Error authenticating", e);
            }
        }
    }
	
	
	private void showEbookList()
	{
		replaceSupportFragment(EbookListFragment.newInstance(), R.id.base);
	}
	
	
	public static class EbookListFragment extends ListFragment implements OnFilesLoadedCallback {
		public static final String TAG = "MainActivity$EbookListFragment";
		
		private static EbookListFragment mInstance;
		
		private static final String FILE_EXTENSION = ".epub";
		private static final String ROOT_PATH = "/";
		private static final int MAX_FILES = 10;
		
		private EbookSortedListAdapter mAdapter;
		
		public static EbookListFragment newInstance() {
			
			if(null == mInstance)
				mInstance = new EbookListFragment();
			
            return mInstance;
        }
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			setEmptyText("No existen archivos de tipo .epub en la cuenta dropbox");
			
			mAdapter = new EbookSortedListAdapter(getActivity());
			
			setListAdapter(mAdapter);
			
			loadFilesList();

			setListShown(false);
		}
		
		private void loadFilesList() {
			
				EReaderManager
					.getInstance()
					.getFilesByExtension(ROOT_PATH, FILE_EXTENSION, MAX_FILES, this);
		}

		@Override
		public void onFilesLoaded(List<DbxFileInfo> data) {
			
			mAdapter.addAll(data);

			setListShown(true);
		}
		
		public class EbookSortedListAdapter extends SortedListAdapter<DbxFileInfo> {
			private static final String TAG = "EbookSortedListAdapter";
			
			public EbookSortedListAdapter(Context context) {
				super(context);
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) 
			{
				ViewHolder holder;

				try{
					
					DbxFileInfo fileInfo = getItem(position);
					
					if(null == convertView)
					{
						convertView = mInflater.inflate( R.layout.fragment_ebook_list, 
								parent, false );
						
						holder = new ViewHolder();
						
						holder.title 		= (TextView) convertView.findViewById(R.id.title);
						
						convertView.setTag(holder);
					} else {
		            	holder = (ViewHolder) convertView.getTag();
		            }
					
					holder.title.setText(fileInfo.getBook().getTitle());
					
				}catch(Exception e) {
					Log.w(TAG, e.getMessage());
				}
				
				return convertView;
			}
			
			private class ViewHolder 
			{
				TextView title;
			}
		}
		
	}

    
    private void checkAppKeySetup() {
        // Check to make sure that we have a valid app key
        if (EReaderManager.APP_KEY.startsWith("CHANGE") ||
        		EReaderManager.APP_SECRET.startsWith("CHANGE")) {
            showToast(	"You must apply for an app key and secret from developers.dropbox.com, " +
            			"and add them to the DBRoulette ap before trying it.");
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
            showToast("URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
            finish();
        }
    }
    
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
	
}
