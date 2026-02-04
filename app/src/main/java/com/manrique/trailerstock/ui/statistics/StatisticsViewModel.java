package com.manrique.trailerstock.ui.statistics;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.ProductoVendido;
import com.manrique.trailerstock.model.Producto;

import java.util.Calendar;
import java.util.List;

public class StatisticsViewModel extends AndroidViewModel {

    private final AppDatabase database;

    // LiveData para métricas
    private final MutableLiveData<Double> ventasHoyLiveData;
    private final MutableLiveData<Double> ventasMesLiveData;
    private final MutableLiveData<Integer> cantidadVentasHoyLiveData;
    private final MutableLiveData<Integer> productosStockBajoLiveData;
    private final MutableLiveData<List<ProductoVendido>> topProductosLiveData;
    private final MutableLiveData<Double> ventasListaLiveData;
    private final MutableLiveData<Double> ventasMayoristaLiveData;
    private final MutableLiveData<Double> promedioVentasLiveData;

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getDatabase(application);

        ventasHoyLiveData = new MutableLiveData<>();
        ventasMesLiveData = new MutableLiveData<>();
        cantidadVentasHoyLiveData = new MutableLiveData<>();
        productosStockBajoLiveData = new MutableLiveData<>();
        topProductosLiveData = new MutableLiveData<>();
        ventasListaLiveData = new MutableLiveData<>();
        ventasMayoristaLiveData = new MutableLiveData<>();
        promedioVentasLiveData = new MutableLiveData<>();
    }

    // Getters para LiveData
    public LiveData<Double> getVentasHoy() {
        return ventasHoyLiveData;
    }

    public LiveData<Double> getVentasMes() {
        return ventasMesLiveData;
    }

    public LiveData<Integer> getCantidadVentasHoy() {
        return cantidadVentasHoyLiveData;
    }

    public LiveData<Integer> getProductosStockBajo() {
        return productosStockBajoLiveData;
    }

    public LiveData<List<ProductoVendido>> getTopProductos() {
        return topProductosLiveData;
    }

    public LiveData<Double> getVentasLista() {
        return ventasListaLiveData;
    }

    public LiveData<Double> getVentasMayorista() {
        return ventasMayoristaLiveData;
    }

    public LiveData<Double> getPromedioVentas() {
        return promedioVentasLiveData;
    }

    /**
     * Cargar todas las estadísticas en background
     */
    public void cargarEstadisticas() {
        new Thread(() -> {
            long inicioDelDia = obtenerInicioDelDia();
            long inicioDelMes = obtenerInicioDelMes();

            // Ventas del día
            Double totalHoy = database.ventaDao().obtenerTotalVentasDelDia(inicioDelDia);
            ventasHoyLiveData.postValue(totalHoy != null ? totalHoy : 0.0);

            int cantidadHoy = database.ventaDao().obtenerCantidadVentasDelDia(inicioDelDia);
            cantidadVentasHoyLiveData.postValue(cantidadHoy);

            // Ventas del mes
            Double totalMes = database.ventaDao().obtenerTotalVentasDelMes(inicioDelMes);
            ventasMesLiveData.postValue(totalMes != null ? totalMes : 0.0);

            // Productos con stock bajo
            int stockBajo = database.productoDao().contarProductosConStockBajo();
            productosStockBajoLiveData.postValue(stockBajo);

            // Top 5 productos más vendidos (del mes)
            List<ProductoVendido> topProductos = database.ventaDetalleDao()
                    .obtenerProductosMasVendidos(inicioDelMes, 5);
            topProductosLiveData.postValue(topProductos);

            // Ventas por tipo de cliente (del mes)
            Double ventasLista = database.ventaDao().obtenerVentasPorTipo("Lista", inicioDelMes);
            ventasListaLiveData.postValue(ventasLista != null ? ventasLista : 0.0);

            Double ventasMayorista = database.ventaDao().obtenerVentasPorTipo("Mayorista", inicioDelMes);
            ventasMayoristaLiveData.postValue(ventasMayorista != null ? ventasMayorista : 0.0);

            // Promedio de ventas (del mes)
            Double promedio = database.ventaDao().obtenerPromedioVentas(inicioDelMes);
            promedioVentasLiveData.postValue(promedio != null ? promedio : 0.0);

        }).start();
    }

    /**
     * Obtiene el timestamp del inicio del día actual (00:00:00)
     */
    private long obtenerInicioDelDia() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Obtiene el timestamp del inicio del mes actual (día 1 a las 00:00:00)
     */
    private long obtenerInicioDelMes() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
