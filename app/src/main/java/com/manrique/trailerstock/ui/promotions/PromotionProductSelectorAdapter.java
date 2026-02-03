package com.manrique.trailerstock.ui.promotions;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.model.Producto;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PromotionProductSelectorAdapter
        extends RecyclerView.Adapter<PromotionProductSelectorAdapter.ProductViewHolder> {

    private List<Producto> productos = new ArrayList<>();
    private List<Producto> productosFiltered = new ArrayList<>();
    private Map<Integer, Integer> selectedProducts = new HashMap<>(); // productId -> requiredQuantity

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promotion_product_selector, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Producto producto = productosFiltered.get(position);
        holder.bind(producto, selectedProducts);
    }

    @Override
    public int getItemCount() {
        return productosFiltered.size();
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
        this.productosFiltered = new ArrayList<>(productos);
        notifyDataSetChanged();
    }

    public void setSelectedProducts(Map<Integer, Integer> selectedProducts) {
        this.selectedProducts = selectedProducts;
        notifyDataSetChanged();
    }

    public Map<Integer, Integer> getSelectedProducts() {
        return selectedProducts;
    }

    public void filter(String query) {
        productosFiltered.clear();
        if (query == null || query.isEmpty()) {
            productosFiltered.addAll(productos);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Producto producto : productos) {
                if (producto.getNombre().toLowerCase().contains(lowerCaseQuery)) {
                    productosFiltered.add(producto);
                }
            }
        }
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCheckBox checkboxProduct;
        private final TextView tvProductName;
        private final TextView tvProductPrice;
        private final TextInputEditText etRequiredQuantity;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxProduct = itemView.findViewById(R.id.checkbox_product);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            etRequiredQuantity = itemView.findViewById(R.id.et_required_quantity);
        }

        public void bind(Producto producto, Map<Integer, Integer> selectedProducts) {
            tvProductName.setText(producto.getNombre());

            // Formatear precio
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
            tvProductPrice.setText(currencyFormat.format(producto.getPrecioLista()));

            // Configurar checkbox
            boolean isSelected = selectedProducts.containsKey(producto.getId());
            checkboxProduct.setChecked(isSelected);

            // Configurar cantidad
            int quantity = selectedProducts.getOrDefault(producto.getId(), 1);
            etRequiredQuantity.setText(String.valueOf(quantity));
            etRequiredQuantity.setEnabled(isSelected);

            // Listener del checkbox
            checkboxProduct.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    int qty = 1;
                    try {
                        String qtyText = etRequiredQuantity.getText().toString();
                        if (!qtyText.isEmpty()) {
                            qty = Integer.parseInt(qtyText);
                        }
                    } catch (NumberFormatException e) {
                        qty = 1;
                    }
                    selectedProducts.put(producto.getId(), qty);
                    etRequiredQuantity.setEnabled(true);
                } else {
                    selectedProducts.remove(producto.getId());
                    etRequiredQuantity.setEnabled(false);
                }
            });

            // Listener del campo de cantidad
            etRequiredQuantity.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (checkboxProduct.isChecked() && s.length() > 0) {
                        try {
                            int qty = Integer.parseInt(s.toString());
                            if (qty > 0) {
                                selectedProducts.put(producto.getId(), qty);
                            }
                        } catch (NumberFormatException e) {
                            // Ignore invalid input
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }
}
