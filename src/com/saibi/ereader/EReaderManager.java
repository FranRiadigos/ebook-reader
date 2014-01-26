package com.saibi.ereader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;
import com.saibi.ereader.domain.DbxFileInfo;

/**
 * Gestor que maneja peticiones contra la Api de dropbox
 * 
 * Lo usamos tanto para conectarnos, como para recuperar los archivos en una tarea asincrona.
 */
public class EReaderManager {
	private static final String TAG = "DropboxManager";
	
    final static public String APP_KEY = "CHANGE_ME";
	final static public String APP_SECRET = "CHANGE_ME_SECRET";

    final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;

    // You don't need to change these, leave them alone.
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";

	private static EReaderManager mInstance;
	
	private DropboxAPI<AndroidAuthSession> mDropboxAPI;
	
	public interface OnFilesLoadedCallback {
		public void onFilesLoaded(List<DbxFileInfo> data);
	}
	
	private EReaderManager() {
		
		AndroidAuthSession session = buildSession();
		mDropboxAPI = new DropboxAPI<AndroidAuthSession>(session);
	}
	
	public static EReaderManager getInstance() {
		if(null == mInstance)
			mInstance = new EReaderManager();
		
		return mInstance;
	}
	
    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys();
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }

        return session;
    }
	
	/**
	 * Metodo para conectarse a la cuenta de dropbox
	 * 
	 * @param activity		Contexto de la actividad que lanza el metodo
	 */
	public void doLogin(Activity activity)
	{
		if(!isLoggedIn())
			mDropboxAPI.getSession().startAuthentication(activity);
	}
	
	
	public boolean isLoggedIn()
	{
		return mDropboxAPI.getSession().isLinked();
	}
	
	
	public void finishAuthentication() throws IllegalStateException
	{
		AndroidAuthSession session = mDropboxAPI.getSession();
		
        // The next part must be inserted in the onResume() method of the
        // activity from which session.startAuthentication() was called, so
        // that Dropbox authentication completes properly.
        if (session.authenticationSuccessful()) {
            // Mandatory call to complete the auth
            session.finishAuthentication();

            // Store it locally in our app for later use
            TokenPair tokens = session.getAccessTokenPair();
            storeKeys(tokens.key, tokens.secret);
        }
	}


    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     *
     * @return Array of [access_key, access_secret], or null if none stored
     */
    private String[] getKeys() {
        SharedPreferences prefs = EReaderApplication.getAppContext().getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
        	String[] ret = new String[2];
        	ret[0] = key;
        	ret[1] = secret;
        	return ret;
        } else {
        	return null;
        }
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void storeKeys(String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = EReaderApplication.getAppContext().getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }
	
	
	public void getFilesByExtension(String path, String fileExtension, 
			int maxFiles, OnFilesLoadedCallback callback) {
	        
        DbxFilesTaskLoader task = new DbxFilesTaskLoader(path, fileExtension, maxFiles, callback);
        task.execute();
	}
	
	
	public class DbxFilesTaskLoader extends AsyncTask<Void, Integer, List<DbxFileInfo>>
	{
		private String mPath;
		private String mFileExtension;
		private int mMaxFiles;
		private OnFilesLoadedCallback mCallback;
		
		public DbxFilesTaskLoader(String path, String fileExtension, 
				int maxFiles, OnFilesLoadedCallback callback) {
			
			mPath = path;
			mFileExtension = fileExtension;
			mMaxFiles = maxFiles;
			mCallback = callback;
		}

		@Override
		protected List<DbxFileInfo> doInBackground(Void... params) {
				
			List<Entry> fileList;
			try {
				fileList = mDropboxAPI.search(mPath, mFileExtension, mMaxFiles, false);
				
				EpubReader reader = new EpubReader();
				
				ArrayList<DbxFileInfo> booksFound = new ArrayList<DbxFileInfo>();
				
	            for( Entry entry : fileList )
	            {
	                if (entry.mimeType.compareTo("application/epub+zip") == 0)
	                {
	                	InputStream is = mDropboxAPI.getFileStream(entry.path, null);
	                	
	                	Book book = reader.readEpub(is);
	                	
	                	DbxFileInfo fileInfo = new DbxFileInfo(book, entry);
	                	
	                	booksFound.add(fileInfo);
	                }
	            }          
	            
	            return booksFound;
	            
			} catch (DropboxException e) {
				
				Log.e(TAG, e.getMessage());
				
			} catch (IOException e) {
				
				Log.e(TAG, e.getMessage());
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(List<DbxFileInfo> result) {
			mCallback.onFilesLoaded(result);
		}
		
	}
	
}
