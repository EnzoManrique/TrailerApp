package com.manrique.trailerstock.ui.statistics.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manrique.trailerstock.R;
import com.manrique.trailerstock.model.VentaConDetalles;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VentaResumidaAdapter extends RecyclerView.Adapter<VentaResumidaAdapter.ViewHolder> {

    private List<VentaConDetalles> ventas;
    private OnVentaClickListener listener;
    private NumberFormat currencyFormatter;
    private SimpleDateFormat timeFormatter;

    public interface OnVentaClickListener {
        void onVentaClick(VentaConDetalles venta);
    }

    public VentaResumidaAdapter(OnVentaClickListener listener) {
        this.ventas = new ArrayList<>();
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        this.currencyFormatter.setMaximumFractionDigits(2);
        this.timeFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    public void setVentas(List<VentaConDetalles> ventas) {
        this.ventas = ventas != null ? ventas : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_venta_resumida, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VentaConDetalles venta = ventas.get(position);

        // Formatear hora
        String time = timeFormatter.format(new Date(venta.getFecha()));
        holder.tvTime.setText(time);

        // Formatear total
        holder.tvTotal.setText(currencyFormatter.format(venta.getTotal()));

        // Tipo de cliente
        holder.tvClientType.setText(venta.getTipoCliente());

        // Cantidad de productos
        int count = venta.getCantidadProductos();
        holder.tvProductCount.setText(count + (count == 1 ? " producto" : " productos"));

        // Badge de promoción
        holder.badgePromo.setVisibility(venta.isAplicoPromo() ? View.VISIBLE : View.GONE);

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVentaClick(venta);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ventas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        TextView tvTotal;
        TextView tvClientType;
        TextView tvProductCount;
        TextView badgePromo;

        ViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_sale_time);
            tvTotal = itemView.findViewById(R.id.tv_sale_total);
            tvClientType = itemView.findViewById(R.id.tv_client_type);
            tvProductCount = itemView.findViewById(R.id.tv_product_count);
            badgePromo = itemView.findViewById(R.id.badge_promo);
        }
    }
}
