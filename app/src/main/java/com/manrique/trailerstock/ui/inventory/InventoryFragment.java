package com.manrique.trailerstock.ui.inventory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.Categoria;
import com.manrique.trailerstock.model.Producto;

import java.util.List;

public class InventoryFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private InventoryViewModel viewModel;
    private ProductAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyStateLayout;
    private FloatingActionButton fabAddProduct;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        recyclerView = view.findViewById(R.id.rv_products);
        emptyStateLayout = view.findViewById(R.id.layout_empty_state);
        fabAddProduct = view.findViewById(R.id.fab_add_product);

        // Configurar RecyclerView
        adapter = new ProductAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);

        // Cargar categorías en background thread y pasarlas al adapter
        loadCategories();

        // Observar cambios en la lista de productos
        viewModel.getAllProductos().observe(getViewLifecycleOwner(), productos -> {
            adapter.setProductos(productos);

            // Mostrar/ocultar vista vacía
            if (productos == null || productos.isEmpty()) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        // Click en FAB para agregar producto
        fabAddProduct.setOnClickListener(v -> {
            AddEditProductDialog dialog = AddEditProductDialog.newInstance(null);
            dialog.setOnProductSavedListener(producto -> viewModel.insert(producto));
            dialog.show(getParentFragmentManager(), "AddProductDialog");
        });
    }

    private void loadCategories() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            // Obtener categorías en background thread
            List<Categoria> categorias = db.categoriaDao().obtenerTodas();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Solo actualizar UI en main thread
                    adapter.setCategorias(categorias);
                });
            }
        }).start();
    }

    @Override
    public void onEditClick(Producto producto) {
        AddEditProductDialog dialog = AddEditProductDialog.newInstance(producto);
        dialog.setOnProductSavedListener(editedProducto -> viewModel.update(editedProducto));
        dialog.show(getParentFragmentManager(), "EditProductDialog");
    }

    @Override
    public void onDeleteClick(Producto producto) {
        // TODO: Mostrar diálogo de confirmación
        viewModel.delete(producto);
    }
}
