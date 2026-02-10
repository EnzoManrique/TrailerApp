package com.manrique.trailerstock.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.manrique.trailerstock.model.Producto;
import java.util.List;

@Dao
public interface ProductoDao {

    @Insert
    void insertar(Producto producto);

    @Update
    void actualizar(Producto producto);

    @Query("UPDATE productos SET eliminado = 1 WHERE id = :productoId")
    void marcarComoEliminado(int productoId);

    @Query("SELECT * FROM productos WHERE eliminado = 0 ORDER BY nombre ASC")
    LiveData<List<Producto>> getAllProductos();

    @Query("SELECT * FROM productos WHERE eliminado = 0 ORDER BY nombre ASC")
    List<Producto> obtenerTodos();

    @Query("SELECT * FROM productos WHERE id = :id")
    Producto obtenerPorId(int id);

    @Query("SELECT * FROM productos WHERE eliminado = 0 AND stock_actual <= stock_minimo")
    List<Producto> obtenerStockBajo();

    @Query("SELECT COUNT(*) FROM productos WHERE eliminado = 0 AND stock_actual <= stock_minimo")
    int contarProductosConStockBajo();
}