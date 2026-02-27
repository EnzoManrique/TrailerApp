package com.manrique.trailerstock.ui.screens.promotions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manrique.trailerstock.data.local.entities.Promocion
import com.manrique.trailerstock.data.local.entities.TipoDescuento
import com.manrique.trailerstock.data.local.entities.MetodoPago
import com.manrique.trailerstock.model.ProductoEnPromocion
import kotlinx.coroutines.launch

/**
 * Pantalla para agregar o editar promoción.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPromotionScreen(
    promotionId: Int,
    viewModel: PromotionsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipoDescuento by remember { mutableStateOf(TipoDescuento.PORCENTAJE) }
    var valorDescuento by remember { mutableStateOf("") }
    var estaActiva by remember { mutableStateOf(true) }
    var fechaInicio by remember { mutableStateOf<Long?>(null) }
    var fechaFin by remember { mutableStateOf<Long?>(null) }
    var selectedMetodosPago by remember { mutableStateOf<Set<MetodoPago>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedProductos by remember { mutableStateOf<List<ProductoEnPromocion>>(emptyList()) }
    var showProductSelector by remember { mutableStateOf(false) }
    var showDatePickerInicio by remember { mutableStateOf(false) }
    var showDatePickerFin by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val isEditMode = promotionId != 0
    
    // Cargar promoción si estamos en modo edición
    LaunchedEffect(promotionId) {
        if (isEditMode) {
            viewModel.getPromotionWithProductsById(promotionId)?.let { promo ->
                nombre = promo.promocion.nombrePromo
                descripcion = promo.promocion.descripcion ?: ""
                tipoDescuento = promo.promocion.tipoDescuento
                valorDescuento = when (tipoDescuento) {
                    TipoDescuento.PORCENTAJE -> promo.promocion.porcentajeDescuento.toString()
                    TipoDescuento.MONTO_FIJO -> promo.promocion.montoDescuento.toString()
                }
                estaActiva = promo.promocion.estaActiva
                fechaInicio = promo.promocion.fechaInicio
                fechaFin = promo.promocion.fechaFin
                selectedProductos = promo.productos
                selectedMetodosPago = promo.metodosPago.toSet()
            }
        }
        isLoading = false
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la promoción") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                
                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = MaterialTheme.shapes.medium
                )
                
                // Tipo de descuento
                Text(
                    text = "Tipo de descuento",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = tipoDescuento == TipoDescuento.PORCENTAJE,
                        onClick = { tipoDescuento = TipoDescuento.PORCENTAJE },
                        label = { Text("Porcentaje (%)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = tipoDescuento == TipoDescuento.MONTO_FIJO,
                        onClick = { tipoDescuento = TipoDescuento.MONTO_FIJO },
                        label = { Text("Monto fijo ($)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Valor del descuento
                OutlinedTextField(
                    value = valorDescuento,
                    onValueChange = { valorDescuento = it },
                    label = {
                        Text(
                            when (tipoDescuento) {
                                TipoDescuento.PORCENTAJE -> "Porcentaje (%)"
                                TipoDescuento.MONTO_FIJO -> "Monto ($)"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                
                // Productos
                Text(
                    text = "Productos en esta promoción",
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (selectedProductos.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = "No hay productos seleccionados",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            selectedProductos.forEach { productoEnPromo ->
                                ListItem(
                                    headlineContent = { Text(productoEnPromo.producto.nombre) },
                                    supportingContent = {
                                        Text("Cantidad requerida: ${productoEnPromo.cantidadRequerida}")
                                    }
                                )
                            }
                        }
                    }
                }
                
                Button(
                    onClick = { showProductSelector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Seleccionar productos")
                }
                
                // Estado activa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Promoción activa",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = estaActiva,
                        onCheckedChange = { estaActiva = it }
                    )
                }
                
                // Fechas de vigencia
                Text(
                    text = "Fechas de vigencia (opcional)",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Fecha inicio
                    OutlinedButton(
                        onClick = { showDatePickerInicio = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Desde",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = fechaInicio?.let {
                                    java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                        .format(java.util.Date(it))
                                } ?: "Sin fecha",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // Fecha fin
                    OutlinedButton(
                        onClick = { showDatePickerFin = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Hasta",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = fechaFin?.let {
                                    java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                        .format(java.util.Date(it))
                                } ?: "Sin fecha",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Botones para limpiar fechas
                if (fechaInicio != null || fechaFin != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (fechaInicio != null) {
                            TextButton(
                                onClick = { fechaInicio = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Limpiar inicio")
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        
                        if (fechaFin != null) {
                            TextButton(
                                onClick = { fechaFin = null },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Limpiar fin")
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Métodos de pago
                Text(
                    text = "Métodos de pago (opcional)",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Si no seleccionas ninguno, la promoción aplicará a todos los métodos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // FilterChips para métodos de pago
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedMetodosPago.contains(MetodoPago.EFECTIVO),
                            onClick = {
                                selectedMetodosPago = if (selectedMetodosPago.contains(MetodoPago.EFECTIVO)) {
                                    selectedMetodosPago - MetodoPago.EFECTIVO
                                } else {
                                    selectedMetodosPago + MetodoPago.EFECTIVO
                                }
                            },
                            label = { Text(MetodoPago.EFECTIVO.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedMetodosPago.contains(MetodoPago.TARJETA_DEBITO),
                            onClick = {
                                selectedMetodosPago = if (selectedMetodosPago.contains(MetodoPago.TARJETA_DEBITO)) {
                                    selectedMetodosPago - MetodoPago.TARJETA_DEBITO
                                } else {
                                    selectedMetodosPago + MetodoPago.TARJETA_DEBITO
                                }
                            },
                            label = { Text(MetodoPago.TARJETA_DEBITO.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedMetodosPago.contains(MetodoPago.TARJETA_CREDITO),
                            onClick = {
                                selectedMetodosPago = if (selectedMetodosPago.contains(MetodoPago.TARJETA_CREDITO)) {
                                    selectedMetodosPago - MetodoPago.TARJETA_CREDITO
                                } else {
                                    selectedMetodosPago + MetodoPago.TARJETA_CREDITO
                                }
                            },
                            label = { Text(MetodoPago.TARJETA_CREDITO.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedMetodosPago.contains(MetodoPago.TRANSFERENCIA),
                            onClick = {
                                selectedMetodosPago = if (selectedMetodosPago.contains(MetodoPago.TRANSFERENCIA)) {
                                    selectedMetodosPago - MetodoPago.TRANSFERENCIA
                                } else {
                                    selectedMetodosPago + MetodoPago.TRANSFERENCIA
                                }
                            },
                            label = { Text(MetodoPago.TRANSFERENCIA.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Botón guardar
                Button(
                    onClick = {
                        scope.launch {
                            val promocion = Promocion(
                                id = promotionId,
                                nombrePromo = nombre,
                                descripcion = descripcion.ifBlank { null },
                                tipoDescuento = tipoDescuento,
                                porcentajeDescuento = if (tipoDescuento == TipoDescuento.PORCENTAJE)
                                    valorDescuento.toDoubleOrNull() ?: 0.0 else 0.0,
                                montoDescuento = if (tipoDescuento == TipoDescuento.MONTO_FIJO)
                                    valorDescuento.toDoubleOrNull() ?: 0.0 else 0.0,
                                estaActiva = estaActiva,
                                fechaInicio = fechaInicio,
                                fechaFin = fechaFin
                            )
                            viewModel.savePromotion(promocion, selectedProductos, selectedMetodosPago.toList())
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = nombre.isNotBlank() && valorDescuento.toDoubleOrNull() != null && selectedProductos.isNotEmpty()
                ) {
                    Text("Guardar")
                }
            }
        }
    }
    
    // Diálogo de selección de productos
    if (showProductSelector) {
        ProductSelectorDialog(
            viewModel = viewModel,
            currentProducts = selectedProductos,
            onDismiss = { showProductSelector = false },
            onConfirm = { productos ->
                selectedProductos = productos
                showProductSelector = false
            }
        )
    }
    
    // Diálogo de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Promoción") },
            text = { Text("¿Estás seguro de que deseas eliminar esta promoción?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.getPromotionWithProductsById(promotionId)?.let {
                                viewModel.deletePromotion(it.promocion)
                            }
                            showDeleteDialog = false
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // DatePicker para fecha inicio
    if (showDatePickerInicio) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaInicio ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerInicio = false },
            confirmButton = {
                TextButton(onClick = {
                    fechaInicio = datePickerState.selectedDateMillis
                    showDatePickerInicio = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerInicio = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // DatePicker para fecha fin
    if (showDatePickerFin) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaFin ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerFin = false },
            confirmButton = {
                TextButton(onClick = {
                    fechaFin = datePickerState.selectedDateMillis
                    showDatePickerFin = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerFin = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Diálogo para seleccionar productos con sus cantidades
 */
