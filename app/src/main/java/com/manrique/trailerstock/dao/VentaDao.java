package com.manrique.trailerstock.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.manrique.trailerstock.model.Venta;
import java.util.List;

@Dao
public interface VentaDao {

    @Insert
    long insertar(Venta venta); // Retorna el ID generado para usarlo en el detalle

    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    LiveData<List<Venta>> getAllVentas();

    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    List<Venta> obtenerTodas();

    // Consulta para saber cuanto se vendio hoy
    @Query("SELECT SUM(total) FROM ventas WHERE fecha >= :inicioDia")
    double obtenerTotalVendidoDia(long inicioDia);

    @Query("SELECT * FROM ventas WHERE id = :id")
    Venta obtenerPorId(int id);
}