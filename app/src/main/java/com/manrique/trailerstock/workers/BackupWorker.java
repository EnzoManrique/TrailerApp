package com.manrique.trailerstock.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.manrique.trailerstock.utils.BackupManager;
import com.manrique.trailerstock.utils.PreferencesManager;

import java.io.File;

/**
 * Worker para realizar backups automáticos en background
 */
public class BackupWorker extends Worker {

    private static final String TAG = "BackupWorker";

    public BackupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        PreferencesManager prefs = PreferencesManager.getInstance(context);

        try {
            Log.d(TAG, "Starting automatic backup...");

            // 1. Crear backup local
            File backupFile = BackupManager.createLocalBackup(context);
            Log.d(TAG, "Backup created: " + backupFile.getName());

            // 2. Limpiar backups antiguos
            int maxBackups = prefs.getMaxLocalBackups();
            BackupManager.cleanOldBackups(context, maxBackups);
            Log.d(TAG, "Old backups cleaned, keeping " + maxBackups);

            // 3. TODO: Si Drive está habilitado, subir backup
            if (prefs.isDriveBackupEnabled()) {
                // DriveBackupHelper.uploadInBackground(backupFile);
                Log.d(TAG, "Drive upload pending implementation");
            }

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Backup failed: " + e.getMessage(), e);
            return Result.failure();
        }
    }
}
