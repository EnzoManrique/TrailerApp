package com.manrique.trailerstock.model;

import androidx.room.ColumnInfo;

/**
 * POJO para mostrar detalles de venta con información del producto
 * Usado en la vista de detalle de venta
 */
public class VentaDetalleConProducto {
    @ColumnInfo(name = "detalleId")
    private int detalleId;
    @ColumnInfo(name = "cantidad")
    private int cantidad;
    @ColumnInfo(name = "precioUnitario")
    private double precioUnitario;
    @ColumnInfo(name = "nombreProducto")
    private String nombreProducto;
    @ColumnInfo(name = "nombreCategoria")
    private String nombreCategoria;

    public VentaDetalleConProducto(int detalleId, int cantidad, double precioUnitario,
            String nombreProducto, String nombreCategoria) {
        this.detalleId = detalleId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.nombreProducto = nombreProducto;
        this.nombreCategoria = nombreCategoria;
    }

    public int getDetalleId() {
        return detalleId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public double getSubtotal() {
        return cantidad * precioUnitario;
    }
}
