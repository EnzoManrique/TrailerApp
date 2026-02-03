package com.manrique.trailerstock.ui.inventory;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.Categoria;
import com.manrique.trailerstock.model.Producto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEditProductDialog extends DialogFragment {

    private static final String ARG_PRODUCT_ID = "product_id";
    private static final String ARG_PRODUCT_NAME = "product_name";
    private static final String ARG_PRODUCT_DESC = "product_desc";
    private static final String ARG_PRODUCT_PRICE_COST = "product_price_cost";
    private static final String ARG_PRODUCT_PRICE = "product_price";
    private static final String ARG_PRODUCT_PRICE_WHOLESALE = "product_price_wholesale";
    private static final String ARG_PRODUCT_STOCK = "product_stock";
    private static final String ARG_PRODUCT_STOCK_MIN = "product_stock_min";
    private static final String ARG_PRODUCT_CAT = "product_cat";

    private Producto producto;
    private OnProductSavedListener listener;

    private TextInputEditText etProductName;
    private TextInputEditText etDescription;
    private TextInputEditText etPriceCost;
    private TextInputEditText etPrice;
    private TextInputEditText etPriceWholesale;
    private TextInputEditText etStock;
    private TextInputEditText etStockMin;
    private AutoCompleteTextView spinnerCategory;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private MaterialButton btnAddCategory;
    private TextView tvDialogTitle;

    private List<Categoria> categorias;
    private Map<String, Integer> categoriaNombreToId;

    public interface OnProductSavedListener {
        void onProductSaved(Producto producto);
    }

    public static AddEditProductDialog newInstance(@Nullable Producto producto) {
        AddEditProductDialog dialog = new AddEditProductDialog();
        if (producto != null) {
            Bundle args = new Bundle();
            args.putInt(ARG_PRODUCT_ID, producto.getId());
            args.putString(ARG_PRODUCT_NAME, producto.getNombre());
            args.putString(ARG_PRODUCT_DESC, producto.getDescripcion());
            args.putDouble(ARG_PRODUCT_PRICE_COST, producto.getPrecioCosto());
            args.putDouble(ARG_PRODUCT_PRICE, producto.getPrecioLista());
            args.putDouble(ARG_PRODUCT_PRICE_WHOLESALE, producto.getPrecioMayorista());
            args.putInt(ARG_PRODUCT_STOCK, producto.getStockActual());
            args.putInt(ARG_PRODUCT_STOCK_MIN, producto.getStockMinimo());
            args.putInt(ARG_PRODUCT_CAT, producto.getCategoriaId());
            dialog.setArguments(args);
        }
        return dialog;
    }

    public void setOnProductSavedListener(OnProductSavedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Usar ContextThemeWrapper para asegurar el tema correcto
        android.view.ContextThemeWrapper themedContext = new android.view.ContextThemeWrapper(
                requireContext(),
                com.google.android.material.R.style.Theme_MaterialComponents_Light_Dialog);

        View view = LayoutInflater.from(themedContext).inflate(R.layout.dialog_add_edit_product, null);

        // Inicializar vistas
        tvDialogTitle = view.findViewById(R.id.tv_dialog_title);
        etProductName = view.findViewById(R.id.et_product_name);
        etDescription = view.findViewById(R.id.et_description);
        etPriceCost = view.findViewById(R.id.et_price_cost);
        etPrice = view.findViewById(R.id.et_price);
        etPriceWholesale = view.findViewById(R.id.et_price_wholesale);
        etStock = view.findViewById(R.id.et_stock);
        etStockMin = view.findViewById(R.id.et_stock_min);
        spinnerCategory = view.findViewById(R.id.spinner_category);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnAddCategory = view.findViewById(R.id.btn_add_category);

        // Cargar categorías desde la base de datos
        loadCategories();

        // Si es edición, cargar datos
        if (getArguments() != null) {
            tvDialogTitle.setText("Editar Producto");
            etProductName.setText(getArguments().getString(ARG_PRODUCT_NAME));
            etDescription.setText(getArguments().getString(ARG_PRODUCT_DESC));
            etPriceCost.setText(String.valueOf(getArguments().getDouble(ARG_PRODUCT_PRICE_COST, 0)));
            etPrice.setText(String.valueOf(getArguments().getDouble(ARG_PRODUCT_PRICE, 0)));
            etPriceWholesale.setText(String.valueOf(getArguments().getDouble(ARG_PRODUCT_PRICE_WHOLESALE, 0)));
            etStock.setText(String.valueOf(getArguments().getInt(ARG_PRODUCT_STOCK, 0)));
            etStockMin.setText(String.valueOf(getArguments().getInt(ARG_PRODUCT_STOCK_MIN, 5)));

            // Buscar nombre de categoría por ID
            int categoriaId = getArguments().getInt(ARG_PRODUCT_CAT);
            setCategoryById(categoriaId);
        } else {
            tvDialogTitle.setText("Agregar Producto");
            // Valores por defecto
            etStockMin.setText("5");
        }

        // Listeners
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveProduct());
        btnAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        // Hacer el diálogo con bordes redondeados
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.clay_card_background);
        }

        return dialog;
    }

    private void showAddCategoryDialog() {
        AddCategoryDialog dialog = AddCategoryDialog.newInstance();
        dialog.setOnCategoryAddedListener(categoria -> {
            // Recargar categorías
            loadCategories();
            // Seleccionar la nueva categoría
            spinnerCategory.setText(categoria.getNombre(), false);
        });
        dialog.show(getParentFragmentManager(), "AddCategoryDialog");
    }

    private void loadCategories() {
        // Cargar categorías en background thread
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(requireContext());
            categorias = db.categoriaDao().obtenerTodas();

            // Crear mapa de nombre -> ID
            categoriaNombreToId = new HashMap<>();
            List<String> nombres = new ArrayList<>();

            for (Categoria cat : categorias) {
                nombres.add(cat.getNombre());
                categoriaNombreToId.put(cat.getNombre(), cat.getId());
            }

            // Actualizar UI en main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_dropdown_item_1line, nombres);
                    spinnerCategory.setAdapter(adapter);

                    // Si es edición y ya cargamos las categorías, setear la categoría seleccionada
                    if (getArguments() != null) {
                        int categoriaId = getArguments().getInt(ARG_PRODUCT_CAT);
                        setCategoryById(categoriaId);
                    }
                });
            }
        }).start();
    }

    private void setCategoryById(int categoriaId) {
        if (categorias != null) {
            for (Categoria cat : categorias) {
                if (cat.getId() == categoriaId) {
                    spinnerCategory.setText(cat.getNombre(), false);
                    break;
                }
            }
        }
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceCostStr = etPriceCost.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String priceWholesaleStr = etPriceWholesale.getText().toString().trim();
        String stockStr = etStock.getText().toString().trim();
        String stockMinStr = etStockMin.getText().toString().trim();
        String categoryName = spinnerCategory.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(name)) {
            etProductName.setError("Ingresa un nombre");
            return;
        }

        if (TextUtils.isEmpty(priceCostStr)) {
            etPriceCost.setError("Ingresa el precio de costo");
            return;
        }

        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Ingresa el precio de lista");
            return;
        }

        if (TextUtils.isEmpty(priceWholesaleStr)) {
            etPriceWholesale.setError("Ingresa el precio mayorista");
            return;
        }

        if (TextUtils.isEmpty(stockStr)) {
            etStock.setError("Ingresa el stock");
            return;
        }

        if (TextUtils.isEmpty(stockMinStr)) {
            etStockMin.setError("Ingresa el stock mínimo");
            return;
        }

        if (TextUtils.isEmpty(categoryName)) {
            spinnerCategory.setError("Selecciona una categoría");
            return;
        }

        // Obtener ID de categoría
        Integer categoryId = categoriaNombreToId.get(categoryName);
        if (categoryId == null) {
            Toast.makeText(getContext(), "Categoría no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double priceCost = Double.parseDouble(priceCostStr);
            double price = Double.parseDouble(priceStr);
            double priceWholesale = Double.parseDouble(priceWholesaleStr);
            int stock = Integer.parseInt(stockStr);
            int stockMin = Integer.parseInt(stockMinStr);

            if (getArguments() != null) {
                // Editar producto existente
                int id = getArguments().getInt(ARG_PRODUCT_ID);
                producto = new Producto(name, description, priceCost, price, priceWholesale, stock, stockMin,
                        categoryId);
                producto.setId(id);
            } else {
                // Crear nuevo producto
                producto = new Producto(name, description, priceCost, price, priceWholesale, stock, stockMin,
                        categoryId);
            }

            if (listener != null) {
                listener.onProductSaved(producto);
            }

            Toast.makeText(getContext(), "Producto guardado", Toast.LENGTH_SHORT).show();
            dismiss();

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Los precios o stock no son válidos", Toast.LENGTH_SHORT).show();
        }
    }
}
