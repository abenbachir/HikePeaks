package com.android.hikepeaks.Tasks;


import android.os.Handler;

import java.util.TimerTask;

public class CounterTask extends TimerTask {
    private int totalSeconds = 0;
    private OnTimeChangedListener listener;
    private Handler handler;

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void reset() {
        totalSeconds = 0;
    }

    public OnTimeChangedListener getListener() {
        return listener;
    }

    public void setListener(OnTimeChangedListener listener) {
        this.listener = listener;
    }

    public int getTotalSeconds() {
        return totalSeconds;
    }

    @Override
    public void run() {
        totalSeconds++;
        if (handler != null)
            handler.sendEmptyMessage(totalSeconds);
    }

    public interface OnTimeChangedListener {
        void OnTimeChanged(int seconds);
    }
}
