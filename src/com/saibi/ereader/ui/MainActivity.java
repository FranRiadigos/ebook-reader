package com.saibi.ereader.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.sync.android.DbxPath;
import com.saibi.ereader.EReaderManager;
import com.saibi.ereader.EReaderManager.OnFilesLoadedCallback;
import com.saibi.ereader.R;
import com.saibi.ereader.adapters.SortedListAdapter;
import com.saibi.ereader.pojos.EPubInfo;

public class MainActivity extends Base {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		replaceSupportFragment(EbookListFragment.newInstance(), R.id.base);
	}
	
	
	public static class EbookListFragment extends ListFragment implements OnFilesLoadedCallback<EPubInfo> {
		public static final String TAG = "MainActivity$EbookListFragment";
		
		private static EbookListFragment mInstance;
		
		private static final String FILE_EXTENSION = ".epub";
		private static final int MAX_FILES = 10;
		
		private List<DbxPath> paths;
		
		private EPubSortedListAdapater mAdapter;
		
		public static EbookListFragment newInstance() {
			
			if(null == mInstance)
				mInstance = new EbookListFragment();
			
            return mInstance;
        }
		
		@Override
		public void onCreate(Bundle savedInstanceState) {

			paths = new ArrayList<DbxPath>();
			paths.add(DbxPath.ROOT);
			
			super.onCreate(savedInstanceState);
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			setEmptyText("No existen archivos de tipo .epub en la cuenta dropbox");

			setListShown(false);
			
			mAdapter = new EPubSortedListAdapater(getActivity());
			
			loadFilesList();
		}
		
		
		private void loadFilesList()
		{
			try {
				EReaderManager.getInstance().<EPubInfo>getFilesByExtension(paths, FILE_EXTENSION, MAX_FILES, this, EPubInfo.class);
			} catch (Exception e) {
				// Esto ocurre si hemos perdido el link de sesion con dropbox
				Log.w(TAG, e.getMessage());
				
				// Deberiamos mostrar aqui un dialogo indicando que es necesario registrarse de nuevo.
				// Al aceptar el dialogo, lanzamos LoginActivity
			}
		}

		@Override
		public void onFilesLoaded(List<EPubInfo> data) {
			
			mAdapter.addAll(data);
			
			setListShown(true);
		}
		
		public class EPubSortedListAdapater extends SortedListAdapter<EPubInfo> {
			private static final String TAG = "SortedListAdapter";
			
			public EPubSortedListAdapater(Context context) {
				super(context);
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) 
			{
				ViewHolder holder;

				try{
					
					EPubInfo item = getItem(position);
					
					if(null == convertView)
					{
						convertView = mInflater.inflate( R.layout.fragment_ebook_list, 
								parent, false );
						
						holder = new ViewHolder();
						
						holder.icon 		= (ImageView) convertView.findViewById(R.id.icon);
						holder.title 		= (TextView) convertView.findViewById(R.id.title);
						holder.updated 		= (TextView) convertView.findViewById(R.id.updated);
						holder.size 		= (TextView) convertView.findViewById(R.id.size);
						
						convertView.setTag(holder);
					} else {
		            	holder = (ViewHolder) convertView.getTag();
		            }
					
					holder.icon.setImageResource(0);
					holder.title.setText(item.getTitle());
					holder.updated.setText(item.getModifiedTime().toString());
					holder.size.setText(String.valueOf(item.getFileInfo().size));
					
				}catch(Exception e) {
					Log.w(TAG, e.getLocalizedMessage());
				}
				
				return convertView;
			}
			
			private class ViewHolder 
			{
				ImageView icon;
				TextView title;
				TextView updated;
				TextView size;
			}
		}
		
	}
	
}
