package com.manrique.trailerstock.ui.sales;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.model.Venta;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaleAdapter extends RecyclerView.Adapter<SaleAdapter.SaleViewHolder> {

    private List<Venta> ventasList;
    private OnSaleClickListener listener;

    public interface OnSaleClickListener {
        void onSaleClick(Venta venta);
    }

    public SaleAdapter(OnSaleClickListener listener) {
        this.ventasList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public SaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_venta, parent, false);
        return new SaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleViewHolder holder, int position) {
        Venta venta = ventasList.get(position);
        holder.bind(venta, listener);
    }

    @Override
    public int getItemCount() {
        return ventasList.size();
    }

    public void setVentas(List<Venta> ventas) {
        this.ventasList = ventas != null ? ventas : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class SaleViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSaleDate;
        private final TextView tvCustomerType;
        private final TextView tvSaleTotal;
        private final TextView tvPromoBadge;
        private final TextView tvItemsCount;

        public SaleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSaleDate = itemView.findViewById(R.id.tv_sale_date);
            tvCustomerType = itemView.findViewById(R.id.tv_customer_type);
            tvSaleTotal = itemView.findViewById(R.id.tv_sale_total);
            tvPromoBadge = itemView.findViewById(R.id.tv_promo_badge);
            tvItemsCount = itemView.findViewById(R.id.tv_items_count);
        }

        public void bind(Venta venta, OnSaleClickListener listener) {
            // Formatear fecha
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("es", "AR"));
            String formattedDate = dateFormat.format(new Date(venta.getFecha()));
            tvSaleDate.setText(formattedDate);

            // Tipo de cliente
            tvCustomerType.setText("Cliente: " + venta.getTipoCliente());

            // Total formateado
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
            formatter.setMaximumFractionDigits(2);
            String totalFormatted = formatter.format(venta.getTotal());
            tvSaleTotal.setText(totalFormatted);

            // Badge de promoción
            if (venta.isAplicoPromo()) {
                tvPromoBadge.setVisibility(View.VISIBLE);
            } else {
                tvPromoBadge.setVisibility(View.GONE);
            }

            // Items count - Por ahora mostrar "N/A" ya que no tenemos el dato aquí
            // En una mejora futura se podría contar desde VentaDetalle
            tvItemsCount.setText("Ver detalles");

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSaleClick(venta);
                }
            });
        }
    }
}
