package com.manrique.trailerstock.ui.statistics.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manrique.trailerstock.R;
import com.manrique.trailerstock.model.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LowStockProductAdapter extends RecyclerView.Adapter<LowStockProductAdapter.ViewHolder> {

    private List<Producto> productos;
    private Map<Integer, String> categoryMap;

    public LowStockProductAdapter(Map<Integer, String> categoryMap) {
        this.productos = new ArrayList<>();
        this.categoryMap = categoryMap;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos != null ? productos : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_low_stock_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Producto producto = productos.get(position);

        // Nombre del producto
        holder.tvProductName.setText(producto.getNombre());

        // Categoría
        String categoryName = categoryMap.get(producto.getCategoriaId());
        holder.tvCategory.setText(categoryName != null ? categoryName : "Sin categoría");

        // Stock actual
        holder.tvCurrentStock.setText(String.valueOf(producto.getStockActual()));

        // Stock mínimo
        holder.tvMinStock.setText("Min: " + producto.getStockMinimo());
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName;
        TextView tvCategory;
        TextView tvCurrentStock;
        TextView tvMinStock;

        ViewHolder(View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvCurrentStock = itemView.findViewById(R.id.tv_current_stock);
            tvMinStock = itemView.findViewById(R.id.tv_min_stock);
        }
    }
}
