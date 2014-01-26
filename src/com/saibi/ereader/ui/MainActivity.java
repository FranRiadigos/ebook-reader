package com.saibi.ereader.ui;

import java.util.Comparator;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.android.AuthActivity;
import com.saibi.ereader.EReaderManager;
import com.saibi.ereader.EReaderManager.OnFilesLoadedCallback;
import com.saibi.ereader.R;
import com.saibi.ereader.adapters.SortedListAdapter;
import com.saibi.ereader.adapters.SortedListAdapter.OnSortedListFinished;
import com.saibi.ereader.domain.DbxFileInfo;

public class MainActivity extends Base {
	private static final String TAG = "MainActivity";
    
    EReaderManager mEReaderManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        checkAppKeySetup();
        
        mEReaderManager = EReaderManager.getInstance();
        
        /*
         * Despues de la primera vez que nos logeamos
         * guardamos el token de sesion en las SharedPreferences.
         * Por lo tanto, no necesitamos de nuevo lanzar la vista de dropbox
         * para logearnos. 
         */
        if(mEReaderManager.isLoggedIn())
        	showEbookList();
        else
        	mEReaderManager.doLogin(MainActivity.this);
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        
        if(!mEReaderManager.isLoggedIn())
        {
        	try {
        		mEReaderManager.finishAuthentication();
        		
        		showEbookList();
        		
            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.i(TAG, "Error authenticating", e);
            }
        }
    }
	
	private void showEbookList()
	{
		// Vamos a usar ListFragment para aprovechar los metodos utiles.
		replaceSupportFragment(EbookListFragment.newInstance(), R.id.base);
	}
	
	public static class EbookListFragment extends ListFragment 
		implements OnFilesLoadedCallback, ActionBar.OnNavigationListener, OnSortedListFinished {
		public static final String TAG = "MainActivity$EbookListFragment";
		
		private static EbookListFragment mInstance;
		
		/**
		 * Guardamos estos datos por ejemplo al girar el dispositivo
		 */
		private static final String COMPARATOR_TYPE_KEY = "COMPARATOR_TYPE_KEY";
	    private static final String STATE_SELECTED_NAVIGATION_ITEM = "STATE_SELECTED_NAVIGATION_ITEM";
		
	    /**
	     * TODO: Podriamos extraer estos datos en un Constant.java
	     * Util con la ofuscacion de codigo, a diferencia de valores en string.xml
	     */
		private static final String FILE_EXTENSION = ".epub";
		private static final String ROOT_PATH = "/";
		private static final int MAX_FILES = 10;
		
		private EbookSortedListAdapter mAdapter;
		
		private ActionBar mActionBar;
		
		private CustomComparator mComparator = new CustomComparator();
		private int mCurrentComparatorType = CustomComparator.BY_TITLE_NAME;
		
		/**
		 * Flag necesario para evitar que onNavigationItemSelected ejecute la ordenacion sin datos
		 */
		private boolean mFilesLoaded = false;
		
		public static EbookListFragment newInstance() {
			
			if(null == mInstance)
				mInstance = new EbookListFragment();
			
            return mInstance;
        }
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			
			mActionBar = activity.getActionBar();
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

	        // Set up the action bar to show a dropdown list.
			mActionBar.setDisplayShowTitleEnabled(false);
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

	        // Set up the dropdown list navigation in the action bar.
			mActionBar.setListNavigationCallbacks(
	                // Specify a SpinnerAdapter to populate the dropdown list.
					
					ArrayAdapter.createFromResource(getActionBarThemedContextCompat(), 
							R.array.sort_dropdown_values, 
							android.R.layout.simple_list_item_1) ,
	                this);
			
			mComparator.setType(mCurrentComparatorType);
		}

	    /**
	     * Backward-compatible version of {@link ActionBar#getThemedContext()} that
	     * simply returns the {@link android.app.Activity} if
	     * <code>getThemedContext</code> is unavailable.
	     */
	    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	    private Context getActionBarThemedContextCompat() {
	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
	            return mActionBar.getThemedContext();
	        } else {
	            return getActivity();
	        }
	    }
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			if(null != savedInstanceState)
			{
				if (savedInstanceState.containsKey(COMPARATOR_TYPE_KEY)) {
					mCurrentComparatorType = savedInstanceState.getInt(COMPARATOR_TYPE_KEY);
				}
				if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
					mActionBar.setSelectedNavigationItem(
		                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		        }
			}
			
			setEmptyText(getResources().getString(R.string.empty_list));
			
			mAdapter = new EbookSortedListAdapter(getActivity());
			
			setListAdapter(mAdapter);
			
			loadFilesList();

			setListShown(false);
		}
		
		/**
		 * Este metodo se lanza al menos 1 vez en cuanto se carga el layout
		 */
	    @Override
	    public boolean onNavigationItemSelected(int position, long id) {
	    	
	    	// Comprueba que se han cargado los datos
	    	if(mFilesLoaded)
	    	{
	    		setListShown(false);
	    		mAdapter.clear();
		    	mComparator.setType(position);
		    	mAdapter.sortBy(mComparator, this);
	    	}
	    	
	    	return true;
	    }
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putInt(COMPARATOR_TYPE_KEY, mCurrentComparatorType);
			outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
					mActionBar.getSelectedNavigationIndex());
		}
		
		private void loadFilesList() {
			
				EReaderManager
					.getInstance()
					.getFilesByExtension(ROOT_PATH, FILE_EXTENSION, MAX_FILES, this);
		}

		@Override
		public void onFilesLoaded(List<DbxFileInfo> data) {
			
			mAdapter.sortBy(data, mComparator, this);
			
			mFilesLoaded = true;
		}
		
		@Override
		public void onSortedListFinished() {
			setListShown(true);
		}
		
		/**
		 * TODO: Implementar EndlessAdapter
		 */
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
		
		private class CustomComparator implements Comparator<DbxFileInfo>{

			public static final int BY_TITLE_NAME    	= 0x00;
			public static final int BY_CREATION_DATE 	= 0x01;
			
			private int mComparatorType;
			
			public void setType(int type) {
				mComparatorType = type;
			}

	        @Override
	        public int compare(DbxFileInfo n1, DbxFileInfo n2) {
	        	switch(mComparatorType)
	        	{
	        		// comparamos por la fecha de ultima modificacion en dropbox
	        		// ya que la fecha del libro puede estar vacia !
		        	case BY_CREATION_DATE:
		        		return n1.getEntry().clientMtime.compareTo(n2.getEntry().clientMtime);
		        	
		        	default:
		        		return n1.getBook().getTitle().compareTo(n2.getBook().getTitle());
	        	}
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
