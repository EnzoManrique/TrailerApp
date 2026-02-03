package com.manrique.trailerstock.ui.categories;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manrique.trailerstock.R;
import com.manrique.trailerstock.model.Categoria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Categoria> categorias = new ArrayList<>();
    private Map<Integer, Integer> categoriaProductCount = new HashMap<>();
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onEditClick(Categoria categoria);

        void onDeleteClick(Categoria categoria);
    }

    public CategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categoria, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Categoria categoria = categorias.get(position);
        holder.bind(categoria, listener, categoriaProductCount);
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
        notifyDataSetChanged();
    }

    public void setProductCount(Map<Integer, Integer> productCount) {
        this.categoriaProductCount = productCount;
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategoryName;
        private final TextView tvProductCount;
        private final View viewCategoryColor;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvProductCount = itemView.findViewById(R.id.tv_product_count);
            viewCategoryColor = itemView.findViewById(R.id.view_category_color);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Categoria categoria, OnCategoryClickListener listener,
                Map<Integer, Integer> productCount) {
            tvCategoryName.setText(categoria.getNombre());

            // Mostrar contador de productos
            int count = productCount.getOrDefault(categoria.getId(), 0);
            String countText = count == 1 ? "1 producto" : count + " productos";
            tvProductCount.setText(countText);

            // Click listeners
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(categoria);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(categoria);
                }
            });
        }
    }
}
