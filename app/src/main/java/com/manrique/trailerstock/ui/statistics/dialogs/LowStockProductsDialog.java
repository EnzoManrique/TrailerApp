package com.manrique.trailerstock.ui.statistics.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.Categoria;
import com.manrique.trailerstock.model.Producto;
import com.manrique.trailerstock.ui.statistics.adapters.LowStockProductAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LowStockProductsDialog extends DialogFragment {

    private RecyclerView rvProducts;
    private TextView tvNoProducts;
    private MaterialButton btnClose;

    private LowStockProductAdapter adapter;
    private AppDatabase database;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.dialog_low_stock_products, null, false);

        // Inicializar vistas
        rvProducts = view.findViewById(R.id.rv_low_stock_products);
        tvNoProducts = view.findViewById(R.id.tv_no_products);
        btnClose = view.findViewById(R.id.btn_close);

        // Inicializar database
        database = AppDatabase.getDatabase(requireContext());

        // Cargar datos
        loadLowStockProducts();

        // Configurar listeners
        btnClose.setOnClickListener(v -> dismiss());

        // Crear diálogo
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(view);

        return builder.create();
    }

    private void loadLowStockProducts() {
        new Thread(() -> {
            // Obtener productos con stock bajo
            List<Producto> productos = database.productoDao().obtenerStockBajo();

            // Obtener categorías para el map
            List<Categoria> categorias = database.categoriaDao().obtenerTodas();
            Map<Integer, String> categoryMap = new HashMap<>();
            for (Categoria cat : categorias) {
                categoryMap.put(cat.getId(), cat.getNombre());
            }

            requireActivity().runOnUiThread(() -> {
                if (productos == null || productos.isEmpty()) {
                    rvProducts.setVisibility(View.GONE);
                    tvNoProducts.setVisibility(View.VISIBLE);
                } else {
                    rvProducts.setVisibility(View.VISIBLE);
                    tvNoProducts.setVisibility(View.GONE);

                    // Configurar RecyclerView
                    rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
                    adapter = new LowStockProductAdapter(categoryMap);
                    adapter.setProductos(productos);
                    rvProducts.setAdapter(adapter);
                }
            });
        }).start();
    }
}
