package com.manrique.trailerstock.ui.promotions;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.Producto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PromotionProductSelectorDialog extends DialogFragment {

    private static final String ARG_SELECTED_PRODUCTS = "selected_products";

    private SearchView searchView;
    private RecyclerView rvProducts;
    private MaterialButton btnAccept;
    private MaterialButton btnCancel;

    private PromotionProductSelectorAdapter adapter;
    private OnProductsSelectedListener listener;
    private Map<Integer, Integer> initialSelectedProducts = new HashMap<>();

    public interface OnProductsSelectedListener {
        void onProductsSelected(Map<Integer, Integer> selectedProducts);
    }

    public static PromotionProductSelectorDialog newInstance(Map<Integer, Integer> selectedProducts) {
        PromotionProductSelectorDialog dialog = new PromotionProductSelectorDialog();
        // Copiar el mapa
        if (selectedProducts != null) {
            dialog.initialSelectedProducts = new HashMap<>(selectedProducts);
        }
        return dialog;
    }

    public void setOnProductsSelectedListener(OnProductsSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        android.view.ContextThemeWrapper themedContext = new android.view.ContextThemeWrapper(
                requireContext(),
                com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog);

        View view = LayoutInflater.from(themedContext).inflate(R.layout.dialog_promotion_product_selector, null);

        // Inicializar vistas
        searchView = view.findViewById(R.id.search_view);
        rvProducts = view.findViewById(R.id.rv_products);
        btnAccept = view.findViewById(R.id.btn_accept);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Configurar RecyclerView
        adapter = new PromotionProductSelectorAdapter();
        adapter.setSelectedProducts(initialSelectedProducts);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(adapter);

        // Cargar productos
        loadProducts();

        // Configurar SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        // Listeners
        btnCancel.setOnClickListener(v -> dismiss());
        btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductsSelected(adapter.getSelectedProducts());
            }
            dismiss();
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.clay_card_background);
        }

        return dialog;
    }

    private void loadProducts() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            List<Producto> productos = db.productoDao().obtenerTodos();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.setProductos(productos));
            }
        }).start();
    }
}
