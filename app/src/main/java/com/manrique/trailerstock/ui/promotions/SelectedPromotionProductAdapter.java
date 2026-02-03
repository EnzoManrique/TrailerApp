package com.manrique.trailerstock.ui.promotions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manrique.trailerstock.R;
import com.manrique.trailerstock.model.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectedPromotionProductAdapter
        extends RecyclerView.Adapter<SelectedPromotionProductAdapter.SelectedProductViewHolder> {

    private List<ProductWithQuantity> selectedProducts = new ArrayList<>();
    private OnProductRemoveListener listener;

    public interface OnProductRemoveListener {
        void onRemove(int productId);
    }

    public SelectedPromotionProductAdapter(OnProductRemoveListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SelectedProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_promotion_product, parent, false);
        return new SelectedProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedProductViewHolder holder, int position) {
        ProductWithQuantity item = selectedProducts.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return selectedProducts.size();
    }

    public void setSelectedProducts(List<Producto> productos, Map<Integer, Integer> quantities) {
        selectedProducts.clear();
        for (Producto producto : productos) {
            int quantity = quantities.getOrDefault(producto.getId(), 1);
            selectedProducts.add(new ProductWithQuantity(producto, quantity));
        }
        notifyDataSetChanged();
    }

    static class SelectedProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProductName;
        private final TextView tvRequiredQuantity;
        private final ImageButton btnRemove;

        public SelectedProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvRequiredQuantity = itemView.findViewById(R.id.tv_required_quantity);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }

        public void bind(ProductWithQuantity item, OnProductRemoveListener listener) {
            tvProductName.setText(item.producto.getNombre());
            tvRequiredQuantity.setText("Cantidad: " + item.quantity);

            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemove(item.producto.getId());
                }
            });
        }
    }

    static class ProductWithQuantity {
        Producto producto;
        int quantity;

        ProductWithQuantity(Producto producto, int quantity) {
            this.producto = producto;
            this.quantity = quantity;
        }
    }
}
