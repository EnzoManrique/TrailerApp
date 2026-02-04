package com.manrique.trailerstock.ui.statistics.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.VentasPorDia;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SalesChartDialog extends DialogFragment {

    private LineChart lineChart;
    private TextView tvChartTotal, tvChartAverage;
    private ChipGroup chipGroupPeriod;
    private Chip chip7Days, chip30Days, chipCustom;
    private LinearLayout layoutDateRange;
    private MaterialButton btnStartDate, btnEndDate, btnClose;

    private AppDatabase database;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat dateFormatter;

    private long startDate;
    private long endDate;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.dialog_sales_chart, null, false);

        // Inicializar vistas
        lineChart = view.findViewById(R.id.line_chart);
        tvChartTotal = view.findViewById(R.id.tv_chart_total);
        tvChartAverage = view.findViewById(R.id.tv_chart_average);
        chipGroupPeriod = view.findViewById(R.id.chip_group_period);
        chip7Days = view.findViewById(R.id.chip_7_days);
        chip30Days = view.findViewById(R.id.chip_30_days);
        chipCustom = view.findViewById(R.id.chip_custom);
        layoutDateRange = view.findViewById(R.id.layout_date_range);
        btnStartDate = view.findViewById(R.id.btn_start_date);
        btnEndDate = view.findViewById(R.id.btn_end_date);
        btnClose = view.findViewById(R.id.btn_close);

        // Inicializar formatters
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        currencyFormatter.setMaximumFractionDigits(2);
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Inicializar database
        database = AppDatabase.getDatabase(requireContext());

        // Configurar listeners
        setupListeners();

        // Cargar datos iniciales (últimos 7 días)
        loadData(7);

        // Configurar el diálogo
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(view);

        return builder.create();
    }

    private void setupListeners() {
        chipGroupPeriod.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_7_days)) {
                layoutDateRange.setVisibility(View.GONE);
                loadData(7);
            } else if (checkedIds.contains(R.id.chip_30_days)) {
                layoutDateRange.setVisibility(View.GONE);
                loadData(30);
            } else if (checkedIds.contains(R.id.chip_custom)) {
                layoutDateRange.setVisibility(View.VISIBLE);
            }
        });

        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));
        btnClose.setOnClickListener(v -> dismiss());
    }

    private void loadData(int days) {
        new Thread(() -> {
            Calendar cal = Calendar.getInstance();
            endDate = cal.getTimeInMillis();

            cal.add(Calendar.DAY_OF_YEAR, -days);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            startDate = cal.getTimeInMillis();

            List<VentasPorDia> ventas = database.ventaDao().obtenerVentasPorDia(startDate, endDate);

            requireActivity().runOnUiThread(() -> {
                updateChart(ventas);
                updateMetrics(ventas);
            });
        }).start();
    }

    private void loadDataCustomRange() {
        new Thread(() -> {
            List<VentasPorDia> ventas = database.ventaDao().obtenerVentasPorDia(startDate, endDate);

            requireActivity().runOnUiThread(() -> {
                updateChart(ventas);
                updateMetrics(ventas);
            });
        }).start();
    }

    private void updateChart(List<VentasPorDia> ventas) {
        if (ventas == null || ventas.isEmpty()) {
            lineChart.clear();
            lineChart.setNoDataText("No hay datos para mostrar en este período");
            return;
        }

        List<Entry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();

        for (int i = 0; i < ventas.size(); i++) {
            VentasPorDia venta = ventas.get(i);
            entries.add(new Entry(i, (float) venta.getTotalVentas()));

            // Formatear fecha para el eje X (solo día/mes)
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
                Date date = inputFormat.parse(venta.getFecha());
                labels.add(outputFormat.format(date));
            } catch (ParseException e) {
                labels.add(venta.getFecha());
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Ventas");
        dataSet.setColor(getResources().getColor(R.color.primary, null));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(getResources().getColor(R.color.primary, null));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Configurar eje X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });

        // Configurar apariencia
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    private void updateMetrics(List<VentasPorDia> ventas) {
        if (ventas == null || ventas.isEmpty()) {
            tvChartTotal.setText("$0.00");
            tvChartAverage.setText("$0.00");
            return;
        }

        double total = 0;
        for (VentasPorDia venta : ventas) {
            total += venta.getTotalVentas();
        }

        double average = total / ventas.size();

        tvChartTotal.setText(currencyFormatter.format(total));
        tvChartAverage.setText(currencyFormatter.format(average));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar cal = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, month, dayOfMonth);

                    if (isStartDate) {
                        selectedCal.set(Calendar.HOUR_OF_DAY, 0);
                        selectedCal.set(Calendar.MINUTE, 0);
                        selectedCal.set(Calendar.SECOND, 0);
                        selectedCal.set(Calendar.MILLISECOND, 0);
                        startDate = selectedCal.getTimeInMillis();
                        btnStartDate.setText(dateFormatter.format(selectedCal.getTime()));
                    } else {
                        selectedCal.set(Calendar.HOUR_OF_DAY, 23);
                        selectedCal.set(Calendar.MINUTE, 59);
                        selectedCal.set(Calendar.SECOND, 59);
                        selectedCal.set(Calendar.MILLISECOND, 999);
                        endDate = selectedCal.getTimeInMillis();
                        btnEndDate.setText(dateFormatter.format(selectedCal.getTime()));
                    }

                    // Si ambas fechas están seleccionadas, cargar datos
                    if (startDate > 0 && endDate > 0) {
                        loadDataCustomRange();
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }
}
