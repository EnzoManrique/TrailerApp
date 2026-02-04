package com.manrique.trailerstock.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.manrique.trailerstock.model.ProductoVendido;
import com.manrique.trailerstock.model.VentaDetalle;
import com.manrique.trailerstock.model.VentaDetalleConProducto;
import java.util.List;

@Dao
public interface VentaDetalleDao {

    @Insert
    void insertar(VentaDetalle detalle);

    @Insert
    void insertarMultiples(List<VentaDetalle> detalles);

    @Query("SELECT * FROM venta_detalles WHERE ventaId = :ventaId")
    List<VentaDetalle> obtenerDetallesPorVenta(int ventaId);

    @Query("SELECT vd.id as detalleId, vd.cantidad, vd.precioUnitario, " +
            "p.nombre as nombreProducto, c.nombre as nombreCategoria " +
            "FROM venta_detalles vd " +
            "INNER JOIN productos p ON vd.productoId = p.id " +
            "LEFT JOIN categorias c ON p.categoria_id = c.id " +
            "WHERE vd.ventaId = :ventaId " +
            "ORDER BY vd.id")
    List<VentaDetalleConProducto> obtenerDetallesConProducto(int ventaId);

    // ===== QUERIES PARA ESTADÍSTICAS =====

    @Query("SELECT p.nombre as nombreProducto, " +
            "SUM(vd.cantidad) as cantidadVendida, " +
            "SUM(vd.cantidad * vd.precioUnitario) as totalVendido " +
            "FROM venta_detalles vd " +
            "INNER JOIN productos p ON vd.productoId = p.id " +
            "INNER JOIN ventas v ON vd.ventaId = v.id " +
            "WHERE v.fecha >= :desde " +
            "GROUP BY vd.productoId, p.nombre " +
            "ORDER BY cantidadVendida DESC " +
            "LIMIT :limite")
    List<ProductoVendido> obtenerProductosMasVendidos(long desde, int limite);
}
