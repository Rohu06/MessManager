package com.example.messmanager.data.backup;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.example.messmanager.data.local.AppDatabase;
import com.example.messmanager.util.AppExecutors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * BackupManager
 *
 * Handles exporting the Room database file to a user-chosen location
 * and importing a previously exported file back in. All I/O runs on
 * the shared background executor; results are posted back to the
 * main thread via the callback.
 */
public class BackupManager {

    private static final byte[] SQLITE_HEADER = "SQLite format 3\u0000".getBytes();

    public interface BackupCallback {
        void onSuccess();
        void onError(String message);
    }

    private final Context appContext;
    private final AppExecutors executors;

    public BackupManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.executors = AppExecutors.getInstance();
    }

    /** Copies the live database file to the SAF-provided destination URI. */
    public void exportTo(Uri destinationUri, BackupCallback callback) {
        executors.diskIO().execute(() -> {
            File dbFile = appContext.getDatabasePath(AppDatabase.DATABASE_NAME);

            if (!dbFile.exists()) {
                postError(callback, "No data to back up yet.");
                return;
            }

            ContentResolver resolver = appContext.getContentResolver();
            try (InputStream in = new FileInputStream(dbFile);
                 OutputStream out = resolver.openOutputStream(destinationUri)) {

                if (out == null) throw new IOException("Could not open destination for writing.");
                copyStream(in, out);
                postSuccess(callback);

            } catch (IOException e) {
                postError(callback, "Backup failed: " + e.getMessage());
            }
        });
    }

    /**
     * Validates and copies a chosen file into the app's database slot.
     * Closes the active Room connection first (required), and the
     * caller is responsible for restarting the app process afterward.
     */
    public void importFrom(Uri sourceUri, BackupCallback callback) {
        executors.diskIO().execute(() -> {
            ContentResolver resolver = appContext.getContentResolver();
            File dbFile = appContext.getDatabasePath(AppDatabase.DATABASE_NAME);
            File tempFile = new File(dbFile.getParentFile(), "restore_temp.db");

            try (InputStream in = resolver.openInputStream(sourceUri);
                 OutputStream out = new FileOutputStream(tempFile)) {

                if (in == null) throw new IOException("Could not open selected file.");
                copyStream(in, out);

            } catch (IOException e) {
                postError(callback, "Could not read selected file: " + e.getMessage());
                return;
            }

            if (!isValidSqliteFile(tempFile)) {
                tempFile.delete();
                postError(callback, "Selected file is not a valid Mess Manager backup.");
                return;
            }

            // Safe to overwrite now: close Room's connection before touching the file on disk.
            AppDatabase.closeAndClearInstance();

            if (!tempFile.renameTo(dbFile)) {
                postError(callback, "Could not apply the backup file.");
                return;
            }

            postSuccess(callback);
        });
    }

    private boolean isValidSqliteFile(File file) {
        if (file.length() < SQLITE_HEADER.length) return false;
        byte[] headerBytes = new byte[SQLITE_HEADER.length];
        try (InputStream in = new FileInputStream(file)) {
            int read = in.read(headerBytes);
            return read == SQLITE_HEADER.length && java.util.Arrays.equals(headerBytes, SQLITE_HEADER);
        } catch (IOException e) {
            return false;
        }
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
    }

    private void postSuccess(BackupCallback callback) {
        executors.mainThread().post(callback::onSuccess);
    }

    private void postError(BackupCallback callback, String message) {
        executors.mainThread().post(() -> callback.onError(message));
    }
}