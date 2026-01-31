package com.manrique.trailerstock.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

//Esta tabla guarda la oferta (el nombre y cuánto descuento hace).
@Entity(tableName = "promociones")
public class Promocion {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String nombrePromo;
    private double porcentajeDescuento;
    private boolean estaActiva;

    public Promocion(String nombrePromo, double porcentajeDescuento, boolean estaActiva) {
        this.nombrePromo = nombrePromo;
        this.porcentajeDescuento = porcentajeDescuento;
        this.estaActiva = estaActiva;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombrePromo() {
        return nombrePromo;
    }

    public void setNombrePromo(String nombrePromo) {
        this.nombrePromo = nombrePromo;
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public boolean isEstaActiva() {
        return estaActiva;
    }

    public void setEstaActiva(boolean estaActiva) {
        this.estaActiva = estaActiva;
    }
}