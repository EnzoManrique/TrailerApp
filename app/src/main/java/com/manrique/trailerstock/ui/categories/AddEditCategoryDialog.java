package com.manrique.trailerstock.ui.categories;

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
import com.manrique.trailerstock.model.Categoria;

public class AddEditCategoryDialog extends DialogFragment {

    private static final String ARG_CATEGORIA = "categoria";

    private TextInputEditText etCategoryName;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private OnCategorySavedListener listener;
    private Categoria categoriaToEdit;

    public interface OnCategorySavedListener {
        void onCategorySaved(Categoria categoria);
    }

    public static AddEditCategoryDialog newInstance(@Nullable Categoria categoria) {
        AddEditCategoryDialog dialog = new AddEditCategoryDialog();
        if (categoria != null) {
            Bundle args = new Bundle();
            args.putInt("id", categoria.getId());
            args.putString("nombre", categoria.getNombre());
            dialog.setArguments(args);
        }
        return dialog;
    }

    public void setOnCategorySavedListener(OnCategorySavedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Cargar categoría a editar si existe
        if (getArguments() != null) {
            categoriaToEdit = new Categoria(getArguments().getString("nombre"));
            categoriaToEdit.setId(getArguments().getInt("id"));
        }

        // Usar ContextThemeWrapper para asegurar el tema correcto
        android.view.ContextThemeWrapper themedContext = new android.view.ContextThemeWrapper(
                requireContext(),
                com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog);

        View view = LayoutInflater.from(themedContext).inflate(R.layout.dialog_add_category, null);

        // Inicializar vistas
        etCategoryName = view.findViewById(R.id.et_category_name);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Si es edición, llenar el campo con el nombre actual
        if (categoriaToEdit != null) {
            etCategoryName.setText(categoriaToEdit.getNombre());
        }

        // Listeners
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveCategory());

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

        // Crear o actualizar categoría
        Categoria categoria;
        if (categoriaToEdit != null) {
            // Modo edición
            categoria = categoriaToEdit;
            categoria.setNombre(name);
        } else {
            // Modo creación
            categoria = new Categoria(name);
        }

        // Llamar al listener
        if (listener != null) {
            listener.onCategorySaved(categoria);
        }

        String message = categoriaToEdit != null ? "Categoría actualizada" : "Categoría agregada";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
