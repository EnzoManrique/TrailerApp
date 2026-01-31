package com.manrique.trailerstock;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.Producto;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Obtenemos la instancia de la jefa (Base de Datos)
        AppDatabase db = AppDatabase.getDatabase(this);

        // 2. Usamos el "executor" para trabajar de fondo sin trabar la pantalla
        AppDatabase.databaseWriteExecutor.execute(() -> {

            // --- PASO A: INSERTAR ---
            // Creamos un producto de prueba (Nombre, Desc, P.Lista, P.Mayor, Stock, Min, CatId)
            Producto nuevo = new Producto("Eje 1.5 Pulgadas", "Eje reforzado para trailer",
                    15000.0, 12000.0, 10, 2, 1);

            db.productoDao().insertar(nuevo);
            Log.d("PRUEBA_DB", "¡Producto guardado con éxito!");

            // --- PASO B: LEER ---
            List<Producto> lista = db.productoDao().obtenerTodos();

            for (Producto p : lista) {
                Log.d("PRUEBA_DB", "Producto en DB: " + p.getNombre() + " - Stock: " + p.getStockActual());
            }
        });
    }
}
