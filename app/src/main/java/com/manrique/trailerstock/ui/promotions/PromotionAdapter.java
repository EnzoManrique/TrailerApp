package com.manrique.trailerstock.ui.promotions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.model.Promocion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> {

    private List<Promocion> promociones = new ArrayList<>();
    private Map<Integer, Integer> promocionProductCount = new HashMap<>();
    private OnPromotionClickListener listener;

    public interface OnPromotionClickListener {
        void onEditClick(Promocion promocion);

        void onDeleteClick(Promocion promocion);

        void onToggleActive(Promocion promocion, boolean isActive);
    }

    public PromotionAdapter(OnPromotionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promocion, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        Promocion promocion = promociones.get(position);
        holder.bind(promocion, listener, promocionProductCount);
    }

    @Override
    public int getItemCount() {
        return promociones.size();
    }

    public void setPromociones(List<Promocion> promociones) {
        this.promociones = promociones;
        notifyDataSetChanged();
    }

    public void setProductCount(Map<Integer, Integer> productCount) {
        this.promocionProductCount = productCount;
        notifyDataSetChanged();
    }

    static class PromotionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPromotionName;
        private final TextView tvDiscountBadge;
        private final TextView tvProductCount;
        private final SwitchMaterial switchActive;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        public PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPromotionName = itemView.findViewById(R.id.tv_promotion_name);
            tvDiscountBadge = itemView.findViewById(R.id.tv_discount_badge);
            tvProductCount = itemView.findViewById(R.id.tv_product_count);
            switchActive = itemView.findViewById(R.id.switch_active);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Promocion promocion, OnPromotionClickListener listener,
                Map<Integer, Integer> productCount) {
            tvPromotionName.setText(promocion.getNombrePromo());

            // Mostrar badge de descuento
            int discount = (int) promocion.getPorcentajeDescuento();
            tvDiscountBadge.setText(discount + "%");

            // Mostrar contador de productos
            int count = productCount.getOrDefault(promocion.getId(), 0);
            String countText = count == 1 ? "1 producto" : count + " productos";
            tvProductCount.setText(countText);

            // Configurar switch sin disparar listener
            switchActive.setOnCheckedChangeListener(null);
            switchActive.setChecked(promocion.isEstaActiva());

            // Configurar listener del switch
            switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggleActive(promocion, isChecked);
                }
            });

            // Click listeners para botones
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(promocion);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(promocion);
                }
            });
        }
    }
}
