package com.manrique.trailerstock.ui.sales;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.model.Producto;
import java.util.List;
import java.util.Map;

public class ProductSelectorDialog extends DialogFragment {

    private ProductSelectorAdapter adapter;
    private OnProductWithQuantitySelectedListener listener;
    private List<Producto> availableProducts;
    private Map<Integer, String> categoryMap;
    private String tipoCliente;

    public interface OnProductWithQuantitySelectedListener {
        void onProductSelected(Producto producto, int cantidad);
    }

    public static ProductSelectorDialog newInstance(List<Producto> products, Map<Integer, String> categoryMap,
            String tipoCliente) {
        ProductSelectorDialog dialog = new ProductSelectorDialog();
        dialog.availableProducts = products;
        dialog.categoryMap = categoryMap;
        dialog.tipoCliente = tipoCliente;
        return dialog;
    }

    public void setListener(OnProductWithQuantitySelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.dialog_product_selector, null, false);

        // Inicializar vistas
        EditText etSearch = view.findViewById(R.id.et_search);
        RecyclerView rvProducts = view.findViewById(R.id.rv_products);
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel);

        // Configurar RecyclerView
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductSelectorAdapter(categoryMap, tipoCliente, this::showQuantityDialog);
        adapter.setProducts(availableProducts);
        rvProducts.setAdapter(adapter);

        // Configurar búsqueda
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Botón cancelar
        btnCancel.setOnClickListener(v -> dismiss());

        // Crear diálogo
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(view);

        return builder.create();
    }

    private void showQuantityDialog(Producto producto) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_quantity_input, null);
        TextInputEditText etQuantity = dialogView.findViewById(R.id.et_quantity);
        etQuantity.setText("1");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cantidad de " + producto.getNombre())
                .setMessage("Stock disponible: " + producto.getStockActual())
                .setView(dialogView)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String quantityStr = etQuantity.getText().toString();
                    if (!quantityStr.isEmpty()) {
                        int quantity = Integer.parseInt(quantityStr);
                        if (quantity <= 0) {
                            // Error: cantidad inválida
                            showError("La cantidad debe ser mayor a 0");
                        } else if (quantity > producto.getStockActual()) {
                            // Error: excede stock
                            showError("La cantidad excede el stock disponible (" + producto.getStockActual() + ")");
                        } else {
                            // OK
                            if (listener != null) {
                                listener.onProductSelected(producto, quantity);
                            }
                            dismiss();
                        }
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showError(String message) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
