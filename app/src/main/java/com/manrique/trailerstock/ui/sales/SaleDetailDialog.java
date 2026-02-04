package com.manrique.trailerstock.ui.sales;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
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
import com.manrique.trailerstock.model.Venta;
import com.manrique.trailerstock.model.VentaDetalleConProducto;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaleDetailDialog extends DialogFragment {

    private Venta venta;
    private SaleDetailAdapter adapter;

    public static SaleDetailDialog newInstance(Venta venta) {
        SaleDetailDialog dialog = new SaleDetailDialog();
        dialog.venta = venta;
        return dialog;
    }

    public static SaleDetailDialog newInstance(int ventaId) {
        SaleDetailDialog dialog = new SaleDetailDialog();
        // Will load venta in onCreateDialog
        Bundle args = new Bundle();
        args.putInt("ventaId", ventaId);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Check if venta ID was passed via arguments
        if (venta == null && getArguments() != null && getArguments().containsKey("ventaId")) {
            int ventaId = getArguments().getInt("ventaId");
            // Load venta from database
            new Thread(() -> {
                AppDatabase db = AppDatabase.getDatabase(requireContext());
                venta = db.ventaDao().obtenerPorId(ventaId);
                requireActivity().runOnUiThread(() -> {
                    // Reload dialog with venta data
                    if (getDialog() != null) {
                        dismiss();
                        SaleDetailDialog newDialog = SaleDetailDialog.newInstance(venta);
                        newDialog.show(getParentFragmentManager(), "SaleDetailDialog");
                    }
                });
            }).start();
            // Return empty dialog temporarily
            return new MaterialAlertDialogBuilder(requireContext()).create();
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View view = inflater.inflate(R.layout.dialog_sale_detail, null);

        // Inicializar vistas
        TextView tvSaleDate = view.findViewById(R.id.tv_sale_date);
        TextView tvCustomerType = view.findViewById(R.id.tv_customer_type);
        TextView tvTotal = view.findViewById(R.id.tv_total);
        RecyclerView rvSaleDetails = view.findViewById(R.id.rv_sale_details);
        ImageButton btnClose = view.findViewById(R.id.btn_close);
        MaterialButton btnOk = view.findViewById(R.id.btn_ok);

        // Configurar RecyclerView
        rvSaleDetails.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SaleDetailAdapter();
        rvSaleDetails.setAdapter(adapter);

        // Mostrar información de la venta
        if (venta != null) {
            // Fecha
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm",
                    new Locale("es", "ES"));
            String formattedDate = dateFormat.format(new Date(venta.getFecha()));
            tvSaleDate.setText(formattedDate);

            // Tipo de cliente
            tvCustomerType.setText(venta.getTipoCliente());

            // Total
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
            formatter.setMaximumFractionDigits(2);
            tvTotal.setText(formatter.format(venta.getTotal()));

            // Cargar detalles en background
            loadSaleDetails();
        }

        // Botones cerrar
        btnClose.setOnClickListener(v -> dismiss());
        btnOk.setOnClickListener(v -> dismiss());

        // Crear diálogo
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setView(view);

        return builder.create();
    }

    private void loadSaleDetails() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            List<VentaDetalleConProducto> detalles = db.ventaDetalleDao().obtenerDetallesConProducto(venta.getId());

            // Actualizar UI en main thread
            requireActivity().runOnUiThread(() -> {
                adapter.setDetalles(detalles);
            });
        }).start();
    }
}
