package com.manrique.trailerstock.ui.inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manrique.trailerstock.R;
import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.Categoria;
import com.manrique.trailerstock.model.Producto;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Producto> productos = new ArrayList<>();
    private Map<Integer, String> categoriaIdToNombre = new HashMap<>();
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onEditClick(Producto producto);

        void onDeleteClick(Producto producto);
    }

    public ProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Producto producto = productos.get(position);
        holder.bind(producto, listener, categoriaIdToNombre);
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
        notifyDataSetChanged();
    }

    public void setCategorias(List<Categoria> categorias) {
        categoriaIdToNombre.clear();
        for (Categoria cat : categorias) {
            categoriaIdToNombre.put(cat.getId(), cat.getNombre());
        }
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProductName;
        private final TextView tvDescription;
        private final TextView tvPrice;
        private final TextView tvStock;
        private final TextView tvCategory;
        private final ImageView ivStockIcon;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;
        private final View stockBadge;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvDescription = itemView.findViewById(R.id.tv_product_description);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvStock = itemView.findViewById(R.id.tv_stock);
            tvCategory = itemView.findViewById(R.id.tv_category);
            ivStockIcon = itemView.findViewById(R.id.iv_stock_icon);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            stockBadge = itemView.findViewById(R.id.tv_stock).getParent() instanceof View
                    ? (View) itemView.findViewById(R.id.tv_stock).getParent()
                    : null;
        }

        public void bind(Producto producto, OnProductClickListener listener, Map<Integer, String> categoriaIdToNombre) {
            tvProductName.setText(producto.getNombre());
            tvDescription.setText(producto.getDescripcion() != null ? producto.getDescripcion() : "Sin descripción");

            // Formatear precio
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
            tvPrice.setText(formatter.format(producto.getPrecioLista()));

            tvStock.setText(String.valueOf(producto.getStockActual()));

            // Mostrar nombre de categoría
            String categoriaNombre = categoriaIdToNombre.get(producto.getCategoriaId());
            tvCategory.setText(categoriaNombre != null ? categoriaNombre : "Cat. " + producto.getCategoriaId());

            // Cambiar color del badge según stock
            int stockColor;
            if (producto.getStockActual() > 10) {
                stockColor = itemView.getContext().getColor(R.color.accent_green);
            } else if (producto.getStockActual() >= 5) {
                stockColor = itemView.getContext().getColor(R.color.accent_yellow);
            } else {
                stockColor = itemView.getContext().getColor(R.color.secondary); // Rojo/Coral
            }

            if (stockBadge != null) {
                stockBadge.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(stockColor));
            }

            // Click listeners
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(producto);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(producto);
                }
            });
        }
    }
}
