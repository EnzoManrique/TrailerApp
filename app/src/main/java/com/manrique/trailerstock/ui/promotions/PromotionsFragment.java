package com.manrique.trailerstock.ui.promotions;

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
import com.manrique.trailerstock.model.Promocion;
import com.manrique.trailerstock.model.PromocionProducto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PromotionsFragment extends Fragment implements PromotionAdapter.OnPromotionClickListener {

    private PromotionsViewModel viewModel;
    private PromotionAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyStateLayout;
    private FloatingActionButton fabAddPromotion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_promotions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        recyclerView = view.findViewById(R.id.rv_promotions);
        emptyStateLayout = view.findViewById(R.id.layout_empty_state);
        fabAddPromotion = view.findViewById(R.id.fab_add_promotion);

        // Configurar RecyclerView
        adapter = new PromotionAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(PromotionsViewModel.class);

        // Observar cambios en la lista de promociones
        viewModel.getAllPromociones().observe(getViewLifecycleOwner(), promociones -> {
            adapter.setPromociones(promociones);

            // Mostrar/ocultar vista vacía
            if (promociones == null || promociones.isEmpty()) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            // Actualizar contador de productos por promoción
            loadProductCount();
        });

        // Click en FAB para agregar promoción
        fabAddPromotion.setOnClickListener(v -> {
            AddEditPromotionDialog dialog = AddEditPromotionDialog.newInstance(null);
            dialog.setOnPromotionSavedListener(promocion -> {
                // El diálogo ya guardó en BD, solo refrescamos
                viewModel.loadPromociones();
            });
            dialog.show(getParentFragmentManager(), "AddPromotionDialog");
        });
    }

    private void loadProductCount() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            List<PromocionProducto> allRelations = db.promocionProductoDao().obtenerProductosPorPromocion(-1);

            // Contar productos por promoción
            Map<Integer, Integer> productCount = new HashMap<>();

            // Obtener todas las promociones
            List<Promocion> promociones = db.promocionDao().obtenerTodas();
            for (Promocion promocion : promociones) {
                List<PromocionProducto> relations = db.promocionProductoDao()
                        .obtenerProductosPorPromocion(promocion.getId());
                productCount.put(promocion.getId(), relations.size());
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.setProductCount(productCount);
                });
            }
        }).start();
    }

    @Override
    public void onEditClick(Promocion promocion) {
        AddEditPromotionDialog dialog = AddEditPromotionDialog.newInstance(promocion);
        dialog.setOnPromotionSavedListener(editedPromocion -> {
            // El diálogo ya actualizó en BD, solo refrescamos
            viewModel.loadPromociones();
        });
        dialog.show(getParentFragmentManager(), "EditPromotionDialog");
    }

    @Override
    public void onDeleteClick(Promocion promocion) {
        // Verificar si la promoción tiene productos asociados
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            List<PromocionProducto> productos = db.promocionProductoDao()
                    .obtenerProductosPorPromocion(promocion.getId());

            int productosAsociados = productos.size();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (productosAsociados > 0) {
                        // Mostrar confirmación especial
                        showDeleteWithProductsConfirmation(promocion, productosAsociados);
                    } else {
                        // Mostrar confirmación simple
                        showDeleteConfirmation(promocion);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onToggleActive(Promocion promocion, boolean isActive) {
        viewModel.toggleActive(promocion, isActive);
        String message = isActive ? "Promoción activada" : "Promoción desactivada";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void showDeleteWithProductsConfirmation(Promocion promocion, int productosAsociados) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar promoción")
                .setMessage("Esta promoción tiene " + productosAsociados +
                        " producto(s) asociado(s). ¿Estás seguro de eliminarla?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    viewModel.delete(promocion);
                    Toast.makeText(requireContext(), "Promoción eliminada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showDeleteConfirmation(Promocion promocion) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar promoción")
                .setMessage("¿Estás seguro de que deseas eliminar \"" + promocion.getNombrePromo() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    viewModel.delete(promocion);
                    Toast.makeText(requireContext(), "Promoción eliminada", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
