package com.saibi.ereader.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

public class SortedListAdapter< T > extends ArrayAdapter< T > {
	
	protected final LayoutInflater mInflater;
	
	public SortedListAdapter(Context context) {
		super(context, 0);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
}
