package com.android.hikepeaks.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hikepeaks.Models.Account;
import com.android.hikepeaks.Models.Trail;
import com.android.hikepeaks.R;
import com.android.hikepeaks.Services.AccountManager;
import com.android.hikepeaks.Services.TerminalManager;
import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class StatsFragment extends BaseFragment {

    /*------ LES SERVICES ------*/
    private AccountManager accountManager = AccountManager.getInstance();
    Account mAccount = accountManager.getAccount();



    public StatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_stats, container, false);


        /*------ PRENDRE TOUTES LES TRAILS ------*/

        ArrayList<Trail> trails = mAccount.getTrails();


        /*------ LES ÉLÉMENTS QU'ON VA INITIALISER ------*/

        TextView value_totHikes = (TextView) view.findViewById(R.id.stats_tot_hikes);
        TextView value_totSteps = (TextView) view.findViewById(R.id.stats_tot_steps);
        TextView value_totPic = (TextView) view.findViewById(R.id.stats_tot_pic);
        TextView value_totDistance = (TextView) view.findViewById(R.id.stats_tot_distance);
        TextView value_avgSteps = (TextView) view.findViewById(R.id.stats_avg_steps);
        TextView value_avgPic = (TextView) view.findViewById(R.id.stats_avg_pic);
        TextView value_avgDistance = (TextView) view.findViewById(R.id.stats_avg_distance);
        TextView value_avgSpeed = (TextView) view.findViewById(R.id.stats_avg_speed);
        TextView value_avgDuration = (TextView) view.findViewById(R.id.stats_avg_duration);

        ImageView mProfilePicture = (ImageView) view.findViewById(R.id.stats_owner_picture);
        TextView  mName = (TextView) view.findViewById(R.id.stats_owner_name);

        /*------ INFORMATIONS DU RANDONNEUR ------*/

        if(mName!=null){
            mName.setText(mAccount.getDisplayName());
        }


        if(mProfilePicture != null) {
            try{
                Glide.with(getContext())
                        .load(mAccount.getPictureUrl())
                        .into(mProfilePicture);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }


        /*------ LES VARIABLES NÉCESSAIRES ------*/

        //variables qui vont être affichées
        int totalHikes = trails.size();
        int totalSteps = 0;
        int totalPic = 0;
        int avgSteps = 0;
        int avgPic = 0;

        double totalDistance = 0;
        double avgDistance = 0;
        double avgSpeed = 0;
        int avgDuration = 0;


        /*------ DÉBUT DES CALCULS ------*/


        if (totalHikes > 0) {

            for (int i = 0; i < trails.size(); i++) {
                totalDistance += trails.get(i).getDistance();
                totalPic += trails.get(i).getCaptures().size();
                totalSteps += trails.get(i).getSteps();

                avgSpeed += trails.get(i).getAverageSpeed();
                avgDuration += trails.get(i).getElapsedTimeInSeconds();
            }

            avgDistance = totalDistance/totalHikes;
            avgSteps = totalSteps/totalHikes;
            avgPic = totalPic/totalHikes;
            avgSpeed = avgSpeed /totalHikes;
            avgDuration = avgDuration/totalHikes;

        }

        //if we don't have a step counter
        if(totalSteps == 0 && avgSteps == 0){
            value_totSteps.setText("Your device is not equiped with a step counter");
            value_avgSteps.setText("Your device is not equiped with a step counter");
        }else {
            value_totSteps.setText(Integer.toString(totalSteps) + " steps");
            value_avgSteps.setText(Integer.toString(avgSteps) + " steps");
        }


        /*------ ASSIGNER LES VALEURS ------*/
        value_totHikes.setText(Integer.toString(totalHikes));
        value_totPic.setText(Integer.toString(totalPic) + " pictures");
        value_totDistance.setText(roundDoubletoString(totalDistance/1000) + " km");
        value_avgPic.setText(Integer.toString(avgPic) + " pictures");
        value_avgDistance.setText(roundDoubletoString(avgDistance/1000) + " km");
        value_avgSpeed.setText(roundDoubletoString(avgSpeed) + " m/s");
        value_avgDuration.setText(timeConversion(avgDuration));

        return view;
    }

    private static String timeConversion(int seconds) {

        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int minutes = seconds / SECONDS_IN_A_MINUTE;
        seconds -= minutes * SECONDS_IN_A_MINUTE;

        int hours = minutes / MINUTES_IN_AN_HOUR;
        minutes -= hours * MINUTES_IN_AN_HOUR;

        return hours + " hours " + minutes + " minutes " + seconds + " seconds";
    }

    private static String roundDoubletoString(double d){
        DecimalFormat df = new DecimalFormat("0.00");
        return (df.format(d));
    }

}

