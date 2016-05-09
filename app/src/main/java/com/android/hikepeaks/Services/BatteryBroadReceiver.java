package com.android.hikepeaks.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.android.hikepeaks.Activities.MainActivity;
import com.android.hikepeaks.R;

import java.util.Calendar;

public class BatteryBroadReceiver extends BroadcastReceiver {

    final int notificationID = 10;
    SharedPreferences sharedPreferences = null;
    String LevelKey = "level", VoltKey = "volt", TimeKey = "time";

    @Override
    public void onReceive(Context context, Intent intent) {
        int intBattLevel = intent.getIntExtra("level", 0); //explained here: http://developer.android.com/reference/android/os/BatteryManager.html → see "EXTRA_"
        int intVoltLevel = intent.getIntExtra("voltage", 0);

        int intStatus = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
        String strStatus;

        switch (intStatus) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                strStatus = "Charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                strStatus = "Discharging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                strStatus = "Full";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                strStatus = "Not Charging";
                break;
            default:
                strStatus = "Unknown";
        }

        if (!context.getClass().getSimpleName().toLowerCase().contains("main")) {//si context provient de MainActivity, alors on sait que c'est le dernier appel du receiver

            SharedPreferences.Editor editor = context.getSharedPreferences("onstart", Context.MODE_PRIVATE).edit();
            editor.putInt(LevelKey, intBattLevel);
            editor.putInt(VoltKey, intVoltLevel);
            editor.putLong(TimeKey, System.currentTimeMillis());
            editor.commit();

//            String title = "Starting " + context.getString(R.string.app_name);
//            String text = "With Battery:\n"+
//                    "Level = " + intBattLevel +" %\n"
//                    + "Voltage = " + intVoltLevel + " mV\n"
//                    + "While battery status is " + strStatus;

//            showNotification(context, title, text, false);
//            Toast.makeText(context,title + " " +text, Toast.LENGTH_LONG).show();
        } else {

            sharedPreferences = context.getSharedPreferences("onstart", Context.MODE_PRIVATE);

            int intElapsedTime = (int) ((System.currentTimeMillis() - sharedPreferences.getLong(TimeKey, 0)) / 1000);
            String strElapsedTime;
            if (intElapsedTime > 60) {
                strElapsedTime = (int) (intElapsedTime / 60) + "min " + (intElapsedTime % 60) + "s";
            } else {
                strElapsedTime = intElapsedTime + "s";
            }

            String title = context.getString(R.string.app_name) + " Energy Report";
            String text = "Battery Consumption during : " + strElapsedTime + "\n"
                    + "Delta level = " + (intBattLevel - sharedPreferences.getInt(LevelKey, 0)) + " %\n"
                    + "Delta Voltage = " + (intVoltLevel - sharedPreferences.getInt(VoltKey, 0)) + " mV\n"
                    + "While battery status is " + strStatus + "\n";
            showNotification(context, title, text, true);
//            Toast.makeText(context, title +text, Toast.LENGTH_LONG).show();

            sharedPreferences.edit().clear();
        }

        context.unregisterReceiver(this);

    }

    public void clearNotifications(Context context)
    {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        //notification.icon = R.drawable.ic_moneta_logo_small;

        notificationManager.cancelAll(); //Clear all currently display notifications
        notificationManager.cancel(notificationID);
    }
    public void showNotification(Context context, final String title, String text, boolean showTimeStamp) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context) //Use a builder
                .setContentTitle(title) // Title
                .setContentText(text) // Message to display
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setTicker(text)
                .setSmallIcon(R.drawable.logo_24x24) // This one is also displayed in ticker message
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_48x48)) // In notification bar
        ;

        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        //mBuilder.addAction(R.drawable.bulb_small, "OK", resultPendingIntent);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS ;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        long time = 0;
        if (showTimeStamp)
            Calendar.getInstance().getTimeInMillis();
        else
            time = android.os.Build.VERSION.SDK_INT >= 9 ? -Long.MAX_VALUE : Long.MAX_VALUE;

        notification.when = time;


        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        //mNotificationManager.cancelAll(); //Clear all currently display notifications
        notificationManager.cancel(notificationID);
        notificationManager.notify(notificationID, notification);
    }

}
