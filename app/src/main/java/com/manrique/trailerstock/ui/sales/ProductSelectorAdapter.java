package com.manrique.trailerstock.ui.sales;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.model.Producto;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductSelectorAdapter extends RecyclerView.Adapter<ProductSelectorAdapter.ProductViewHolder> {

    private List<Producto> allProducts;
    private List<Producto> filteredProducts;
    private Map<Integer, String> categoryMap;
    private String tipoCliente; // "Lista" o "Mayorista"
    private OnProductSelectedListener listener;

    public interface OnProductSelectedListener {
        void onProductSelected(Producto producto);
    }

    public ProductSelectorAdapter(Map<Integer, String> categoryMap, String tipoCliente,
            OnProductSelectedListener listener) {
        this.allProducts = new ArrayList<>();
        this.filteredProducts = new ArrayList<>();
        this.categoryMap = categoryMap;
        this.tipoCliente = tipoCliente;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_selector, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Producto producto = filteredProducts.get(position);
        holder.bind(producto, categoryMap, tipoCliente, listener);
    }

    @Override
    public int getItemCount() {
        return filteredProducts.size();
    }

    public void setProducts(List<Producto> products) {
        this.allProducts = products != null ? products : new ArrayList<>();
        this.filteredProducts = new ArrayList<>(allProducts);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredProducts.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredProducts.addAll(allProducts);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Producto producto : allProducts) {
                if (producto.getNombre().toLowerCase().contains(lowerQuery)) {
                    filteredProducts.add(producto);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProductName;
        private final TextView tvProductCategory;
        private final TextView tvProductPrice;
        private final TextView tvStockBadge;
        private final TextView tvPromoBadge;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductCategory = itemView.findViewById(R.id.tv_product_category);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvStockBadge = itemView.findViewById(R.id.tv_stock_badge);
            tvPromoBadge = itemView.findViewById(R.id.tv_promo_badge);
        }

        public void bind(Producto producto, Map<Integer, String> categoryMap, String tipoCliente,
                OnProductSelectedListener listener) {
            // Nombre
            tvProductName.setText(producto.getNombre());

            // Categoría
            String categoryName = categoryMap.get(producto.getCategoriaId());
            tvProductCategory.setText(categoryName != null ? categoryName : "Sin categoría");

            // Precio según tipo de cliente
            double precio = tipoCliente.equals("Mayorista") ? producto.getPrecioMayorista() : producto.getPrecioLista();
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
            formatter.setMaximumFractionDigits(2);
            tvProductPrice.setText(formatter.format(precio));

            // Stock
            tvStockBadge.setText("Stock: " + producto.getStockActual());

            // TODO: Indicador de promoción (implementar en fase de promociones)
            tvPromoBadge.setVisibility(View.GONE);

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductSelected(producto);
                }
            });
        }
    }
}
