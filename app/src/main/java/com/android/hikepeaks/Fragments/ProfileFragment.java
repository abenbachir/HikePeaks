//source tutoriel : http://alturl.com/aajvz

package com.android.hikepeaks.Fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.hikepeaks.R;

public class ProfileFragment extends Fragment {

    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    public static int int_items = 3;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab_layout, null);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);


        //ajout de l'adaptateur
        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });

        return view;

    }

    class MyAdapter extends FragmentPagerAdapter {


        public MyAdapter(FragmentManager fm) {
            super(fm);
        }
        

        //retourne le fragment avec la bonne position
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    //return hikesFragment;
                    return new HikesFragment();

                case 1:
                    //return statsFragment;
                    return new StatsFragment();

                case 2:
                    //return mediaFragment;
                    return new MediaFragment();
            }
            return null;
        }

        @Override
        public int getCount() {

            return int_items;

        }

        //retourne le nom des onglets en fonction de la position
        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "MY HIKES";
                case 1:
                    return "STATS";
                case 2:
                    return "PICTURES";
            }
            return null;
        }
    }

}

