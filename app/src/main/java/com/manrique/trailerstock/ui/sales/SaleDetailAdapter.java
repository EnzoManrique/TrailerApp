package com.manrique.trailerstock.ui.sales;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.model.VentaDetalleConProducto;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaleDetailAdapter extends RecyclerView.Adapter<SaleDetailAdapter.DetailViewHolder> {

    private List<VentaDetalleConProducto> detalles;

    public SaleDetailAdapter() {
        this.detalles = new ArrayList<>();
    }

    @NonNull
    @Override
    public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sale_detail_product, parent, false);
        return new DetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
        VentaDetalleConProducto detalle = detalles.get(position);
        holder.bind(detalle);
    }

    @Override
    public int getItemCount() {
        return detalles.size();
    }

    public void setDetalles(List<VentaDetalleConProducto> detalles) {
        this.detalles = detalles != null ? detalles : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class DetailViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProductName;
        private final TextView tvCategory;
        private final TextView tvUnitPrice;
        private final TextView tvQuantity;
        private final TextView tvSubtotal;

        public DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvUnitPrice = itemView.findViewById(R.id.tv_unit_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvSubtotal = itemView.findViewById(R.id.tv_subtotal);
        }

        public void bind(VentaDetalleConProducto detalle) {
            // Nombre producto
            tvProductName.setText(detalle.getNombreProducto());

            // Categoría
            String categoria = detalle.getNombreCategoria() != null ? detalle.getNombreCategoria() : "Sin categoría";
            tvCategory.setText(categoria);

            // Formato de moneda
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
            formatter.setMaximumFractionDigits(2);

            // Precio unitario
            tvUnitPrice.setText(formatter.format(detalle.getPrecioUnitario()) + " c/u");

            // Cantidad
            tvQuantity.setText("x" + detalle.getCantidad());

            // Subtotal
            tvSubtotal.setText(formatter.format(detalle.getSubtotal()));
        }
    }
}
