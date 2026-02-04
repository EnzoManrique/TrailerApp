package com.manrique.trailerstock.model;

/**
 * Modelo para productos ordenados por rentabilidad.
 * Incluye información de costos y ganancias.
 */
public class ProductoRentable {
    private String nombreProducto;
    private int cantidadVendida;
    private double totalVendido;
    private double costoTotal;
    private double margenGanancia;

    public ProductoRentable(String nombreProducto, int cantidadVendida,
            double totalVendido, double costoTotal, double margenGanancia) {
        this.nombreProducto = nombreProducto;
        this.cantidadVendida = cantidadVendida;
        this.totalVendido = totalVendido;
        this.costoTotal = costoTotal;
        this.margenGanancia = margenGanancia;
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

    public double getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(double costoTotal) {
        this.costoTotal = costoTotal;
    }

    public double getMargenGanancia() {
        return margenGanancia;
    }

    public void setMargenGanancia(double margenGanancia) {
        this.margenGanancia = margenGanancia;
    }
}
