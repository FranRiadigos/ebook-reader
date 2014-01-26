//package com.saibi.ereader.adapters;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//
//import android.content.Context;
//import android.os.AsyncTask;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.BaseAdapter;
//import android.widget.TextView;
//
///**
// * 
// * @author cjuega
// *
// * This class is a simple adapter to show (and maintain) sorted data. SortedListAdapter guarantees 
// * the data is ALWAYS sorted. As opposite to {@link ArrayAdapter ArrayAdapter}, in which 
// * data can be sorted but then random inserts may occur.
// *
// * @param <T>	The element's type this adapter will hold.
// */
//
//public class SortedListAdapter2<T> extends BaseAdapter {
//
//	private Context mContext;
//	protected LayoutInflater mInflater;
//	
//	protected int mResource;
//	private int mFieldId = 0;
//	
//	protected List<T> mData;
//	private Comparator<? super T> mComparator;
//	
//	protected final Object mLock = new Object();
//	
//	private DataSortTask mTaskRef = null;
//	
//	/* Constructors */
//	
//	public SortedListAdapter2(Context context, int textViewResourceId, Comparator<? super T> comparator) {
//		init(context, textViewResourceId, 0, new ArrayList<T>(), comparator);
//	}
//	
//	public SortedListAdapter2(Context context, int resource, int textViewResourceId, Comparator<? super T> comparator) {
//		init(context, resource, textViewResourceId, new ArrayList<T>(), comparator);
//	}
//
//	public SortedListAdapter2(Context context, int textViewResourceId, T[] objects, Comparator<? super T> comparator) {
//		init(context, textViewResourceId, 0, Arrays.asList(objects), comparator);
//	}
//	
//	public SortedListAdapter2(Context context, int resource, int textViewResourceId, T[] objects, Comparator<? super T> comparator) {
//		init(context, resource, textViewResourceId, Arrays.asList(objects), comparator);
//	}
//	
//	public SortedListAdapter2(Context context, int textViewResourceId, List<T> objects, Comparator<? super T> comparator) {
//		init(context, textViewResourceId, 0, objects, comparator);
//	}
//	
//	public SortedListAdapter2(Context context, int resource, int textViewResourceId, List<T> objects, Comparator<? super T> comparator) {
//		init(context, resource, textViewResourceId, objects, comparator);
//	}
//	
//	/* Methods */
//	
//	public static SortedListAdapter<CharSequence> createFromResource(Context context, int textArrayResId, int textViewResId) {
//		CharSequence[] strings = context.getResources().getTextArray(textArrayResId);
//		return new SortedListAdapter<CharSequence>(context, textViewResId, strings, new Comparator<CharSequence>() {
//																							@Override
//																							public int compare(CharSequence lhs, CharSequence rhs) {
//																								return lhs.toString().compareTo(rhs.toString());
//																							}
//																						});
//	}
//	
//	private void init (Context context, int resource, int textViewResourceId, List<T> objects, Comparator<? super T> comparator){
//		if (comparator == null){
//			Log.e("SortedListAdapter", "You must supply a comparator object");
//			throw new IllegalStateException("SortedListAdapter requires a comparator");
//		}
//		
//		mComparator = comparator;
//		mContext = context;
//		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		mResource = resource;
//		mData = objects;
//		mFieldId = textViewResourceId;
//		
//		if (mData != null && !mData.isEmpty())
//			sort();
//	}
//	
//	public Context getContext(){
//		return mContext;
//	}
//	
//	@Override
//	public int getCount() {
//		return mData.size();
//	}
//
//	@Override
//	public T getItem(int position) {
//		return mData.get(position);
//	}
//
//	@Override
//	public long getItemId(int position) {
//		return position;
//	}
//
//	@Override
//	public View getView(int position, View convertView, ViewGroup parent) {
//		return createViewFromResource(position, convertView, parent, mResource);
//	}
//	
//	public void add(T element){
//		if (sortedInsert(element))
//			notifyDataSetChanged();
//	}
//	
//	public void addAll(T... items) {
//		if(items.length > 0){
//			for (T item : items){
//				synchronized (mLock) {
//					mData.add(item);	
//				}
//			}
//			sort();
//		}
//	}
//	
//	public void addAll(Collection<? extends T> collection) {
//		if(!collection.isEmpty()){
//			for (T item : collection){
//				synchronized (mLock) {
//					mData.add(item);	
//				}
//			}
//			sort();
//		}
//	}
//	
//	public void remove (T element){
//		synchronized (mLock) {
//			mData.remove(element);
//		}
//		notifyDataSetChanged();
//	}
//	
//	public void clear(){
//		synchronized (mLock) {
//			mData.clear();
//		}
//		notifyDataSetChanged();
//	}
//	
//	public void sortby(Comparator<? super T> comparator){
//		if (comparator == null){
//			Log.e("SortedListAdapter", "You must supply a comparator object");
//			throw new IllegalStateException("SortedListAdapter requires a comparator");
//		}
//		
//		mComparator = comparator;
//		
//		sort();
//	}
//	
//	private boolean sortedInsert(T element) {
//		int position = Collections.binarySearch(mData, element, mComparator);
//		
//		if (position < 0){
//			synchronized (mLock) {
//				mData.add(Math.abs(position)-1, element);
//			}
//			return true;
//		}
//		
//		return false;
//	}
//	
//	protected void sort(){
//		if (mTaskRef != null){
//			mTaskRef.cancel(true);
//		}
//		
//		mTaskRef = new DataSortTask();
//		mTaskRef.execute();
//	}
//	
//	protected View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
//		View view;
//		TextView text;
//		
//		if (convertView == null)
//			view = mInflater.inflate(resource, parent, false);
//		else
//			view = convertView;
//        
//		try {
//			if (mFieldId == 0)
//				//  If no custom field is assigned, assume the whole resource is a TextView
//				text = (TextView) view;
//            else
//            	//  Otherwise, find the TextView field within the layout
//            	text = (TextView) view.findViewById(mFieldId);
//
//		} catch (ClassCastException e) {
//			Log.e("SortedListAdapter", "You must supply a resource ID for a TextView");
//			throw new IllegalStateException("ArrayAdapter requires the resource ID to be a TextView", e);
//		}
//
//		T item = getItem(position);
//		
//		if (item instanceof CharSequence) 	
//			text.setText((CharSequence)item);
//        else 
//        	text.setText(item.toString());
//		
//		return view;
//	}
//	
//	class DataSortTask extends AsyncTask<Void, Void, Void>{
//		
//		@Override
//		protected Void doInBackground(Void... params) {
//			synchronized (mLock) {
//				Collections.sort(mData, mComparator);	
//			}
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Void result) {
//			notifyDataSetChanged();
//			mTaskRef = null;
//		}
//	}
//}
