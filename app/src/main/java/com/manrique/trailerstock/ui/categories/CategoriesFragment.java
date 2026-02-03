package com.manrique.trailerstock.ui.categories;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.Categoria;
import com.manrique.trailerstock.model.Producto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoriesFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private CategoriesViewModel viewModel;
    private CategoryAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyStateLayout;
    private FloatingActionButton fabAddCategory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        recyclerView = view.findViewById(R.id.rv_categories);
        emptyStateLayout = view.findViewById(R.id.layout_empty_state);
        fabAddCategory = view.findViewById(R.id.fab_add_category);

        // Configurar RecyclerView
        adapter = new CategoryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(CategoriesViewModel.class);

        // Observar cambios en la lista de categorías
        viewModel.getAllCategorias().observe(getViewLifecycleOwner(), categorias -> {
            adapter.setCategorias(categorias);

            // Mostrar/ocultar vista vacía
            if (categorias == null || categorias.isEmpty()) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            // Actualizar contador de productos por categoría
            loadProductCount();
        });

        // Click en FAB para agregar categoría
        fabAddCategory.setOnClickListener(v -> {
            AddEditCategoryDialog dialog = AddEditCategoryDialog.newInstance(null);
            dialog.setOnCategorySavedListener(categoria -> viewModel.insert(categoria));
            dialog.show(getParentFragmentManager(), "AddCategoryDialog");
        });
    }

    private void loadProductCount() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            List<Producto> productos = db.productoDao().obtenerTodos();

            // Contar productos por categoría
            Map<Integer, Integer> productCount = new HashMap<>();
            for (Producto producto : productos) {
                int categoriaId = producto.getCategoriaId();
                productCount.put(categoriaId, productCount.getOrDefault(categoriaId, 0) + 1);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.setProductCount(productCount);
                });
            }
        }).start();
    }

    @Override
    public void onEditClick(Categoria categoria) {
        AddEditCategoryDialog dialog = AddEditCategoryDialog.newInstance(categoria);
        dialog.setOnCategorySavedListener(editedCategoria -> viewModel.update(editedCategoria));
        dialog.show(getParentFragmentManager(), "EditCategoryDialog");
    }

    @Override
    public void onDeleteClick(Categoria categoria) {
        // Verificar si la categoría tiene productos asociados
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            List<Producto> productos = db.productoDao().obtenerTodos();

            // Contar productos de esta categoría
            int productosAsociados = 0;
            for (Producto producto : productos) {
                if (producto.getCategoriaId() == categoria.getId()) {
                    productosAsociados++;
                }
            }

            int finalProductosAsociados = productosAsociados;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (finalProductosAsociados > 0) {
                        // Mostrar mensaje de error
                        new AlertDialog.Builder(requireContext())
                                .setTitle("No se puede eliminar")
                                .setMessage("Esta categoría tiene " + finalProductosAsociados +
                                        " producto(s) asociado(s). Elimina o reasigna los productos primero.")
                                .setPositiveButton("Entendido", null)
                                .show();
                    } else {
                        // Mostrar diálogo de confirmación
                        showDeleteConfirmation(categoria);
                    }
                });
            }
        }).start();
    }

    private void showDeleteConfirmation(Categoria categoria) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar categoría")
                .setMessage("¿Estás seguro de que deseas eliminar \"" + categoria.getNombre() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    viewModel.delete(categoria);
                    Toast.makeText(requireContext(), "Categoría eliminada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
