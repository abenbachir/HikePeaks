package com.android.hikepeaks.Services;


import com.android.hikepeaks.Synchronizer.SyncObjectInterface;

import java.util.LinkedList;
import java.util.Queue;

public class SyncPool {

    protected static SyncPool INSTANCE = null;
    protected Queue<SyncObjectInterface> pool = new LinkedList<>();

    protected SyncPool() {
    }

    public static SyncPool getInstance() {
        if (INSTANCE == null) {
            synchronized (SyncPool.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SyncPool();
                }
            }
        }
        return INSTANCE;
    }

    public void queueObject(SyncObjectInterface object) {
        pool.add(object);
    }

    public SyncObjectInterface pollObject() {
        return pool.poll();
    }

    public boolean isEmpty() {
        return pool.isEmpty();
    }
}
