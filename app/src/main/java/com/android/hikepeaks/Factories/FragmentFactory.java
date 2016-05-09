package com.android.hikepeaks.Factories;


import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.android.hikepeaks.Fragments.BaseFragment;
import com.android.hikepeaks.Fragments.ExploreFragment;
import com.android.hikepeaks.Fragments.MapFragment;
import com.android.hikepeaks.Fragments.ProfileFragment;
import com.android.hikepeaks.R;

import java.util.Collection;
import java.util.HashMap;

public class FragmentFactory {

    protected static FragmentFactory INSTANCE = null;
    protected HashMap<String, Fragment> cache = new HashMap<>();

    protected FragmentFactory() {
        try {
            //navigationbar
            cache.put(ProfileFragment.class.toString(), ProfileFragment.class.newInstance());
            cache.put(ExploreFragment.class.toString(), ExploreFragment.class.newInstance());
            cache.put(MapFragment.class.toString(),  MapFragment.class.newInstance());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FragmentFactory getInstance() {

        if (INSTANCE == null) {
            synchronized (FragmentFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new FragmentFactory();
                }
            }
        }
        return INSTANCE;
    }

    public Fragment getMainFragment() {
        return getFragment(-1);
    }

    public Fragment getProfileFragment() {
        return getFragment(R.id.nav_profile);
    }

    public Fragment getMapFragment() {
        return getFragment(R.id.nav_map);
    }

    public Collection<Fragment> getAllFragments() {
        return cache.values();
    }

    public Fragment getFragment(int itemId) {

        Class fragmentClass;

        switch (itemId) {
            case R.id.nav_profile:
                fragmentClass = ProfileFragment.class;
                break;
            case R.id.nav_explore:
                fragmentClass = ExploreFragment.class;
                break;
            case R.id.nav_map:
                fragmentClass = MapFragment.class;
                break;
            default:
                fragmentClass = ExploreFragment.class;
        }
        String fragmentName = fragmentClass.toString();
        Fragment fragment;
        if (!cache.containsKey(fragmentName)) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
                cache.put(fragmentName, fragment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        fragment = cache.get(fragmentName);
        return fragment;
    }
}
