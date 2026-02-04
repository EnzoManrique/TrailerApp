package com.manrique.trailerstock.model;

/**
 * Modelo POJO para representar una venta con información resumida.
 * Usado en el listado de ventas diarias.
 */
public class VentaConDetalles {
    private int ventaId;
    private long fecha;
    private double total;
    private String tipoCliente;
    private boolean aplicoPromo;
    private int cantidadProductos;

    public VentaConDetalles(int ventaId, long fecha, double total, String tipoCliente,
            boolean aplicoPromo, int cantidadProductos) {
        this.ventaId = ventaId;
        this.fecha = fecha;
        this.total = total;
        this.tipoCliente = tipoCliente;
        this.aplicoPromo = aplicoPromo;
        this.cantidadProductos = cantidadProductos;
    }

    // Getters y Setters
    public int getVentaId() {
        return ventaId;
    }

    public void setVentaId(int ventaId) {
        this.ventaId = ventaId;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    public boolean isAplicoPromo() {
        return aplicoPromo;
    }

    public void setAplicoPromo(boolean aplicoPromo) {
        this.aplicoPromo = aplicoPromo;
    }

    public int getCantidadProductos() {
        return cantidadProductos;
    }

    public void setCantidadProductos(int cantidadProductos) {
        this.cantidadProductos = cantidadProductos;
    }
}
