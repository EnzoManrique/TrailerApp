package com.manrique.trailerstock.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "productos")
public class Producto {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "nombre")
    private String nombre;

    @ColumnInfo(name = "descripcion")
    private String descripcion;

    @ColumnInfo(name = "precio_costo")
    private double precioCosto;

    @ColumnInfo(name = "precio_lista")
    private double precioLista;

    @ColumnInfo(name = "precio_mayorista")
    private double precioMayorista;

    @ColumnInfo(name = "stock_actual")
    private int stockActual;

    @ColumnInfo(name = "stock_minimo")
    private int stockMinimo;

    @ColumnInfo(name = "categoria_id")
    private int categoriaId; // FK a la tabla Categorias

    @ColumnInfo(name = "eliminado")
    private boolean eliminado;

    // Constructor
    public Producto(String nombre, String descripcion, double precioCosto, double precioLista,
            double precioMayorista, int stockActual, int stockMinimo, int categoriaId) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioCosto = precioCosto;
        this.precioLista = precioLista;
        this.precioMayorista = precioMayorista;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.categoriaId = categoriaId;
        this.eliminado = false; // Por defecto no eliminado
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecioCosto() {
        return precioCosto;
    }

    public void setPrecioCosto(double precioCosto) {
        this.precioCosto = precioCosto;
    }

    public double getPrecioLista() {
        return precioLista;
    }

    public void setPrecioLista(double precioLista) {
        this.precioLista = precioLista;
    }

    public double getPrecioMayorista() {
        return precioMayorista;
    }

    public void setPrecioMayorista(double precioMayorista) {
        this.precioMayorista = precioMayorista;
    }

    public int getStockActual() {
        return stockActual;
    }

    public void setStockActual(int stockActual) {
        this.stockActual = stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }
}