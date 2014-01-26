package com.saibi.ereader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.InvalidParameter;
import com.dropbox.sync.android.DbxException.NotFound;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFileInfo;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;

/**
 * @author Saibi
 *
 * Gestor que maneja peticiones contra la Api de dropbox
 * 
 * Lo usamos tanto para conectarnos, como para recuperar los archivos en una tarea asincrona.
 */
public class EReaderManager {
	private static final String TAG = "DropboxManager";
	
    final static public String APP_KEY = "CHANGE_ME";
	final static public String APP_SECRET = "CHANGE_ME_SECRET";

	private static EReaderManager mInstance;
	
	private DbxAccountManager mDbxAccountManager;
	private DbxFileSystem mDbxFileSystem;
	
	
	public interface OnFilesLoadedCallback<T> {
		public void onFilesLoaded(List<T> data);
	}
	
	
	/**
	 * No debemos instancia el objeto directamente
	 */
	private EReaderManager() {
		
		/*
		 * Obtenemos el Contexto global de la aplicacion, para no depender del contexto de la actividad
		 * en caso de ser finalizada.
		 * Esto es necesario por ejemplo a la hora de sincronizar o deslogear.
		 */
		Context context = EReaderApplication.getAppContext();
		
		mDbxAccountManager = DbxAccountManager.getInstance(context, APP_KEY, APP_SECRET);
	}
	
	/**
	 * Instancia del gestor
	 * 
	 * De forma que podamos reutilizarlo, ahorrar memoria, y trabajar con sus miembros.
	 * 
	 * @return DropboxManager
	 */
	public static EReaderManager getInstance() {
		if(null == mInstance)
			mInstance = new EReaderManager();
		
		return mInstance;
	}
	
	/**
	 * Metodo para conectarse a la cuenta de dropbox
	 * 
	 * @param activity		Contexto de la actividad que lanza el metodo
	 * @param requestCode	Codigo de peticion que diferencia y se procesa en el metodo onActivityResult de la actividad
	 */
	public void doLogin(Activity activity, int requestCode)
	{
		if(!isLoggedIn())
			mDbxAccountManager.startLink(activity, requestCode);
	}
	
	
	public boolean isLoggedIn()
	{
		return mDbxAccountManager.hasLinkedAccount();
	}
	
	
	public void loadFileSystem() throws Exception
	{
		if(isLoggedIn())
		{
			try {
				mDbxFileSystem = DbxFileSystem.forAccount(mDbxAccountManager.getLinkedAccount());
			} catch (Unauthorized e) {
				Log.e(TAG, e.getLocalizedMessage());
			}
			return;
		}
		
		throw new Exception("You have not linked to a Dropbox Account");
	}
	
	public <T> void getFilesByExtension(List<DbxPath> paths, String fileExtension, 
			int maxFiles, OnFilesLoadedCallback<T> callback, Class<T> clzz) throws Exception{
	        
		loadFileSystem();
	        
        DbxFilesTaskLoader<T> task = new DbxFilesTaskLoader<T>(paths, fileExtension, maxFiles, callback, clzz);
        task.execute();
	}
	
	
	public class DbxFilesTaskLoader< T > extends AsyncTask<Void, Integer, List<T>>
	{
		
		private List<DbxPath> mPaths;
		private String mFileExtension;
		private int mMaxFiles;
		private OnFilesLoadedCallback<T> mCallback;
		private Class<T> mClzz;
		
		public DbxFilesTaskLoader(List<DbxPath> paths, String fileExtension, 
				int maxFiles, OnFilesLoadedCallback<T> callback, Class<T> clzz) {
			
			mPaths = paths;
			mFileExtension = fileExtension;
			mMaxFiles = maxFiles;
			mCallback = callback;
			mClzz = clzz;
		}

		@Override
		protected List<T> doInBackground(Void... params) {
			try {
				if (!mDbxFileSystem.hasSynced())
					mDbxFileSystem.awaitFirstSync();
				
		        return iterateThroughPaths(mPaths, mFileExtension, mMaxFiles);
			} catch (DbxException e) {
				
				Log.e(TAG, e.getLocalizedMessage());

				return null;
			}
		}
		
		@Override
		protected void onPostExecute(List<T> result) {
			mCallback.onFilesLoaded(result);
		}
		
		private List<T> iterateThroughPaths(List<DbxPath> paths, String fileExtension, int maxFiles)
		{
			ArrayList<T> filesFound = new ArrayList<T>();
            int nfiles = 0;
            
            try {
                // iterates over all the directories
                while (!paths.isEmpty() && (maxFiles < 0 || nfiles < maxFiles)){
                        DbxPath path = paths.remove(0);
                        List<DbxFileInfo> files = mDbxFileSystem.listFolder(path);
                        
                        // for each file in a directory
                        for (DbxFileInfo fileInfo : files) {
                            // if it is a folder then we include it in the set of directories to explore
                            if (fileInfo.isFolder){
                                paths.add(fileInfo.path);
                            }
                            // if it is the kind of file we are looking for we include it in filesFound
                            else if (fileExtension == null || fileInfo.path.getName().contains(fileExtension)){
                                    
                                Constructor<T> constructor = mClzz.getConstructor(DbxFileInfo.class);
                                
                                T newItem = constructor.newInstance(fileInfo);
                                        
                                filesFound.add(newItem);
                                nfiles++;
                            }
                        }
                }
            } catch (NotFound e){
                    // when the folder supplied to listFolder doesn't exist
                    Log.e("DropboxManager.DropboxListingTask", "You must supply an existing folder to listFolder");
                    throw new IllegalStateException("The folder supplied to listFolder doesn't exist", e);
                    
            } catch (InvalidParameter e) {
                    // when the path supplied to listFolder refers to a file.
                    Log.e("DropboxManager.DropboxListingTask", "You must supply a folder's path to listFolder");
                    throw new IllegalStateException("The path supplied to listFolder refers to a file", e);
                    
            } catch (DbxException e) {
                    // when another failure occurs in listFolder
                    return null;
            } catch (NoSuchMethodException e) {
				Log.e(TAG, e.getLocalizedMessage());
			} catch (InstantiationException e) {
				Log.e(TAG, e.getLocalizedMessage());
			} catch (IllegalAccessException e) {
				Log.e(TAG, e.getLocalizedMessage());
			} catch (IllegalArgumentException e) {
				Log.e(TAG, e.getLocalizedMessage());
			} catch (InvocationTargetException e) {
				Log.e(TAG, e.getLocalizedMessage());
			}
            
            return filesFound;
		}
		
	}
	
}
