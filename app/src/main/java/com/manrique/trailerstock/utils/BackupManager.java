package com.manrique.trailerstock.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.BackupInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Gestiona operaciones de backup y restore de la base de datos
 */
public class BackupManager {

    private static final String TAG = "BackupManager";
    private static final String BACKUP_DIR = "backups";
    private static final String BACKUP_PREFIX = "backup_";
    private static final String BACKUP_EXTENSION = ".db";

    /**
     * Crea un backup local de la base de datos
     */
    public static File createLocalBackup(Context context) throws IOException {
        // Crear directorio de backups si no existe
        File backupDir = new File(context.getExternalFilesDir(null), BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }

        // Generar nombre de archivo con timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String backupFileName = BACKUP_PREFIX + timestamp + BACKUP_EXTENSION;
        File backupFile = new File(backupDir, backupFileName);

        // Cerrar la base de datos antes de copiar
        AppDatabase database = AppDatabase.getDatabase(context);
        database.close();

        // Copiar el archivo de la base de datos
        File dbFile = context.getDatabasePath("trailer_stock_db");
        if (!dbFile.exists()) {
            throw new IOException("Database file not found");
        }

        copyFile(dbFile, backupFile);

        return backupFile;
    }

    /**
     * Lista todos los backups locales disponibles
     */
    public static List<BackupInfo> listLocalBackups(Context context) {
        List<BackupInfo> backups = new ArrayList<>();
        File backupDir = new File(context.getExternalFilesDir(null), BACKUP_DIR);

        if (!backupDir.exists()) {
            return backups;
        }

        File[] files = backupDir
                .listFiles((dir, name) -> name.startsWith(BACKUP_PREFIX) && name.endsWith(BACKUP_EXTENSION));

        if (files == null || files.length == 0) {
            return backups;
        }

        // Ordenar por fecha (más reciente primero)
        Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

        for (File file : files) {
            BackupInfo info = new BackupInfo(
                    file.getName(),
                    file.lastModified(),
                    file.length(),
                    "local");

            // Validar backup
            info.setValid(isValidBackup(file));

            backups.add(info);
        }

        return backups;
    }

    /**
     * Valida que un archivo de backup sea correcto
     */
    public static boolean isValidBackup(File backupFile) {
        if (!backupFile.exists() || backupFile.length() == 0) {
            return false;
        }

        SQLiteDatabase db = null;
        try {
            // Intentar abrir la base de datos
            db = SQLiteDatabase.openDatabase(
                    backupFile.getPath(),
                    null,
                    SQLiteDatabase.OPEN_READONLY);

            // Verificar que existan las tablas requeridas
            String[] requiredTables = {
                    "productos", "ventas", "venta_detalles",
                    "categorias", "promociones"
            };

            for (String table : requiredTables) {
                Cursor cursor = db.rawQuery(
                        "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                        new String[] { table });

                boolean exists = cursor.getCount() > 0;
                cursor.close();

                if (!exists) {
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Restaura la base de datos desde un backup local
     */
    public static void restoreFromLocal(Context context, File backupFile) throws IOException {
        // Validar el backup primero
        if (!isValidBackup(backupFile)) {
            throw new IOException("Invalid backup file");
        }

        // Cerrar la base de datos
        AppDatabase database = AppDatabase.getDatabase(context);
        database.close();

        // Obtener el archivo de la base de datos actual
        File dbFile = context.getDatabasePath("trailer_stock_db");

        // Copiar el backup sobre la base de datos actual
        copyFile(backupFile, dbFile);

        // Reiniciar la app
        restartApp(context);
    }

    /**
     * Limpia backups antiguos, manteniendo solo los últimos N
     */
    public static void cleanOldBackups(Context context, int keepCount) {
        File backupDir = new File(context.getExternalFilesDir(null), BACKUP_DIR);

        if (!backupDir.exists()) {
            return;
        }

        File[] files = backupDir
                .listFiles((dir, name) -> name.startsWith(BACKUP_PREFIX) && name.endsWith(BACKUP_EXTENSION));

        if (files == null || files.length <= keepCount) {
            return;
        }

        // Ordenar por fecha (más antigua primero)
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));

        // Eliminar los más antiguos
        int toDelete = files.length - keepCount;
        for (int i = 0; i < toDelete; i++) {
            files[i].delete();
        }
    }

    /**
     * Comparte un archivo de backup
     */
    public static void shareBackup(Context context, File backupFile) {
        Uri backupUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                backupFile);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/octet-stream");
        shareIntent.putExtra(Intent.EXTRA_STREAM, backupUri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Backup TrailerStock - " + backupFile.getName());
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(shareIntent, "Compartir backup"));
    }

    /**
     * Obtiene el espacio total usado por backups
     */
    public static long getTotalBackupSize(Context context) {
        File backupDir = new File(context.getExternalFilesDir(null), BACKUP_DIR);

        if (!backupDir.exists()) {
            return 0;
        }

        File[] files = backupDir.listFiles();
        if (files == null) {
            return 0;
        }

        long total = 0;
        for (File file : files) {
            total += file.length();
        }

        return total;
    }

    // Métodos privados auxiliares

    private static void copyFile(File source, File destination) throws IOException {
        try (FileChannel sourceChannel = new FileInputStream(source).getChannel();
                FileChannel destChannel = new FileOutputStream(destination).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
    }

    private static void restartApp(Context context) {
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            System.exit(0);
        }
    }
}
