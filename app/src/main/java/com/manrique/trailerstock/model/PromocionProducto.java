package com.manrique.trailerstock.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

//esta tabla resuelve la relacion M.M. Un producto puede estar en varias promos, y una promo tiene varios productos.

@Entity(
        tableName = "promocion_productos",
        primaryKeys = {"promocionId", "productoId"},
        foreignKeys = {
                @ForeignKey(entity = Promocion.class,
                        parentColumns = "id",
                        childColumns = "promocionId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Producto.class,
                        parentColumns = "id",
                        childColumns = "productoId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("productoId"), @Index("promocionId")}
)

public class PromocionProducto{
    private int promocionId;
    private int productoId;
    private int cantidadRequerida;

    public PromocionProducto(int promocionId, int productoId, int cantidadRequerida) {
        this.promocionId = promocionId;
        this.productoId = productoId;
        this.cantidadRequerida = cantidadRequerida;
    }

    // Getters y Setters
    public int getPromocionId() {
        return promocionId;
    }

    public void setPromocionId(int promocionId) {
        this.promocionId = promocionId;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public int getCantidadRequerida() {
        return cantidadRequerida;
    }

    public void setCantidadRequerida(int cantidadRequerida) {
        this.cantidadRequerida = cantidadRequerida;
    }
}