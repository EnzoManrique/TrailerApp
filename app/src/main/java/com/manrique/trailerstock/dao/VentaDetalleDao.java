package com.manrique.trailerstock.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.manrique.trailerstock.model.VentaDetalle;
import java.util.List;

@Dao
public interface VentaDetalleDao {

    @Insert
    void insertar(VentaDetalle detalle);

    @Insert
    void insertarMultiples(List<VentaDetalle> detalles);

    @Query("SELECT * FROM venta_detalles WHERE ventaId = :ventaId")
    List<VentaDetalle> obtenerDetallesPorVenta(int ventaId);
}