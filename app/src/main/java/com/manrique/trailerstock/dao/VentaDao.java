package com.manrique.trailerstock.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.manrique.trailerstock.model.Venta;
import java.util.List;

@Dao
public interface VentaDao {

        @Insert
        long insertar(Venta venta); // Retorna el ID generado para usarlo en el detalle

        @Delete
        void eliminar(Venta venta);

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

        // ===== NUEVAS QUERIES PARA MEJORAS DE ESTADÍSTICAS =====

        // Obtener ventas agrupadas por día para gráficos
        @Query("SELECT strftime('%Y-%m-%d', fecha/1000, 'unixepoch', 'localtime') as fecha, " +
                        "SUM(total) as totalVentas, " +
                        "COUNT(*) as cantidadVentas " +
                        "FROM ventas " +
                        "WHERE fecha >= :desde AND fecha <= :hasta " +
                        "GROUP BY strftime('%Y-%m-%d', fecha/1000, 'unixepoch', 'localtime') " +
                        "ORDER BY fecha ASC")
        List<com.manrique.trailerstock.model.VentasPorDia> obtenerVentasPorDia(long desde, long hasta);

        // Obtener listado de ventas de un día específico con información resumida
        @Query("SELECT v.id as ventaId, v.fecha, v.total, v.tipoCliente, v.aplicoPromo, " +
                        "COUNT(vd.id) as cantidadProductos " +
                        "FROM ventas v " +
                        "LEFT JOIN venta_detalles vd ON v.id = vd.ventaId " +
                        "WHERE v.fecha >= :inicioDia AND v.fecha < :finDia " +
                        "GROUP BY v.id " +
                        "ORDER BY v.fecha DESC")
        List<com.manrique.trailerstock.model.VentaConDetalles> obtenerVentasDelDia(long inicioDia, long finDia);

        // Obtener ventas en un rango de fechas
        @Query("SELECT * FROM ventas WHERE fecha >= :desde AND fecha <= :hasta ORDER BY fecha DESC")
        List<Venta> obtenerVentasPorRango(long desde, long hasta);

        // Calcular total de costos (necesario para margen de ganancia)
        @Query("SELECT SUM(vd.cantidad * p.precio_costo) " +
                        "FROM venta_detalles vd " +
                        "INNER JOIN productos p ON vd.productoId = p.id " +
                        "INNER JOIN ventas v ON vd.ventaId = v.id " +
                        "WHERE v.fecha >= :desde")
        Double obtenerCostoTotalVentas(long desde);
}