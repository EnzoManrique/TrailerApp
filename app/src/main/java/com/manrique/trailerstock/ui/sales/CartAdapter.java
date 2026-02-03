package com.manrique.trailerstock.ui.sales;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.model.CartItem;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartItemActionListener listener;

    public interface OnCartItemActionListener {
        void onQuantityChanged(CartItem item, int newQuantity);

        void onRemoveItem(CartItem item);
    }

    public CartAdapter(OnCartItemActionListener listener) {
        this.cartItems = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void setCartItems(List<CartItem> items) {
        this.cartItems = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateItem(CartItem item) {
        int index = cartItems.indexOf(item);
        if (index >= 0) {
            notifyItemChanged(index);
        }
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProductName;
        private final TextView tvUnitPrice;
        private final TextView tvPromoIndicator;
        private final TextView tvQuantity;
        private final TextView tvSubtotal;
        private final MaterialButton btnDecrease;
        private final MaterialButton btnIncrease;
        private final ImageButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvUnitPrice = itemView.findViewById(R.id.tv_unit_price);
            tvPromoIndicator = itemView.findViewById(R.id.tv_promo_indicator);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvSubtotal = itemView.findViewById(R.id.tv_subtotal);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }

        public void bind(CartItem item, OnCartItemActionListener listener) {
            // Nombre del producto
            tvProductName.setText(item.getProducto().getNombre());

            // Precio unitario
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
            formatter.setMaximumFractionDigits(2);
            tvUnitPrice.setText(formatter.format(item.getPrecioUnitario()));

            // Indicador de promoción
            if (item.isTienePromocion()) {
                tvPromoIndicator.setVisibility(View.VISIBLE);
            } else {
                tvPromoIndicator.setVisibility(View.GONE);
            }

            // Cantidad
            tvQuantity.setText(String.valueOf(item.getCantidad()));

            // Subtotal
            tvSubtotal.setText(formatter.format(item.getSubtotal()));

            // Botón decrementar
            btnDecrease.setOnClickListener(v -> {
                int newQuantity = item.getCantidad() - 1;
                if (newQuantity >= 1 && listener != null) {
                    listener.onQuantityChanged(item, newQuantity);
                }
            });

            // Botón incrementar
            btnIncrease.setOnClickListener(v -> {
                int newQuantity = item.getCantidad() + 1;
                // Validar stock disponible
                if (newQuantity <= item.getProducto().getStockActual() && listener != null) {
                    listener.onQuantityChanged(item, newQuantity);
                }
            });

            // Botón eliminar
            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(item);
                }
            });
        }
    }
}
