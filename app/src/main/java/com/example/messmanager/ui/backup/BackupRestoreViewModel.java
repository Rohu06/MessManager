package com.example.messmanager.ui.backup;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.messmanager.data.backup.BackupManager;

/**
 * BackupRestoreViewModel
 *
 * Thin wrapper around BackupManager so the Activity depends on a
 * ViewModel (consistent with every other screen) rather than
 * instantiating BackupManager itself.
 */
public class BackupRestoreViewModel extends AndroidViewModel {

    private final BackupManager backupManager;

    public BackupRestoreViewModel(@NonNull Application application) {
        super(application);
        backupManager = new BackupManager(application);
    }

    public void exportTo(Uri destination, BackupManager.BackupCallback callback) {
        backupManager.exportTo(destination, callback);
    }

    public void importFrom(Uri source, BackupManager.BackupCallback callback) {
        backupManager.importFrom(source, callback);
    }

    public void exportCsvTo(Uri destination, BackupManager.BackupCallback callback) {
        com.example.messmanager.util.AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                java.util.List<com.example.messmanager.data.local.entity.MealEntry> entries = 
                        com.example.messmanager.data.local.AppDatabase.getInstance(getApplication())
                        .mealDao().getAllEntriesSync();
                        
                if (entries == null || entries.isEmpty()) {
                    com.example.messmanager.util.AppExecutors.getInstance().mainThread().post(() -> 
                        callback.onError("No data to export."));
                    return;
                }
                
                com.example.messmanager.util.CsvExporter.exportToCsv(getApplication(), destination, entries);
                
                com.example.messmanager.util.AppExecutors.getInstance().mainThread().post(callback::onSuccess);
            } catch (Exception e) {
                com.example.messmanager.util.AppExecutors.getInstance().mainThread().post(() -> 
                    callback.onError("CSV Export failed: " + e.getMessage()));
            }
        });
    }
}