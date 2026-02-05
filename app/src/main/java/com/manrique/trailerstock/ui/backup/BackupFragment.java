package com.manrique.trailerstock.ui.backup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.model.BackupInfo;
import com.manrique.trailerstock.utils.BackupManager;
import com.manrique.trailerstock.utils.PreferencesManager;
import com.manrique.trailerstock.workers.BackupWorker;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Fragment para gestionar backups y restore
 */
public class BackupFragment extends Fragment {

    private static final String BACKUP_WORK_TAG = "backup_work";
    private static final int REQUEST_RESTORE_FILE = 100;

    private PreferencesManager preferencesManager;

    // UI Components
    private TextView tvLastBackup, tvDriveStatus, tvBackupCount, tvBackupSize;
    private MaterialButton btnCreateBackup, btnShareBackup, btnDriveConnect;
    private MaterialButton btnViewBackups, btnRestoreFromFile;
    private SwitchMaterial switchAutoBackup, switchDriveUpload;
    private RadioGroup radioGroupFrequency;
    private SeekBar seekbarMaxBackups;
    private TextView tvMaxBackupsLabel;
    private View layoutDriveConnected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_backup, container, false);

        // Inicializar PreferencesManager
        preferencesManager = PreferencesManager.getInstance(requireContext());

        // Inicializar vistas
        initViews(view);

        // Cargar configuración actual
        loadCurrentSettings();

        // Configurar listeners
        setupListeners();

        // Actualizar información
        updateBackupInfo();

        return view;
    }

    private void initViews(View view) {
        // Backup Manual
        tvLastBackup = view.findViewById(R.id.tv_last_backup);
        btnCreateBackup = view.findViewById(R.id.btn_create_backup);
        btnShareBackup = view.findViewById(R.id.btn_share_backup);

        // Backup Automático
        switchAutoBackup = view.findViewById(R.id.switch_auto_backup);
        radioGroupFrequency = view.findViewById(R.id.radio_group_frequency);
        seekbarMaxBackups = view.findViewById(R.id.seekbar_max_backups);
        tvMaxBackupsLabel = view.findViewById(R.id.tv_max_backups_label);

        // Google Drive
        tvDriveStatus = view.findViewById(R.id.tv_drive_status);
        btnDriveConnect = view.findViewById(R.id.btn_drive_connect);
        switchDriveUpload = view.findViewById(R.id.switch_drive_upload);
        layoutDriveConnected = view.findViewById(R.id.layout_drive_connected);

        // Restaurar
        btnViewBackups = view.findViewById(R.id.btn_view_backups);
        btnRestoreFromFile = view.findViewById(R.id.btn_restore_from_file);

        // Información
        tvBackupCount = view.findViewById(R.id.tv_backup_count);
        tvBackupSize = view.findViewById(R.id.tv_backup_size);
    }

    private void loadCurrentSettings() {
        // Backup automático
        switchAutoBackup.setChecked(preferencesManager.isAutoBackupEnabled());

        String frequency = preferencesManager.getAutoBackupFrequency();
        if ("DAILY".equals(frequency)) {
            radioGroupFrequency.check(R.id.radio_daily);
        } else {
            radioGroupFrequency.check(R.id.radio_weekly);
        }

        int maxBackups = preferencesManager.getMaxLocalBackups();
        seekbarMaxBackups.setProgress(maxBackups);
        tvMaxBackupsLabel.setText(maxBackups + " backups");

        // Google Drive
        String driveAccount = preferencesManager.getDriveAccount();
        if (driveAccount != null) {
            tvDriveStatus.setText("Conectado: " + driveAccount);
            btnDriveConnect.setText("Desconectar");
            layoutDriveConnected.setVisibility(View.VISIBLE);
            switchDriveUpload.setChecked(preferencesManager.isDriveBackupEnabled());
        }
    }

    private void setupListeners() {
        // Crear backup manual
        btnCreateBackup.setOnClickListener(v -> createBackup());

        // Compartir último backup
        btnShareBackup.setOnClickListener(v -> shareLastBackup());

        // Toggle backup automático
        switchAutoBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setAutoBackupEnabled(isChecked);
            if (isChecked) {
                scheduleAutomaticBackup();
            } else {
                cancelAutomaticBackup();
            }
        });

        // Cambio de frecuencia
        radioGroupFrequency.setOnCheckedChangeListener((group, checkedId) -> {
            String frequency = checkedId == R.id.radio_daily ? "DAILY" : "WEEKLY";
            preferencesManager.setAutoBackupFrequency(frequency);
            if (switchAutoBackup.isChecked()) {
                scheduleAutomaticBackup();
            }
        });

        // Cambio de cantidad máxima de backups
        seekbarMaxBackups.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvMaxBackupsLabel.setText(progress + " backups");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                preferencesManager.setMaxLocalBackups(seekBar.getProgress());
            }
        });

        // Botones de Drive (placeholder)
        btnDriveConnect.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "Google Drive: Próximamente",
                    Toast.LENGTH_SHORT).show();
        });

        // Ver backups
        btnViewBackups.setOnClickListener(v -> showBackupList());

        // Restaurar desde archivo
        btnRestoreFromFile.setOnClickListener(v -> selectRestoreFile());
    }

    private void createBackup() {
        ProgressDialog progress = new ProgressDialog(requireContext());
        progress.setMessage("Creando backup...");
        progress.setCancelable(false);
        progress.show();

        new Thread(() -> {
            try {
                File backupFile = BackupManager.createLocalBackup(requireContext());

                requireActivity().runOnUiThread(() -> {
                    progress.dismiss();
                    Toast.makeText(requireContext(),
                            "Backup creado: " + backupFile.getName(),
                            Toast.LENGTH_LONG).show();
                    updateBackupInfo();
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    progress.dismiss();
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Error")
                            .setMessage("No se pudo crear el backup: " + e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                });
            }
        }).start();
    }

    private void shareLastBackup() {
        List<BackupInfo> backups = BackupManager.listLocalBackups(requireContext());

        if (backups.isEmpty()) {
            Toast.makeText(requireContext(),
                    "No hay backups para compartir",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        BackupInfo lastBackup = backups.get(0);
        File backupFile = new File(
                requireContext().getExternalFilesDir(null) + "/backups/" + lastBackup.getFileName());

        BackupManager.shareBackup(requireContext(), backupFile);
    }

    private void scheduleAutomaticBackup() {
        String frequency = preferencesManager.getAutoBackupFrequency();
        long intervalHours = "DAILY".equals(frequency) ? 24 : 168; // 24h o 7 días

        PeriodicWorkRequest backupWork = new PeriodicWorkRequest.Builder(
                BackupWorker.class,
                intervalHours,
                TimeUnit.HOURS).addTag(BACKUP_WORK_TAG).build();

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                BACKUP_WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                backupWork);

        Toast.makeText(requireContext(),
                "Backup automático programado (" + frequency.toLowerCase() + ")",
                Toast.LENGTH_SHORT).show();
    }

    private void cancelAutomaticBackup() {
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(BACKUP_WORK_TAG);
        Toast.makeText(requireContext(),
                "Backup automático desactivado",
                Toast.LENGTH_SHORT).show();
    }

    private void showBackupList() {
        List<BackupInfo> backups = BackupManager.listLocalBackups(requireContext());

        if (backups.isEmpty()) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Backups Guardados")
                    .setMessage("No hay backups disponibles")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // Crear lista de backups para mostrar
        String[] backupNames = new String[backups.size()];
        for (int i = 0; i < backups.size(); i++) {
            BackupInfo info = backups.get(i);
            backupNames[i] = info.getFormattedDate() + " - " + info.getFormattedSize();
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Backups Guardados (" + backups.size() + ")")
                .setItems(backupNames, (dialog, which) -> {
                    showBackupOptions(backups.get(which));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showBackupOptions(BackupInfo backup) {
        File backupFile = new File(
                requireContext().getExternalFilesDir(null) + "/backups/" + backup.getFileName());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(backup.getFileName())
                .setMessage("Fecha: " + backup.getFormattedDate() + "\n" +
                        "Tamaño: " + backup.getFormattedSize() + "\n" +
                        "Válido: " + (backup.isValid() ? "Sí" : "No"))
                .setPositiveButton("Restaurar", (d, w) -> confirmRestore(backupFile))
                .setNeutralButton("Compartir", (d, w) -> BackupManager.shareBackup(requireContext(), backupFile))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void selectRestoreFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_RESTORE_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_RESTORE_FILE && resultCode == android.app.Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                // TODO: convertir URI a File y restaurar
                Toast.makeText(requireContext(),
                        "Restauración desde archivo: Próximamente",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void confirmRestore(File backupFile) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("⚠️ Restaurar Backup")
                .setMessage("Esto reemplazará TODOS tus datos actuales.\n\n¿Estás seguro?")
                .setPositiveButton("Restaurar", (d, w) -> performRestore(backupFile))
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performRestore(File backupFile) {
        ProgressDialog progress = new ProgressDialog(requireContext());
        progress.setMessage("Restaurando datos...");
        progress.setCancelable(false);
        progress.show();

        new Thread(() -> {
            try {
                Thread.sleep(1000); // Dar tiempo para que se muestre el dialog

                BackupManager.restoreFromLocal(requireContext(), backupFile);

                // La app se reiniciará automáticamente

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    progress.dismiss();
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Error")
                            .setMessage("No se pudo restaurar: " + e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                });
            }
        }).start();
    }

    private void updateBackupInfo() {
        new Thread(() -> {
            List<BackupInfo> backups = BackupManager.listLocalBackups(requireContext());
            long totalSize = BackupManager.getTotalBackupSize(requireContext());

            requireActivity().runOnUiThread(() -> {
                tvBackupCount.setText("Backups locales: " + backups.size());
                tvBackupSize.setText(String.format("Espacio usado: %.2f MB",
                        totalSize / (1024.0 * 1024.0)));

                if (!backups.isEmpty()) {
                    BackupInfo last = backups.get(0);
                    tvLastBackup.setText("Último backup: " + last.getFormattedDate());
                } else {
                    tvLastBackup.setText("Último backup: Nunca");
                }
            });
        }).start();
    }
}
