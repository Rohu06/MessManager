package com.example.messmanager.ui.backup;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.messmanager.R;
import com.example.messmanager.data.backup.BackupManager;
import com.example.messmanager.databinding.ActivityBackupRestoreBinding;
import com.example.messmanager.ui.splash.SplashActivity;
import com.example.messmanager.util.DateUtils;

/**
 * BackupRestoreActivity
 *
 * Export: lets the user pick a save location via SAF, copies the
 * database there, then offers to Share the resulting file immediately.
 * Import: lets the user pick a previously exported file via SAF,
 * validates and applies it, then restarts the app process — required
 * because Room's open connection can't coexist with overwriting the
 * database file underneath it.
 */
public class BackupRestoreActivity extends AppCompatActivity {

    private ActivityBackupRestoreBinding binding;
    private BackupRestoreViewModel viewModel;

    private ActivityResultLauncher<String> createDocumentLauncher;
    private ActivityResultLauncher<String[]> openDocumentLauncher;

    private Uri lastExportedUri; // kept so "Share" can act on the file just created

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBackupRestoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(BackupRestoreViewModel.class);

        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/octet-stream"),
                uri -> { if (uri != null) performExport(uri); });

        openDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> { if (uri != null) confirmImport(uri); });

        binding.btnExport.setOnClickListener(v -> {
            String suggestedName = "mess_manager_backup_" + DateUtils.getTodayDateString() + ".db";
            createDocumentLauncher.launch(suggestedName);
        });

        binding.btnShareLastExport.setOnClickListener(v -> shareLastExport());
        binding.btnImport.setOnClickListener(v -> openDocumentLauncher.launch(new String[]{"*/*"}));

        binding.btnShareLastExport.setEnabled(false);
    }

    private void performExport(Uri destination) {
        viewModel.exportTo(destination, new BackupManager.BackupCallback() {
            @Override
            public void onSuccess() {
                lastExportedUri = destination;
                binding.btnShareLastExport.setEnabled(true);
                Toast.makeText(BackupRestoreActivity.this, R.string.msg_backup_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(BackupRestoreActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void shareLastExport() {
        if (lastExportedUri == null) return;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/octet-stream");
        shareIntent.putExtra(Intent.EXTRA_STREAM, lastExportedUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.title_share_backup)));
    }

    private void confirmImport(Uri source) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_restore_database)
                .setMessage(R.string.msg_confirm_restore)
                .setPositiveButton(R.string.action_restore, (dialog, which) -> performImport(source))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void performImport(Uri source) {
        viewModel.importFrom(source, new BackupManager.BackupCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(BackupRestoreActivity.this, R.string.msg_restore_success, Toast.LENGTH_SHORT).show();
                restartApp();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(BackupRestoreActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Kills the current process and schedules an immediate alarm to
     * relaunch SplashActivity, giving the app a completely fresh start
     * with the restored database. This is the standard safe pattern
     * for replacing a Room database file out from under a running app.
     */
    private void restartApp() {
        Intent restartIntent = new Intent(this, SplashActivity.class);
        restartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = PendingIntent.FLAG_ONE_SHOT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 9001, restartIntent, flags);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 300, pendingIntent);
        }

        finishAffinity();
        Runtime.getRuntime().exit(0);
    }
}