package com.manrique.trailerstock.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singleton para manejar las preferencias de la aplicación.
 * Gestiona configuraciones de formato de moneda, números y fechas.
 */
public class PreferencesManager {

    private static final String PREF_NAME = "TTMPreferences";

    // Claves de preferencias
    public static final String PREF_CURRENCY_CODE = "currency_code";
    public static final String PREF_NUMBER_FORMAT = "number_format";
    public static final String PREF_DATE_FORMAT = "date_format";

    // Valores por defecto
    public static final String DEFAULT_CURRENCY = "ARS";
    public static final String DEFAULT_NUMBER_FORMAT = "COMMA"; // vs "DOT"
    public static final String DEFAULT_DATE_FORMAT = "DD/MM/YYYY";

    // Formatos de números
    public static final String NUMBER_FORMAT_COMMA = "COMMA"; // 1.000,00
    public static final String NUMBER_FORMAT_DOT = "DOT"; // 1,000.00

    // Formatos de fecha
    public static final String DATE_FORMAT_DMY = "DD/MM/YYYY";
    public static final String DATE_FORMAT_MDY = "MM/DD/YYYY";
    public static final String DATE_FORMAT_YMD = "YYYY-MM-DD";

    // Backup automático
    public static final String PREF_AUTO_BACKUP_ENABLED = "auto_backup_enabled";
    public static final String PREF_AUTO_BACKUP_FREQUENCY = "auto_backup_frequency";
    public static final String PREF_MAX_LOCAL_BACKUPS = "max_local_backups";

    // Google Drive
    public static final String PREF_DRIVE_BACKUP_ENABLED = "drive_backup_enabled";
    public static final String PREF_DRIVE_ACCOUNT = "drive_account";
    public static final String PREF_LAST_DRIVE_SYNC = "last_drive_sync";

    // Valores por defecto backup
    public static final boolean DEFAULT_AUTO_BACKUP = false;
    public static final String DEFAULT_FREQUENCY = "DAILY"; // DAILY, WEEKLY
    public static final int DEFAULT_MAX_BACKUPS = 7;

    private static PreferencesManager instance;
    private final SharedPreferences preferences;

    private PreferencesManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context);
        }
        return instance;
    }

    // ===== CURRENCY =====

    public void setCurrencyCode(String code) {
        preferences.edit().putString(PREF_CURRENCY_CODE, code).apply();
    }

    public String getCurrencyCode() {
        return preferences.getString(PREF_CURRENCY_CODE, DEFAULT_CURRENCY);
    }

    // ===== NUMBER FORMAT =====

    public void setNumberFormat(String format) {
        preferences.edit().putString(PREF_NUMBER_FORMAT, format).apply();
    }

    public String getNumberFormat() {
        return preferences.getString(PREF_NUMBER_FORMAT, DEFAULT_NUMBER_FORMAT);
    }

    public boolean isCommaDecimalFormat() {
        return NUMBER_FORMAT_COMMA.equals(getNumberFormat());
    }

    // ===== DATE FORMAT =====

    public void setDateFormat(String format) {
        preferences.edit().putString(PREF_DATE_FORMAT, format).apply();
    }

    public String getDateFormat() {
        return preferences.getString(PREF_DATE_FORMAT, DEFAULT_DATE_FORMAT);
    }

    // ===== UTILITY METHODS =====

    /**
     * Obtiene el patrón de formato de fecha para SimpleDateFormat
     */
    public String getDatePattern() {
        String format = getDateFormat();
        switch (format) {
            case DATE_FORMAT_MDY:
                return "MM/dd/yyyy";
            case DATE_FORMAT_YMD:
                return "yyyy-MM-dd";
            case DATE_FORMAT_DMY:
            default:
                return "dd/MM/yyyy";
        }
    }

    /**
     * Obtiene el patrón de formato de fecha con hora
     */
    public String getDateTimePattern() {
        return getDatePattern() + " HH:mm";
    }

    // ===== BACKUP SETTINGS =====

    public void setAutoBackupEnabled(boolean enabled) {
        preferences.edit().putBoolean(PREF_AUTO_BACKUP_ENABLED, enabled).apply();
    }

    public boolean isAutoBackupEnabled() {
        return preferences.getBoolean(PREF_AUTO_BACKUP_ENABLED, DEFAULT_AUTO_BACKUP);
    }

    public void setAutoBackupFrequency(String frequency) {
        preferences.edit().putString(PREF_AUTO_BACKUP_FREQUENCY, frequency).apply();
    }

    public String getAutoBackupFrequency() {
        return preferences.getString(PREF_AUTO_BACKUP_FREQUENCY, DEFAULT_FREQUENCY);
    }

    public void setMaxLocalBackups(int count) {
        preferences.edit().putInt(PREF_MAX_LOCAL_BACKUPS, count).apply();
    }

    public int getMaxLocalBackups() {
        return preferences.getInt(PREF_MAX_LOCAL_BACKUPS, DEFAULT_MAX_BACKUPS);
    }

    public void setDriveBackupEnabled(boolean enabled) {
        preferences.edit().putBoolean(PREF_DRIVE_BACKUP_ENABLED, enabled).apply();
    }

    public boolean isDriveBackupEnabled() {
        return preferences.getBoolean(PREF_DRIVE_BACKUP_ENABLED, false);
    }

    public void setDriveAccount(String account) {
        preferences.edit().putString(PREF_DRIVE_ACCOUNT, account).apply();
    }

    public String getDriveAccount() {
        return preferences.getString(PREF_DRIVE_ACCOUNT, null);
    }

    public void setLastDriveSync(long timestamp) {
        preferences.edit().putLong(PREF_LAST_DRIVE_SYNC, timestamp).apply();
    }

    public long getLastDriveSync() {
        return preferences.getLong(PREF_LAST_DRIVE_SYNC, 0);
    }
}
