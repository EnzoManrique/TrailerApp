package com.manrique.trailerstock.ui.promotions;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.Producto;
import com.manrique.trailerstock.model.Promocion;
import com.manrique.trailerstock.model.PromocionProducto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEditPromotionDialog extends DialogFragment {

    private static final String ARG_PROMOCION_ID = "promocion_id";
    private static final String ARG_PROMOCION_NAME = "promocion_name";
    private static final String ARG_PROMOCION_DISCOUNT = "promocion_discount";
    private static final String ARG_PROMOCION_ACTIVE = "promocion_active";

    private TextInputEditText etPromotionName;
    private TextInputEditText etDiscountPercentage;
    private MaterialButton btnSelectProducts;
    private TextView tvSelectedProductsLabel;
    private RecyclerView rvSelectedProducts;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;

    private OnPromotionSavedListener listener;
    private Promocion promocionToEdit;
    private Map<Integer, Integer> selectedProducts = new HashMap<>();
    private List<Producto> allProducts = new ArrayList<>();
    private SelectedPromotionProductAdapter selectedProductsAdapter;

    public interface OnPromotionSavedListener {
        void onPromotionSaved(Promocion promocion);
    }

    public static AddEditPromotionDialog newInstance(@Nullable Promocion promocion) {
        AddEditPromotionDialog dialog = new AddEditPromotionDialog();
        if (promocion != null) {
            Bundle args = new Bundle();
            args.putInt(ARG_PROMOCION_ID, promocion.getId());
            args.putString(ARG_PROMOCION_NAME, promocion.getNombrePromo());
            args.putDouble(ARG_PROMOCION_DISCOUNT, promocion.getPorcentajeDescuento());
            args.putBoolean(ARG_PROMOCION_ACTIVE, promocion.isEstaActiva());
            dialog.setArguments(args);
        }
        return dialog;
    }

    public void setOnPromotionSavedListener(OnPromotionSavedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Cargar promoción a editar si existe
        if (getArguments() != null) {
            String nombre = getArguments().getString(ARG_PROMOCION_NAME);
            double descuento = getArguments().getDouble(ARG_PROMOCION_DISCOUNT);
            boolean activa = getArguments().getBoolean(ARG_PROMOCION_ACTIVE);
            promocionToEdit = new Promocion(nombre, descuento, activa);
            promocionToEdit.setId(getArguments().getInt(ARG_PROMOCION_ID));

            // Cargar productos asociados en background
            loadAssociatedProducts(promocionToEdit.getId());
        }

        android.view.ContextThemeWrapper themedContext = new android.view.ContextThemeWrapper(
                requireContext(),
                com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog);

        View view = LayoutInflater.from(themedContext).inflate(R.layout.dialog_add_edit_promotion, null);

        // Inicializar vistas
        etPromotionName = view.findViewById(R.id.et_promotion_name);
        etDiscountPercentage = view.findViewById(R.id.et_discount_percentage);
        btnSelectProducts = view.findViewById(R.id.btn_select_products);
        tvSelectedProductsLabel = view.findViewById(R.id.tv_selected_products_label);
        rvSelectedProducts = view.findViewById(R.id.rv_selected_products);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Configurar RecyclerView de productos seleccionados
        selectedProductsAdapter = new SelectedPromotionProductAdapter(this::removeProduct);
        rvSelectedProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSelectedProducts.setAdapter(selectedProductsAdapter);

        // Si es edición, llenar los campos
        if (promocionToEdit != null) {
            etPromotionName.setText(promocionToEdit.getNombrePromo());
            etDiscountPercentage.setText(String.valueOf((int) promocionToEdit.getPorcentajeDescuento()));
        }

        // Cargar productos disponibles
        loadAllProducts();

        // Listeners
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> savePromotion());
        btnSelectProducts.setOnClickListener(v -> showProductSelector());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.clay_card_background);
        }

        return dialog;
    }

    private void loadAllProducts() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            allProducts = db.productoDao().obtenerTodos();
        }).start();
    }

    private void loadAssociatedProducts(int promocionId) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            List<PromocionProducto> relations = db.promocionProductoDao().obtenerProductosPorPromocion(promocionId);

            for (PromocionProducto relation : relations) {
                selectedProducts.put(relation.getProductoId(), relation.getCantidadRequerida());
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(this::updateSelectedProductsView);
            }
        }).start();
    }

    private void showProductSelector() {
        PromotionProductSelectorDialog selectorDialog = PromotionProductSelectorDialog.newInstance(selectedProducts);
        selectorDialog.setOnProductsSelectedListener(selected -> {
            selectedProducts = selected;
            updateSelectedProductsView();
        });
        selectorDialog.show(getParentFragmentManager(), "ProductSelector");
    }

    private void removeProduct(int productId) {
        selectedProducts.remove(productId);
        updateSelectedProductsView();
    }

    private void updateSelectedProductsView() {
        if (selectedProducts.isEmpty()) {
            tvSelectedProductsLabel.setVisibility(View.GONE);
            rvSelectedProducts.setVisibility(View.GONE);
        } else {
            tvSelectedProductsLabel.setVisibility(View.VISIBLE);
            rvSelectedProducts.setVisibility(View.VISIBLE);

            // Filtrar productos seleccionados
            List<Producto> selected = new ArrayList<>();
            for (Producto producto : allProducts) {
                if (selectedProducts.containsKey(producto.getId())) {
                    selected.add(producto);
                }
            }
            selectedProductsAdapter.setSelectedProducts(selected, selectedProducts);
        }
    }

    private void savePromotion() {
        String name = etPromotionName.getText().toString().trim();
        String discountStr = etDiscountPercentage.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(name)) {
            etPromotionName.setError("Ingresa un nombre");
            return;
        }

        if (TextUtils.isEmpty(discountStr)) {
            etDiscountPercentage.setError("Ingresa el descuento");
            return;
        }

        double discount;
        try {
            discount = Double.parseDouble(discountStr);
            if (discount < 0 || discount > 100) {
                etDiscountPercentage.setError("Debe ser entre 0 y 100");
                return;
            }
        } catch (NumberFormatException e) {
            etDiscountPercentage.setError("Número inválido");
            return;
        }

        // Crear o actualizar promoción
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());

            Promocion promocion;
            if (promocionToEdit != null) {
                // Modo edición
                promocion = promocionToEdit;
                promocion.setNombrePromo(name);
                promocion.setPorcentajeDescuento(discount);
                db.promocionDao().actualizar(promocion);

                // Eliminar relaciones antiguas
                db.promocionProductoDao().eliminarProductosPorPromocion(promocion.getId());
            } else {
                // Modo creación
                promocion = new Promocion(name, discount, true);
                long promocionId = db.promocionDao().insertar(promocion);
                promocion.setId((int) promocionId);
            }

            // Insertar productos asociados
            for (Map.Entry<Integer, Integer> entry : selectedProducts.entrySet()) {
                PromocionProducto pp = new PromocionProducto(
                        promocion.getId(),
                        entry.getKey(),
                        entry.getValue());
                db.promocionProductoDao().insertar(pp);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (listener != null) {
                        listener.onPromotionSaved(promocion);
                    }
                    String message = promocionToEdit != null ? "Promoción actualizada" : "Promoción creada";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    dismiss();
                });
            }
        }).start();
    }
}
