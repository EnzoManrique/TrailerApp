package com.manrique.trailerstock.ui.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.manrique.trailerstock.data.local.entities.MetodoPago
import com.manrique.trailerstock.data.local.entities.Producto
import com.manrique.trailerstock.data.local.entities.TipoDescuento
import com.manrique.trailerstock.data.local.entities.Venta
import com.manrique.trailerstock.data.local.entities.VentaDetalle
import com.manrique.trailerstock.data.repository.ProductoRepository
import com.manrique.trailerstock.data.repository.PromocionRepository
import com.manrique.trailerstock.data.repository.VentaRepository
import com.manrique.trailerstock.model.CarritoItem
import com.manrique.trailerstock.model.PromocionConProductos
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel para pantalla de crear venta (POS)
 */
class CreateSaleViewModel(
    private val ventaRepository: VentaRepository,
    private val productoRepository: ProductoRepository,
    private val promocionRepository: PromocionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateSaleUiState())
    val uiState: StateFlow<CreateSaleUiState> = _uiState.asStateFlow()
    
    // Flow de productos disponibles para el selector
    val productosDisponibles: Flow<List<Producto>> = productoRepository.productosActivos

    init {
        loadPromociones()
    }

    private fun loadPromociones() {
        viewModelScope.launch {
            promocionRepository.getPromocionesConProductos().collect { promociones ->
                _uiState.value = _uiState.value.copy(
                    promocionesDisponibles = promociones.filter { it.promocion.estaVigente() }
                )
                // Recalcular descuentos al cargar promociones
                recalcularTotales()
            }
        }
    }

    fun agregarProducto(producto: Producto, cantidad: Int = 1) {
        val carritoActual = _uiState.value.carritoItems.toMutableList()
        
        // Verificar si ya existe en el carrito
        val itemExistente = carritoActual.find { it.producto.id == producto.id }
        
        if (itemExistente != null) {
            // Incrementar cantidad
            itemExistente.cantidad += cantidad
        } else {
            // Agregar nuevo item
            val precio = obtenerPrecioSegunTipo(producto)
            carritoActual.add(
                CarritoItem(
                    producto = producto,
                    cantidad = cantidad,
                    precioUnitario = precio
                )
            )
        }
        
        _uiState.value = _uiState.value.copy(carritoItems = carritoActual)
        recalcularTotales()
    }

    fun eliminarProducto(productoId: Int) {
        val carritoActual = _uiState.value.carritoItems.filter { it.producto.id != productoId }
        _uiState.value = _uiState.value.copy(carritoItems = carritoActual)
        recalcularTotales()
    }

    fun actualizarCantidad(productoId: Int, cantidad: Int) {
        if (cantidad <= 0) {
            eliminarProducto(productoId)
            return
        }
        
        val carritoActual = _uiState.value.carritoItems.map { item ->
            if (item.producto.id == productoId) {
                item.copy(cantidad = cantidad)
            } else {
                item
            }
        }
        
        _uiState.value = _uiState.value.copy(carritoItems = carritoActual)
        recalcularTotales()
    }

    fun cambiarTipoCliente(tipo: String) {
        _uiState.value = _uiState.value.copy(tipoCliente = tipo)
        
        // Recalcular precios de items en carrito
        val carritoActualizado = _uiState.value.carritoItems.map { item ->
            item.copy(precioUnitario = obtenerPrecioSegunTipo(item.producto))
        }
        
        _uiState.value = _uiState.value.copy(carritoItems = carritoActualizado)
        recalcularTotales()
    }

    fun cambiarMetodoPago(metodo: MetodoPago) {
        _uiState.value = _uiState.value.copy(metodoPago = metodo)
        recalcularTotales() // Recalcular porque promociones pueden depender del método
    }

    fun setNotas(notas: String) {
        _uiState.value = _uiState.value.copy(notas = notas)
    }

    fun limpiarCarrito() {
        _uiState.value = CreateSaleUiState()
        loadPromociones()
    }

    fun finalizarVenta(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val state = _uiState.value
        
        if (state.carritoItems.isEmpty()) {
            onError("El carrito está vacío")
            return
        }
        
        // Verificar stock suficiente
        for (item in state.carritoItems) {
            if (item.producto.stockActual < item.cantidad) {
                onError("Stock insuficiente para ${item.producto.nombre}")
                return
            }
        }
        
        viewModelScope.launch {
            try {
                val numeroVenta = generarNumeroVenta()
                
                val venta = Venta(
                    total = state.total,
                    tipoCliente = state.tipoCliente,
                    metodoPago = state.metodoPago,
                    numeroVenta = numeroVenta,
                    notas = state.notas.ifBlank { null }
                )
                
                val ventaId = ventaRepository.insert(venta).toInt()
                
                // Crear detalles
                val detalles = state.carritoItems.map { item ->
                    VentaDetalle.create(
                        ventaId = ventaId,
                        productoId = item.producto.id,
                        cantidad = item.cantidad,
                        precioUnitario = item.precioUnitario
                    )
                }
                
                ventaRepository.insertWithDetails(
                    venta.copy(id = ventaId),
                    detalles
                )
                
                // Descontar stock
                state.carritoItems.forEach { item ->
                    productoRepository.descontarStock(item.producto.id, item.cantidad)
                }
                
                limpiarCarrito()
                onSuccess()
                
            } catch (e: Exception) {
                onError(e.message ?: "Error al finalizar venta")
            }
        }
    }

    private fun recalcularTotales() {
        val state = _uiState.value
        
        // Aplicar promociones
        val itemsConPromociones = aplicarPromociones(
            state.carritoItems,
            state.promocionesDisponibles,
            state.metodoPago
        )
        
        val subtotal = itemsConPromociones.sumOf { it.precioUnitario * it.cantidad }
        val descuentoTotal = itemsConPromociones.sumOf { it.descuentoAplicado }
        val total = subtotal - descuentoTotal
        
        _uiState.value = state.copy(
            carritoItems = itemsConPromociones,
            subtotal = subtotal,
            descuentoTotal = descuentoTotal,
            total = total.coerceAtLeast(0.0)
        )
    }

    private fun aplicarPromociones(
        items: List<CarritoItem>,
        promociones: List<PromocionConProductos>,
        metodoPago: MetodoPago
    ): List<CarritoItem> {
        val itemsActualizados = items.toMutableList()
        
        // Resetear descuentos primero
        itemsActualizados.forEachIndexed { index, item ->
            itemsActualizados[index] = item.copy(
                descuentoAplicado = 0.0,
                promocionAplicada = null
            )
        }
        
        for (promocion in promociones) {
            // Verificar si aplica al método de pago
            if (!promocionEsAplicable(promocion, metodoPago)) continue
            
            // Verificar si el carrito cumple los requisitos
            val cumpleRequisitos = promocion.productos.all { productoRequerido ->
                val cantidadEnCarrito = items
                    .filter { it.producto.id == productoRequerido.producto.id }
                    .sumOf { it.cantidad }
                
                cantidadEnCarrito >= productoRequerido.cantidadRequerida
            }
            
            if (cumpleRequisitos) {
                // Aplicar descuento a todos los productos de la promoción
                promocion.productos.forEach { productoEnPromo ->
                    val itemIndex = itemsActualizados.indexOfFirst { 
                        it.producto.id == productoEnPromo.producto.id 
                    }
                    
                    if (itemIndex != -1) {
                        val item = itemsActualizados[itemIndex]
                        val subtotalItem = item.precioUnitario * item.cantidad
                        
                        val descuento = when (promocion.promocion.tipoDescuento) {
                            TipoDescuento.PORCENTAJE -> {
                                subtotalItem * (promocion.promocion.porcentajeDescuento / 100.0)
                            }
                            TipoDescuento.MONTO_FIJO -> {
                                promocion.promocion.montoDescuento
                            }
                        }
                        
                        itemsActualizados[itemIndex] = item.copy(
                            descuentoAplicado = descuento,
                            promocionAplicada = promocion
                        )
                    }
                }
            }
        }
        
        return itemsActualizados
    }

    private fun promocionEsAplicable(
        promocion: PromocionConProductos,
        metodoPagoActual: MetodoPago
    ): Boolean {
        // Sin restricción → aplica a todos
        if (promocion.metodosPago.isEmpty()) return true
        
        // Con restricción → verificar
        return promocion.metodosPago.contains(metodoPagoActual)
    }

    private fun obtenerPrecioSegunTipo(producto: Producto): Double {
        return when (_uiState.value.tipoCliente) {
            Venta.TIPO_MAYORISTA -> producto.precioMayorista ?: producto.precioLista
            else -> producto.precioLista
        }
    }

    private fun generarNumeroVenta(): String {
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        return "V-${dateFormat.format(Date())}"
    }
}

/**
 * Estado de UI para crear venta
 */
data class CreateSaleUiState(
    val carritoItems: List<CarritoItem> = emptyList(),
    val tipoCliente: String = Venta.TIPO_LISTA,
    val metodoPago: MetodoPago = MetodoPago.EFECTIVO,
    val promocionesDisponibles: List<PromocionConProductos> = emptyList(),
    val subtotal: Double = 0.0,
    val descuentoTotal: Double = 0.0,
    val total: Double = 0.0,
    val notas: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
