package com.saibi.ereader.fragments;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.saibi.ereader.EReaderManager;
import com.saibi.ereader.EReaderManager.OnFilesLoadedCallback;
import com.saibi.ereader.R;
import com.saibi.ereader.adapters.SortedListAdapter;
import com.saibi.ereader.adapters.SortedListAdapter.OnSortedListFinished;
import com.saibi.ereader.domain.DbxFileInfo;
import com.saibi.ereader.utils.ImageLazyLoader;
import com.saibi.ereader.utils.ImageLazyLoader.OnImageLoaded;
import com.saibi.ereader.widgets.DoubleTapListViewCompat;
import com.saibi.ereader.widgets.DoubleTapListViewCompat.OnItemDoubleClickListener;

public class EbookListFragment extends CustomListFragment implements
		OnFilesLoadedCallback, ActionBar.OnNavigationListener,
		OnSortedListFinished, OnImageLoaded {
	public static final String TAG = "MainActivity$EbookListFragment";

	private static EbookListFragment mInstance;

	/**
	 * Guardamos estos datos por ejemplo al girar el dispositivo
	 */
	private static final String COMPARATOR_TYPE_KEY = "COMPARATOR_TYPE_KEY";
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "STATE_SELECTED_NAVIGATION_ITEM";

	/**
	 * TODO: Podriamos extraer estos datos en un Constant.java Util con la
	 * ofuscacion de codigo, a diferencia de valores en string.xml
	 */
	private static final String FILE_EXTENSION = ".epub";
	private static final String ROOT_PATH = "/";
	private static final int MAX_FILES = 10;

	private EbookSortedListAdapter mAdapter;

	private ActionBar mActionBar;

	private CustomComparator mComparator = new CustomComparator();
	private int mCurrentComparatorType = CustomComparator.BY_TITLE_NAME;

	/**
	 * Flag necesario para evitar que onNavigationItemSelected ejecute la
	 * ordenacion sin datos
	 */
	private boolean mFilesLoaded = false;

	public interface CompatibleVersionCodes {
		public static final int ICE_CREAM_SANDWICH = 14;
	}

	public static EbookListFragment newInstance() {

		if (null == mInstance)
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

				ArrayAdapter.createFromResource(
						getActionBarThemedContextCompat(),
						R.array.dropdown_sort_values,
						android.R.layout.simple_list_item_1), this);

		mComparator.setType(mCurrentComparatorType);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Double Tap
		View view = super.onCreateView(inflater, container, savedInstanceState);

		DoubleTapListViewCompat listView = (DoubleTapListViewCompat) view
				.findViewById(android.R.id.list);

		listView.setOnItemDoubleClickListener(new OnItemDoubleClickListener() {

			@Override
			public void OnItemDoubleClick(AdapterView<?> parent, View view,
					int position, long id) {

				startDisplayCover(mAdapter.getItem(position).getBook());
			}
		});

		return view;
	}

	/**
	 * Backward-compatible version of {@link ActionBar#getThemedContext()} that
	 * simply returns the {@link android.app.Activity} if
	 * <code>getThemedContext</code> is unavailable.
	 */
	@TargetApi(CompatibleVersionCodes.ICE_CREAM_SANDWICH)
	private Context getActionBarThemedContextCompat() {
		if (Build.VERSION.SDK_INT >= CompatibleVersionCodes.ICE_CREAM_SANDWICH) {
			return mActionBar.getThemedContext();
		} else {
			return getActivity();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (null != savedInstanceState) {
			if (savedInstanceState.containsKey(COMPARATOR_TYPE_KEY)) {
				mCurrentComparatorType = savedInstanceState
						.getInt(COMPARATOR_TYPE_KEY);
			}
			if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
				mActionBar.setSelectedNavigationItem(savedInstanceState
						.getInt(STATE_SELECTED_NAVIGATION_ITEM));
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
		if (mFilesLoaded) {
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

		EReaderManager.getInstance().getFilesByExtension(ROOT_PATH,
				FILE_EXTENSION, MAX_FILES, this);
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
	 * Lanza un activity que muestra a pantalla completa y transparente la
	 * imagen Es necesario pasar el objeto Book como extra
	 */
	private ProgressBar mProgressPopup;

	private void startDisplayCover(Book book) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				getActivity(), R.style.CoverTheme);

		View container = getActivity().getLayoutInflater().inflate(
				R.layout.cover, null);

		mProgressPopup = (ProgressBar) container.findViewById(R.id.progress);

		ImageView imageView = (ImageView) container.findViewById(R.id.cover);

		final ImageView closeView = (ImageView) container
				.findViewById(R.id.close);

		TextView titleView = (TextView) container.findViewById(R.id.title);
		titleView.setText(book.getTitle());

		alertDialog.setView(container);

		final AlertDialog dialogAlert = alertDialog.setCancelable(true)
				.create();

		dialogAlert.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {

				closeView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialogAlert.dismiss();
					}
				});
			}
		});

		dialogAlert.setCanceledOnTouchOutside(true);
		dialogAlert.show();

		try {
			ImageLazyLoader.loadFromInputStream(imageView, book.getCoverImage()
					.getInputStream(), this);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	@Override
	public void onImageLoaded() {
		mProgressPopup.setVisibility(View.GONE);
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
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			try {

				DbxFileInfo fileInfo = getItem(position);

				if (null == convertView) {
					convertView = mInflater.inflate(
							R.layout.fragment_ebook_list, parent, false);

					holder = new ViewHolder();

					holder.title = (TextView) convertView
							.findViewById(R.id.title);

					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}

				holder.title.setText(fileInfo.getBook().getTitle());

			} catch (Exception e) {
				Log.w(TAG, e.getMessage());
			}

			return convertView;
		}

		private class ViewHolder {
			TextView title;
		}
	}

	private class CustomComparator implements Comparator<DbxFileInfo> {

		public static final int BY_TITLE_NAME = 0x00;
		public static final int BY_CREATION_DATE = 0x01;

		private int mComparatorType;

		public void setType(int type) {
			mComparatorType = type;
		}

		@Override
		public int compare(DbxFileInfo n1, DbxFileInfo n2) {
			switch (mComparatorType) {
			// comparamos por la fecha de ultima modificacion en dropbox
			// ya que la fecha del libro puede estar vacia !
			case BY_CREATION_DATE:
				return n1.getEntry().clientMtime
						.compareTo(n2.getEntry().clientMtime);

			default:
				return n1.getBook().getTitle()
						.compareTo(n2.getBook().getTitle());
			}
		}
	}

}