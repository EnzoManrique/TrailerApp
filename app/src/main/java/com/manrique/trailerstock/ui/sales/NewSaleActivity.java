package com.manrique.trailerstock.ui.sales;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.dao.PromocionDao;
import com.manrique.trailerstock.dao.PromocionProductoDao;
import com.manrique.trailerstock.dao.ProductoDao;
import com.manrique.trailerstock.dao.VentaDao;
import com.manrique.trailerstock.dao.VentaDetalleDao;
import com.manrique.trailerstock.dao.CategoriaDao;
import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.CartItem;
import com.manrique.trailerstock.model.Categoria;
import com.manrique.trailerstock.model.Producto;
import com.manrique.trailerstock.model.Promocion;
import com.manrique.trailerstock.model.PromocionProducto;
import com.manrique.trailerstock.model.Venta;
import com.manrique.trailerstock.model.VentaDetalle;
import com.manrique.trailerstock.utils.PromotionHelper;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NewSaleActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private CartAdapter cartAdapter;
    private ChipGroup chipGroupCustomerType;
    private Chip chipLista, chipMayorista;
    private TextView tvSubtotal, tvDiscount, tvTotal, tvEmptyCart;
    private LinearLayout layoutDiscount;
    private MaterialButton btnAddProduct, btnCompleteSale;

    // Data
    private List<CartItem> cartItems = new ArrayList<>();
    private List<Producto> availableProducts = new ArrayList<>();
    private Map<Integer, String> categoryMap = new HashMap<>();
    private List<Promocion> activePromotions = new ArrayList<>();
    private String tipoCliente = "Lista";

    // Totals
    private double subtotal = 0;
    private double descuento = 0;
    private double total = 0;
    private Promocion promocionAplicada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sale);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupCustomerTypeSelector();
        setupButtons();
        loadData();
    }

    private void initializeViews() {
        rvCart = findViewById(R.id.rv_cart);
        chipGroupCustomerType = findViewById(R.id.chip_group_customer_type);
        chipLista = findViewById(R.id.chip_lista);
        chipMayorista = findViewById(R.id.chip_mayorista);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDiscount = findViewById(R.id.tv_discount);
        tvTotal = findViewById(R.id.tv_total);
        tvEmptyCart = findViewById(R.id.tv_empty_cart);
        layoutDiscount = findViewById(R.id.layout_discount);
        btnAddProduct = findViewById(R.id.btn_add_product);
        btnCompleteSale = findViewById(R.id.btn_complete_sale);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        if (!cartItems.isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Salir sin guardar")
                    .setMessage("Tienes productos en el carrito. ¿Deseas salir sin completar la venta?")
                    .setPositiveButton("Salir", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private void setupRecyclerView() {
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(new CartAdapter.OnCartItemActionListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                item.setCantidad(newQuantity);
                cartAdapter.updateItem(item);
                recalcularTotales();
            }

            @Override
            public void onRemoveItem(CartItem item) {
                cartItems.remove(item);
                cartAdapter.setCartItems(cartItems);
                updateEmptyCartState();
                recalcularTotales();
            }
        });
        rvCart.setAdapter(cartAdapter);
        updateEmptyCartState();
    }

    private void setupCustomerTypeSelector() {
        chipGroupCustomerType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_lista)) {
                tipoCliente = "Lista";
            } else if (checkedIds.contains(R.id.chip_mayorista)) {
                tipoCliente = "Mayorista";
            }
            actualizarPreciosCarrito();
        });
    }

    private void setupButtons() {
        btnAddProduct.setOnClickListener(v -> showProductSelector());
        btnCompleteSale.setOnClickListener(v -> completeSale());
    }

    private void loadData() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);

            // Cargar productos con stock > 0
            List<Producto> allProducts = db.productoDao().obtenerTodos();
            availableProducts.clear();
            for (Producto p : allProducts) {
                if (p.getStockActual() > 0) {
                    availableProducts.add(p);
                }
            }

            // Cargar categorías para mapeo
            List<Categoria> categorias = db.categoriaDao().obtenerTodas();
            categoryMap.clear();
            for (Categoria cat : categorias) {
                categoryMap.put(cat.getId(), cat.getNombre());
            }

            // Cargar promociones activas
            activePromotions = db.promocionDao().obtenerPromocionesActivas();

        }).start();
    }

    private void showProductSelector() {
        ProductSelectorDialog dialog = ProductSelectorDialog.newInstance(
                availableProducts,
                categoryMap,
                tipoCliente);
        dialog.setListener((producto, cantidad) -> {
            agregarAlCarrito(producto, cantidad);
        });
        dialog.show(getSupportFragmentManager(), "ProductSelector");
    }

    private void agregarAlCarrito(Producto producto, int cantidad) {
        // Verificar si el producto ya está en el carrito
        for (CartItem item : cartItems) {
            if (item.getProducto().getId() == producto.getId()) {
                // Actualizar cantidad
                int newQuantity = item.getCantidad() + cantidad;
                if (newQuantity <= producto.getStockActual()) {
                    item.setCantidad(newQuantity);
                    cartAdapter.setCartItems(cartItems);
                    recalcularTotales();
                    return;
                } else {
                    Toast.makeText(this, "Stock insuficiente", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        // Agregar nuevo item
        double precio = tipoCliente.equals("Mayorista") ? producto.getPrecioMayorista() : producto.getPrecioLista();
        CartItem newItem = new CartItem(producto, cantidad, precio);
        cartItems.add(newItem);
        cartAdapter.setCartItems(cartItems);
        updateEmptyCartState();
        recalcularTotales();
    }

    private void actualizarPreciosCarrito() {
        // Actualizar precios de todos los items según el tipo de cliente
        for (CartItem item : cartItems) {
            Producto p = item.getProducto();
            double nuevoPrecio = tipoCliente.equals("Mayorista") ? p.getPrecioMayorista() : p.getPrecioLista();
            item.setPrecioUnitario(nuevoPrecio);
        }
        cartAdapter.setCartItems(cartItems);
        recalcularTotales();
    }

    private void recalcularTotales() {
        // Calcular subtotal
        subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getSubtotal();
        }

        // Aplicar promoción automáticamente (evaluar la mejor)
        descuento = 0;
        promocionAplicada = null;

        if (!activePromotions.isEmpty() && !cartItems.isEmpty()) {
            // Evaluar en background para no bloquear UI
            new Thread(() -> {
                AppDatabase db = AppDatabase.getDatabase(this);
                PromotionHelper.PromocionAplicable mejorPromo = PromotionHelper.evaluarMejorPromocion(cartItems,
                        activePromotions, db);

                runOnUiThread(() -> {
                    if (mejorPromo != null) {
                        descuento = mejorPromo.descuentoEnPesos;
                        promocionAplicada = mejorPromo.promocion;
                    }

                    // Calcular total
                    total = subtotal - descuento;

                    // Actualizar UI
                    actualizarUITotales();
                });
            }).start();
        } else {
            // No hay promociones o carrito vacío - calcular directo
            total = subtotal - descuento;
            actualizarUITotales();
        }
    }

    private void actualizarUITotales() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("es", "AR"));
        formatter.setMaximumFractionDigits(2);

        tvSubtotal.setText(formatter.format(subtotal));
        tvTotal.setText(formatter.format(total));

        if (descuento > 0 && promocionAplicada != null) {
            layoutDiscount.setVisibility(View.VISIBLE);
            tvDiscount.setText("🎁 " + promocionAplicada.getNombrePromo() + ": -" + formatter.format(descuento));
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }
    }

    private void updateEmptyCartState() {
        if (cartItems.isEmpty()) {
            rvCart.setVisibility(View.GONE);
            tvEmptyCart.setVisibility(View.VISIBLE);
        } else {
            rvCart.setVisibility(View.VISIBLE);
            tvEmptyCart.setVisibility(View.GONE);
        }
    }

    private void completeSale() {
        // Validaciones
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Confirmar
        new MaterialAlertDialogBuilder(this)
                .setTitle("Finalizar Venta")
                .setMessage("¿Confirmas la venta por " + tvTotal.getText() + "?")
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    guardarVenta();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarVenta() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);

            try {
                // 1. Insertar Venta
                Integer promocionId = (promocionAplicada != null) ? promocionAplicada.getId() : null;
                Venta venta = new Venta(
                        System.currentTimeMillis(),
                        total,
                        tipoCliente,
                        promocionAplicada != null,
                        promocionId);
                long ventaId = db.ventaDao().insertar(venta);

                // 2. Insertar Detalles y Actualizar Stock
                List<VentaDetalle> detalles = new ArrayList<>();
                for (CartItem item : cartItems) {
                    // Crear detalle
                    VentaDetalle detalle = new VentaDetalle(
                            (int) ventaId,
                            item.getProducto().getId(),
                            item.getCantidad(),
                            item.getPrecioUnitario());
                    detalles.add(detalle);

                    // Actualizar stock
                    Producto producto = item.getProducto();
                    int nuevoStock = producto.getStockActual() - item.getCantidad();
                    producto.setStockActual(nuevoStock);
                    db.productoDao().actualizar(producto);
                }

                // Insertar todos los detalles
                db.ventaDetalleDao().insertarMultiples(detalles);

                // Éxito
                runOnUiThread(() -> {
                    Toast.makeText(this, "✅ Venta registrada exitosamente", Toast.LENGTH_LONG).show();
                    finish(); // Volver a SalesFragment
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "❌ Error al registrar venta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

        }).start();
    }
}
