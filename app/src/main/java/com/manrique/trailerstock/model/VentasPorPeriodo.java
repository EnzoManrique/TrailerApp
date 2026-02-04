package com.manrique.trailerstock.model;

/**
 * Modelo para representar ventas agrupadas por período de tiempo.
 * Usado para gráficos de tendencia de ventas.
 */
public class VentasPorPeriodo {
    private String fecha; // Formato: "2026-02-03"
    private double totalVentas;
    private int cantidadVentas;

    public VentasPorPeriodo(String fecha, double totalVentas, int cantidadVentas) {
        this.fecha = fecha;
        this.totalVentas = totalVentas;
        this.cantidadVentas = cantidadVentas;
    }

    // Getters y Setters
    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public double getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(double totalVentas) {
        this.totalVentas = totalVentas;
    }

    public int getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(int cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }
}
