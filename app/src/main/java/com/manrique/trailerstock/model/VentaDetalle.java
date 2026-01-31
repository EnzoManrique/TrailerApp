package com.manrique.trailerstock.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "venta_detalles",
        foreignKeys = @ForeignKey(
                entity = Venta.class,
                parentColumns = "id",
                childColumns = "ventaId",
                onDelete = ForeignKey.CASCADE
        )
)
public class VentaDetalle {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int ventaId;
    private int productoId;
    private int cantidad;
    private double precioUnitario; // El precio al que se vendió ese día

    public VentaDetalle(int ventaId, int productoId, int cantidad, double precioUnitario) {
        this.ventaId = ventaId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVentaId() { return ventaId; }
    public void setVentaId(int ventaId) { this.ventaId = ventaId; }
    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
}