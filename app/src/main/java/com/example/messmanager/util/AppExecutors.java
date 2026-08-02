package com.example.messmanager.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AppExecutors
 *
 * Provides a shared background executor for disk/database operations
 * and a handle to the main thread for posting results back to the UI.
 * Singleton so the whole app shares one background thread pool rather
 * than creating new threads per operation.
 */
public class AppExecutors {

    private static volatile AppExecutors INSTANCE;

    private final ExecutorService diskIO;
    private final Handler mainThread;

    private AppExecutors() {
        diskIO = Executors.newSingleThreadExecutor();
        mainThread = new Handler(Looper.getMainLooper());
    }

    public static AppExecutors getInstance() {
        if (INSTANCE == null) {
            synchronized (AppExecutors.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppExecutors();
                }
            }
        }
        return INSTANCE;
    }

    public ExecutorService diskIO() {
        return diskIO;
    }

    public Handler mainThread() {
        return mainThread;
    }
}