@Composable
private fun ProductSelectorDialog(
    viewModel: PromotionsViewModel,
    currentProducts: List<ProductoEnPromocion>,
    onDismiss: () -> Unit,
    onConfirm: (List<ProductoEnPromocion>) -> Unit
) {
    var selectedProducts by remember { mutableStateOf(currentProducts) }
    var allProducts by remember { mutableStateOf<List<com.manrique.trailerstock.data.local.entities.Producto>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        allProducts = viewModel.getAllProductos()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Productos") },
        text = {
            LazyColumn(
                modifier = Modifier.height(400.dp)
            ) {
                items(allProducts) { producto ->
                    val productoEnPromo = selectedProducts.find { it.producto.id == producto.id }
                    val isSelected = productoEnPromo != null
                    val cantidad = productoEnPromo?.cantidadRequerida ?: 1
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = producto.nombre,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                     ) {
                                        Text(
                                            text = "Cantidad:",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        // Botón -
                                        IconButton(
                                            onClick = {
                                                if (cantidad > 1) {
                                                    selectedProducts = selectedProducts.map {
                                                        if (it.producto.id == producto.id) {
                                                            it.copy(cantidadRequerida = cantidad - 1)
                                                        } else it
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Disminuir",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        
                                        // Cantidad
                                        Text(
                                            text = cantidad.toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.width(30.dp),
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        // Botón +
                                        IconButton(
                                            onClick = {
                                                selectedProducts = selectedProducts.map {
                                                    if (it.producto.id == producto.id) {
                                                        it.copy(cantidadRequerida = cantidad + 1)
                                                    } else it
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Aumentar",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    selectedProducts = if (checked) {
                                        selectedProducts + ProductoEnPromocion(producto, 1)
                                    } else {
                                        selectedProducts.filter { it.producto.id != producto.id }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedProducts) },
                enabled = selectedProducts.isNotEmpty()
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
