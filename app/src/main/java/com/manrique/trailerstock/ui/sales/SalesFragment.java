package com.manrique.trailerstock.ui.sales;

import android.content.Intent;
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
import com.manrique.trailerstock.model.Venta;

public class SalesFragment extends Fragment {

    private SalesViewModel viewModel;
    private SaleAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sales, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        recyclerView = view.findViewById(R.id.rv_sales);
        emptyState = view.findViewById(R.id.layout_empty_state);
        FloatingActionButton fabNewSale = view.findViewById(R.id.fab_new_sale);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SaleAdapter(this::onSaleClick);
        recyclerView.setAdapter(adapter);

        // Configurar ViewModel
        viewModel = new ViewModelProvider(this).get(SalesViewModel.class);

        // Observar cambios en las ventas
        viewModel.getAllVentas().observe(getViewLifecycleOwner(), ventas -> {
            adapter.setVentas(ventas);
            updateEmptyState(ventas == null || ventas.isEmpty());
        });

        // Configurar FAB
        fabNewSale.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), NewSaleActivity.class);
            startActivity(intent);
        });
    }

    private void onSaleClick(Venta venta) {
        // TODO: Implementar vista de detalles de venta en una fase futura
        // Por ahora no hace nada
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
}
