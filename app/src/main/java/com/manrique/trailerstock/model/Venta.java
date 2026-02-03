package com.manrique.trailerstock.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ventas")
public class Venta {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private long fecha; //
    private double total;
    private String tipoCliente; // "Lista" o "Mayorista"
    private boolean aplicoPromo;
    private Integer promocionId; // ID de la promoción aplicada (nullable)

    public Venta(long fecha, double total, String tipoCliente, boolean aplicoPromo, Integer promocionId) {
        this.fecha = fecha;
        this.total = total;
        this.tipoCliente = tipoCliente;
        this.aplicoPromo = aplicoPromo;
        this.promocionId = promocionId;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Integer getPromocionId() {
        return promocionId;
    }

    public void setPromocionId(Integer promocionId) {
        this.promocionId = promocionId;
    }
}