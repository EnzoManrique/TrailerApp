package com.manrique.trailerstock.model;

/**
 * Modelo para representar productos vendidos con estadísticas agregadas.
 * Usado en consultas de productos más vendidos.
 */
public class ProductoVendido {
    private String nombreProducto;
    private int cantidadVendida;
    private double totalVendido;

    public ProductoVendido(String nombreProducto, int cantidadVendida, double totalVendido) {
        this.nombreProducto = nombreProducto;
        this.cantidadVendida = cantidadVendida;
        this.totalVendido = totalVendido;
    }

    // Getters y Setters
    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(int cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }

    public double getTotalVendido() {
        return totalVendido;
    }

    public void setTotalVendido(double totalVendido) {
        this.totalVendido = totalVendido;
    }
}
