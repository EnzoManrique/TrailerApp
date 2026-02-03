package com.manrique.trailerstock.model;

/**
 * Clase auxiliar (no entity de Room) para representar un producto en el carrito
 * de compra
 */
public class CartItem {
    private Producto producto;
    private int cantidad;
    private double precioUnitario; // Precio aplicado según tipo de cliente (lista o mayorista)
    private boolean tienePromocion;
    private double descuentoPromocion;

    public CartItem(Producto producto, int cantidad, double precioUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.tienePromocion = false;
        this.descuentoPromocion = 0;
    }

    /**
     * Calcula el subtotal del item (precio * cantidad)
     */
    public double getSubtotal() {
        return precioUnitario * cantidad;
    }

    /**
     * Calcula el subtotal con descuento de promoción aplicado
     */
    public double getSubtotalConDescuento() {
        return getSubtotal() - descuentoPromocion;
    }

    // Getters y Setters
    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public boolean isTienePromocion() {
        return tienePromocion;
    }

    public void setTienePromocion(boolean tienePromocion) {
        this.tienePromocion = tienePromocion;
    }

    public double getDescuentoPromocion() {
        return descuentoPromocion;
    }

    public void setDescuentoPromocion(double descuentoPromocion) {
        this.descuentoPromocion = descuentoPromocion;
    }
}
