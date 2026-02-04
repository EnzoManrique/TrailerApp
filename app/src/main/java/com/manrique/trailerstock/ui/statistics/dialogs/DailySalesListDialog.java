package com.manrique.trailerstock.ui.statistics.dialogs;

import android.app.DatePickerDialog;
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
import com.manrique.trailerstock.model.VentaConDetalles;
import com.manrique.trailerstock.ui.sales.SaleDetailDialog;
import com.manrique.trailerstock.ui.statistics.adapters.VentaResumidaAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DailySalesListDialog extends DialogFragment {

    private RecyclerView rvSales;
    private TextView tvNoSales;
    private MaterialButton btnSelectDate, btnClose;

    private VentaResumidaAdapter adapter;
    private AppDatabase database;
    private SimpleDateFormat dateFormatter;

    private Calendar selectedDate;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.dialog_daily_sales_list, null, false);

        // Inicializar vistas
        rvSales = view.findViewById(R.id.rv_sales);
        tvNoSales = view.findViewById(R.id.tv_no_sales);
        btnSelectDate = view.findViewById(R.id.btn_select_date);
        btnClose = view.findViewById(R.id.btn_close);

        // Inicializar formatter
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Inicializar database
        database = AppDatabase.getDatabase(requireContext());

        // Configurar RecyclerView
        rvSales.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VentaResumidaAdapter(this::onVentaClick);
        rvSales.setAdapter(adapter);

        // Inicializar con fecha actual
        selectedDate = Calendar.getInstance();
        updateDateButton();

        // Cargar datos
        loadSales();

        // Configurar listeners
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnClose.setOnClickListener(v -> dismiss());

        // Crear diálogo
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(view);

        return builder.create();
    }

    private void loadSales() {
        new Thread(() -> {
            // Calcular inicio y fin del día
            Calendar startCal = (Calendar) selectedDate.clone();
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
            long startDay = startCal.getTimeInMillis();

            Calendar endCal = (Calendar) selectedDate.clone();
            endCal.set(Calendar.HOUR_OF_DAY, 23);
            endCal.set(Calendar.MINUTE, 59);
            endCal.set(Calendar.SECOND, 59);
            endCal.set(Calendar.MILLISECOND, 999);
            long endDay = endCal.getTimeInMillis();

            List<VentaConDetalles> ventas = database.ventaDao().obtenerVentasDelDia(startDay, endDay);

            requireActivity().runOnUiThread(() -> {
                if (ventas == null || ventas.isEmpty()) {
                    rvSales.setVisibility(View.GONE);
                    tvNoSales.setVisibility(View.VISIBLE);
                } else {
                    rvSales.setVisibility(View.VISIBLE);
                    tvNoSales.setVisibility(View.GONE);
                    adapter.setVentas(ventas);
                }
            });
        }).start();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateDateButton();
                    loadSales();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void updateDateButton() {
        String dateText = dateFormatter.format(selectedDate.getTime());
        btnSelectDate.setText(dateText);
    }

    private void onVentaClick(VentaConDetalles venta) {
        // Mostrar detalles de la venta usando el SaleDetailDialog existente
        SaleDetailDialog dialog = SaleDetailDialog.newInstance(venta.getVentaId());
        dialog.show(getParentFragmentManager(), "SaleDetailDialog");
    }
}
