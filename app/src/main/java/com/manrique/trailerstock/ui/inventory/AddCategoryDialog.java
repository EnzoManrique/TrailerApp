package com.manrique.trailerstock.ui.inventory;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.Categoria;

import java.util.List;

public class AddCategoryDialog extends DialogFragment {

    private TextInputEditText etCategoryName;
    private MaterialButton btnAdd;
    private MaterialButton btnCancel;
    private OnCategoryAddedListener listener;

    public interface OnCategoryAddedListener {
        void onCategoryAdded(Categoria categoria);
    }

    public static AddCategoryDialog newInstance() {
        return new AddCategoryDialog();
    }

    public void setOnCategoryAddedListener(OnCategoryAddedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Usar ContextThemeWrapper para asegurar el tema correcto
        android.view.ContextThemeWrapper themedContext = new android.view.ContextThemeWrapper(
                requireContext(),
                com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog);

        View view = LayoutInflater.from(themedContext).inflate(R.layout.dialog_add_category, null);

        // Inicializar vistas
        etCategoryName = view.findViewById(R.id.et_category_name);
        btnAdd = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Listeners
        btnCancel.setOnClickListener(v -> dismiss());
        btnAdd.setOnClickListener(v -> saveCategory());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        // Hacer el diálogo con bordes redondeados
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.clay_card_background);
        }

        return dialog;
    }

    private void saveCategory() {
        String name = etCategoryName.getText().toString().trim();

        // Validación
        if (TextUtils.isEmpty(name)) {
            etCategoryName.setError("Ingresa un nombre");
            return;
        }

        // Verificar si ya existe en background thread
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            List<Categoria> categorias = db.categoriaDao().obtenerTodas();

            // Verificar duplicados
            boolean exists = false;
            for (Categoria cat : categorias) {
                if (cat.getNombre().equalsIgnoreCase(name)) {
                    exists = true;
                    break;
                }
            }

            boolean finalExists = exists;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (finalExists) {
                        etCategoryName.setError("Esta categoría ya existe");
                    } else {
                        // Insertar nueva categoría
                        Categoria nuevaCategoria = new Categoria(name);
                        new Thread(() -> {
                            db.categoriaDao().insertar(nuevaCategoria);

                            // Obtener la categoría insertada con su ID
                            List<Categoria> cats = db.categoriaDao().obtenerTodas();
                            Categoria insertada = null;
                            for (Categoria cat : cats) {
                                if (cat.getNombre().equals(name)) {
                                    insertada = cat;
                                    break;
                                }
                            }

                            Categoria finalInsertada = insertada;
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (listener != null && finalInsertada != null) {
                                        listener.onCategoryAdded(finalInsertada);
                                    }
                                    Toast.makeText(getContext(), "Categoría agregada", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                });
                            }
                        }).start();
                    }
                });
            }
        }).start();
    }
}
