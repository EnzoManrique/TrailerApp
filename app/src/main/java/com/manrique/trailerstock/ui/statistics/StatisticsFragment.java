package com.manrique.trailerstock.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.manrique.trailerstock.R;
import com.manrique.trailerstock.model.ProductoVendido;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel viewModel;

    // Views
    private TextView tvVentasHoy, tvCantidadVentasHoy;
    private TextView tvVentasMes;
    private TextView tvStockBajo;
    private TextView tvVentasLista, tvVentasMayorista;
    private TextView tvTicketPromedio, tvMargenGanancia;
    private LinearLayout layoutTopProductos;
    private TextView tvNoData;
    private com.google.android.material.card.MaterialCardView cardVentasHoy, cardVentasMes, cardStockBajo;

    private NumberFormat currencyFormatter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar formatter de moneda
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        currencyFormatter.setMaximumFractionDigits(2);

        // Inicializar vistas
        tvVentasHoy = view.findViewById(R.id.tv_ventas_hoy);
        tvCantidadVentasHoy = view.findViewById(R.id.tv_cantidad_ventas_hoy);
        tvVentasMes = view.findViewById(R.id.tv_ventas_mes);
        tvStockBajo = view.findViewById(R.id.tv_stock_bajo);
        tvVentasLista = view.findViewById(R.id.tv_ventas_lista);
        tvVentasMayorista = view.findViewById(R.id.tv_ventas_mayorista);
        tvTicketPromedio = view.findViewById(R.id.tv_ticket_promedio);
        tvMargenGanancia = view.findViewById(R.id.tv_margen_ganancia);
        layoutTopProductos = view.findViewById(R.id.layout_top_productos);
        tvNoData = view.findViewById(R.id.tv_no_data);

        // Inicializar cards
        cardVentasHoy = view.findViewById(R.id.card_ventas_hoy);
        cardVentasMes = view.findViewById(R.id.card_ventas_mes);
        cardStockBajo = view.findViewById(R.id.card_stock_bajo);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        // Configurar click listeners
        setupClickListeners();

        // Observar datos
        observeData();

        // Cargar estadísticas
        viewModel.cargarEstadisticas();
    }

    private void observeData() {
        // Ventas del día
        viewModel.getVentasHoy().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                tvVentasHoy.setText(currencyFormatter.format(total));
            }
        });

        viewModel.getCantidadVentasHoy().observe(getViewLifecycleOwner(), cantidad -> {
            if (cantidad != null) {
                String texto = cantidad + (cantidad == 1 ? " venta" : " ventas");
                tvCantidadVentasHoy.setText(texto);
            }
        });

        // Ventas del mes
        viewModel.getVentasMes().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                tvVentasMes.setText(currencyFormatter.format(total));
            }
        });

        // Productos con stock bajo
        viewModel.getProductosStockBajo().observe(getViewLifecycleOwner(), cantidad -> {
            if (cantidad != null) {
                tvStockBajo.setText(String.valueOf(cantidad));
            }
        });

        // Top productos
        viewModel.getTopProductos().observe(getViewLifecycleOwner(), productos -> {
            mostrarTopProductos(productos);
        });

        // Ventas por tipo de cliente
        viewModel.getVentasLista().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                tvVentasLista.setText(currencyFormatter.format(total));
            }
        });

        viewModel.getVentasMayorista().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                tvVentasMayorista.setText(currencyFormatter.format(total));
            }
        });

        // Ticket promedio
        viewModel.getTicketPromedio().observe(getViewLifecycleOwner(), promedio -> {
            if (promedio != null) {
                tvTicketPromedio.setText(currencyFormatter.format(promedio));
            }
        });

        // Margen de ganancia
        viewModel.getMargenGanancia().observe(getViewLifecycleOwner(), margen -> {
            if (margen != null) {
                tvMargenGanancia.setText(currencyFormatter.format(margen));
            }
        });
    }

    private void setupClickListeners() {
        // Card Ventas Hoy - Abre listado de ventas
        cardVentasHoy.setOnClickListener(v -> {
            com.manrique.trailerstock.ui.statistics.dialogs.DailySalesListDialog dialog = new com.manrique.trailerstock.ui.statistics.dialogs.DailySalesListDialog();
            dialog.show(getParentFragmentManager(), "DailySalesListDialog");
        });

        // Card Ventas del Mes - Abre gráfico
        cardVentasMes.setOnClickListener(v -> {
            com.manrique.trailerstock.ui.statistics.dialogs.SalesChartDialog dialog = new com.manrique.trailerstock.ui.statistics.dialogs.SalesChartDialog();
            dialog.show(getParentFragmentManager(), "SalesChartDialog");
        });

        // Card Stock Bajo - Abre listado de productos
        cardStockBajo.setOnClickListener(v -> {
            com.manrique.trailerstock.ui.statistics.dialogs.LowStockProductsDialog dialog = new com.manrique.trailerstock.ui.statistics.dialogs.LowStockProductsDialog();
            dialog.show(getParentFragmentManager(), "LowStockProductsDialog");
        });
    }

    private void mostrarTopProductos(List<ProductoVendido> productos) {
        // Limpiar layout
        layoutTopProductos.removeAllViews();

        if (productos == null || productos.isEmpty()) {
            // Mostrar mensaje de sin datos
            TextView tvEmpty = new TextView(requireContext());
            tvEmpty.setText("No hay ventas aún");
            tvEmpty.setTextSize(14);
            tvEmpty.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            tvEmpty.setGravity(View.TEXT_ALIGNMENT_CENTER);
            layoutTopProductos.addView(tvEmpty);
            return;
        }

        // Agregar cada producto
        for (int i = 0; i < productos.size(); i++) {
            ProductoVendido producto = productos.get(i);

            // Crear layout horizontal para cada producto
            LinearLayout itemLayout = new LinearLayout(requireContext());
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(0, 8, 0, 8);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            itemLayout.setLayoutParams(layoutParams);

            // Número de ranking (sin emojis)
            TextView tvMedal = new TextView(requireContext());
            tvMedal.setText((i + 1) + ". ");
            tvMedal.setTextSize(16);
            tvMedal.setTextColor(getResources().getColor(R.color.primary, null));
            tvMedal.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            itemLayout.addView(tvMedal);

            // Nombre del producto
            TextView tvNombre = new TextView(requireContext());
            tvNombre.setText(producto.getNombreProducto());
            tvNombre.setTextSize(14);
            LinearLayout.LayoutParams nombreParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f);
            tvNombre.setLayoutParams(nombreParams);
            itemLayout.addView(tvNombre);

            // Cantidad vendida
            TextView tvCantidad = new TextView(requireContext());
            tvCantidad.setText(producto.getCantidadVendida() + " unid.");
            tvCantidad.setTextSize(14);
            tvCantidad.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            tvCantidad.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            itemLayout.addView(tvCantidad);

            layoutTopProductos.addView(itemLayout);
        }
    }
}
