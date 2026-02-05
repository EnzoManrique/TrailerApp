package com.manrique.trailerstock.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Información de un archivo de backup
 */
public class BackupInfo {

    private String fileName;
    private long timestamp;
    private long sizeBytes;
    private String location; // "local" o "drive"
    private boolean isValid;
    private int recordsCount;

    public BackupInfo(String fileName, long timestamp, long sizeBytes, String location) {
        this.fileName = fileName;
        this.timestamp = timestamp;
        this.sizeBytes = sizeBytes;
        this.location = location;
        this.isValid = true;
        this.recordsCount = 0;
    }

    // Getters
    public String getFileName() {
        return fileName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getLocation() {
        return location;
    }

    public boolean isValid() {
        return isValid;
    }

    public int getRecordsCount() {
        return recordsCount;
    }

    // Setters
    public void setValid(boolean valid) {
        isValid = valid;
    }

    public void setRecordsCount(int recordsCount) {
        this.recordsCount = recordsCount;
    }

    // Utility methods
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getFormattedSize() {
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        } else if (sizeBytes < 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.2f KB", sizeBytes / 1024.0);
        } else {
            return String.format(Locale.getDefault(), "%.2f MB", sizeBytes / (1024.0 * 1024.0));
        }
    }

    public boolean isLocal() {
        return "local".equals(location);
    }

    public boolean isDrive() {
        return "drive".equals(location);
    }
}
