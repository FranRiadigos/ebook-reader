package com.saibi.ereader.adapters;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

/**
 * ListAdapter personalizado
 * 
 * TODO: Implementar sincronizacion (en caso de que se usara)
 * 
 * @param &lt;T&gt; Item Type
 */
public class SortedListAdapter<T> extends ArrayAdapter<T> {

	protected final LayoutInflater mInflater;

	// Lo hacemos privado solo para esta instancia
	// evitando que se confunda con el bloqueo de los datos del ArrayAdapter
	private List<T> mData;

	private Comparator<T> mComparator;

	private OnSortedListFinished mSortedFinishCallback;

	// Lo hacemos privado solo para esta instancia
	// evitando que se confunda con el bloqueo de los datos del ArrayAdapter
	private final Object mLock = new Object();

	private SortAdapterTask task = null;

	// Interfaz que avisa al contexto que ha ejecutado la tarea
	// de que ha finalizado de ordenar la lista de items
	public interface OnSortedListFinished {
		public void onSortedListFinished();
	}

	public SortedListAdapter(Context context) {
		super(context, 0);
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void sortBy(Comparator<T> comparator, OnSortedListFinished callback) {
		mComparator = comparator;
		mSortedFinishCallback = callback;
		sort();
	}

	public void sortBy(List<T> data, Comparator<T> comparator,
			OnSortedListFinished callback) {
		mData = data;
		mComparator = comparator;
		mSortedFinishCallback = callback;
		sort();
	}

	private void sort() {
		if (task != null) {
			task.cancel(true);
		}

		task = new SortAdapterTask();
		task.execute();
	}

	private class SortAdapterTask extends AsyncTask<Void, Void, List<T>> {

		@Override
		protected List<T> doInBackground(Void... params) {

			synchronized (mLock) {
				Collections.sort(mData, mComparator);
			}
			return mData;
		}

		@Override
		protected void onPostExecute(List<T> result) {
			addAll(result);
			task = null;
			mSortedFinishCallback.onSortedListFinished();
		}
	}
}
