package com.manrique.trailerstock.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.manrique.trailerstock.dao.CategoriaDao;
import com.manrique.trailerstock.dao.ProductoDao;
import com.manrique.trailerstock.dao.VentaDao;
import com.manrique.trailerstock.dao.VentaDetalleDao;
import com.manrique.trailerstock.model.Categoria;
import com.manrique.trailerstock.model.Producto;
import com.manrique.trailerstock.model.Promocion;
import com.manrique.trailerstock.model.PromocionProducto;
import com.manrique.trailerstock.model.Venta;
import com.manrique.trailerstock.model.VentaDetalle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        Producto.class, Categoria.class, Promocion.class,
        PromocionProducto.class, Venta.class, VentaDetalle.class
}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ProductoDao productoDao();
    public abstract CategoriaDao categoriaDao();
    public abstract VentaDao ventaDao();
    public abstract VentaDetalleDao ventaDetalleDao();

    private static volatile AppDatabase INSTANCE;

    //esto funciona para que las tareas de la base de datos no bloqueen la pantalla
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "trailer_stock_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}