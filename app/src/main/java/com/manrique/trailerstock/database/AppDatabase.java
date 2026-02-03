package com.manrique.trailerstock.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.manrique.trailerstock.dao.CategoriaDao;
import com.manrique.trailerstock.dao.ProductoDao;
import com.manrique.trailerstock.dao.PromocionDao;
import com.manrique.trailerstock.dao.PromocionProductoDao;
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
}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ProductoDao productoDao();

    public abstract CategoriaDao categoriaDao();

    public abstract VentaDao ventaDao();

    public abstract VentaDetalleDao ventaDetalleDao();

    public abstract PromocionDao promocionDao();

    public abstract PromocionProductoDao promocionProductoDao();

    private static volatile AppDatabase INSTANCE;

    // esto funciona para que las tareas de la base de datos no bloqueen la pantalla
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "trailer_stock_db")
                            .fallbackToDestructiveMigration() // Recrear DB si cambia schema
                            .build();

                    // Poblar categorías de ejemplo en background
                    databaseWriteExecutor.execute(() -> {
                        populateSampleData(INSTANCE);
                    });
                }
            }
        }
        return INSTANCE;
    }

    private static void populateSampleData(AppDatabase db) {
        // Solo insertar si no hay categorías
        if (db.categoriaDao().obtenerTodas().isEmpty()) {
            // Insertar categorías de ejemplo
            db.categoriaDao().insertar(new Categoria("Elásticos"));
            db.categoriaDao().insertar(new Categoria("Balancines"));
            db.categoriaDao().insertar(new Categoria("Guardabarros"));
            db.categoriaDao().insertar(new Categoria("Malacates"));
            db.categoriaDao().insertar(new Categoria("Rodamientos"));
            db.categoriaDao().insertar(new Categoria("Cadenas"));
            db.categoriaDao().insertar(new Categoria("Ganchos"));
            db.categoriaDao().insertar(new Categoria("Luces"));
        }
    }
}