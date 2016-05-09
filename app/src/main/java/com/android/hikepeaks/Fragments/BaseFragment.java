package com.android.hikepeaks.Fragments;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.android.hikepeaks.Utilities.Connectivity;

public class BaseFragment extends Fragment {

    protected Activity mActivity;
    protected OnFragmentInteractionListener mListener;


    public BaseFragment() {
        // Required empty public constructor
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!this.isHidden()) {
            onVisible();
        } else {
            onHidden();
        }
    }

    public boolean isNetworkAvailable() {
        return Connectivity.isNetworkOnline(getContext());
    }

    public void onVisible() {
    }

    public void onHidden() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
        mListener = null;
    }
}
