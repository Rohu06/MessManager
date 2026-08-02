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
}