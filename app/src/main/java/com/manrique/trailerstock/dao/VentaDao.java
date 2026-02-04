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

    @Query("SELECT * FROM ventas WHERE aplicoPromo = 1 ORDER BY fecha DESC")
    List<Venta> obtenerVentasConPromocion();

    // ===== QUERIES PARA ESTADÍSTICAS =====

    // Ventas del día
    @Query("SELECT SUM(total) FROM ventas WHERE fecha >= :inicioDelDia")
    Double obtenerTotalVentasDelDia(long inicioDelDia);

    @Query("SELECT COUNT(*) FROM ventas WHERE fecha >= :inicioDelDia")
    int obtenerCantidadVentasDelDia(long inicioDelDia);

    // Ventas del mes
    @Query("SELECT SUM(total) FROM ventas WHERE fecha >= :inicioDelMes")
    Double obtenerTotalVentasDelMes(long inicioDelMes);

    @Query("SELECT COUNT(*) FROM ventas WHERE fecha >= :inicioDelMes")
    int obtenerCantidadVentasDelMes(long inicioDelMes);

    // Ventas por tipo de cliente
    @Query("SELECT SUM(total) FROM ventas WHERE tipoCliente = :tipo AND fecha >= :desde")
    Double obtenerVentasPorTipo(String tipo, long desde);

    // Ventas con promociones
    @Query("SELECT COUNT(*) FROM ventas WHERE aplicoPromo = 1 AND fecha >= :desde")
    int obtenerCantidadVentasConPromocion(long desde);

    // Promedio de venta
    @Query("SELECT AVG(total) FROM ventas WHERE fecha >= :desde")
    Double obtenerPromedioVentas(long desde);
}