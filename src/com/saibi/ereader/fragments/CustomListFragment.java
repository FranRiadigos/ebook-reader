package com.saibi.ereader.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.saibi.ereader.R;

public class CustomListFragment extends ListFragment {
	private boolean mListShown;
	private View mProgressContainer;
	private View mListContainer;

	private void setListShown(boolean shown, boolean animate) {
		if (mListShown == shown) {
			return;
		}

		if (getActivity() == null || mProgressContainer == null
				|| mListContainer == null) {
			return;
		}

		mListShown = shown;
		if (shown) {
			if (animate) {
				mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), android.R.anim.fade_out));
				mListContainer.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), android.R.anim.fade_in));
			}
			mProgressContainer.setVisibility(View.GONE);
			mListContainer.setVisibility(View.VISIBLE);
		} else {
			if (animate) {
				mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), android.R.anim.fade_in));
				mListContainer.startAnimation(AnimationUtils.loadAnimation(
						getActivity(), android.R.anim.fade_out));
			}
			mProgressContainer.setVisibility(View.VISIBLE);
			mListContainer.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void setListShown(boolean shown) {
		setListShown(shown, true);
	}

	@Override
	public void setListShownNoAnimation(boolean shown) {
		setListShown(shown, false);
	}

	@Override
	public void setEmptyText(CharSequence text) {
		((TextView) mListContainer.findViewById(android.R.id.empty))
				.setText(text);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_custom_list, container,
				false);

		mListContainer = root.findViewById(R.id.container);
		mProgressContainer = root.findViewById(R.id.progress);
		mListShown = true;
		return root;
	}
}
