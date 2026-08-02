package com.example.messmanager.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.messmanager.data.local.dao.MealDao;
import com.example.messmanager.data.local.entity.MealEntry;

/**
 * AppDatabase
 *
 * Central Room database for Mess Manager. Singleton — only one instance
 * exists for the app's lifetime. Uses TRUNCATE journal mode (single
 * file on disk, no separate -wal/-shm files) specifically so backup
 * and restore can work with a simple whole-file copy.
 */
@Database(
        entities = {MealEntry.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "mess_manager_db";

    private static volatile AppDatabase INSTANCE;

    public abstract MealDao mealDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .setJournalMode(JournalMode.TRUNCATE)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Closes the current database connection and clears the singleton
     * so the next getInstance() call opens a fresh connection. Must be
     * called before overwriting the database file on disk (restore),
     * otherwise the write will conflict with Room's open connection.
     */
    public static synchronized void closeAndClearInstance() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }
}