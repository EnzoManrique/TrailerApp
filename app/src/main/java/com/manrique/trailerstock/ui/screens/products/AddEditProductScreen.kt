package com.manrique.trailerstock.ui.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.manrique.trailerstock.R
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.manrique.trailerstock.data.local.entities.Categoria
import com.manrique.trailerstock.data.local.entities.Producto
import kotlinx.coroutines.launch

/**
 * Pantalla para agregar o editar un producto.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: Int,
    viewModel: ProductsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Form state
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precioCosto by remember { mutableStateOf("") }
    var precioLista by remember { mutableStateOf("") }
    var precioMayorista by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var stockMinimo by remember { mutableStateOf("") }
    var selectedCategoria by remember { mutableStateOf<Categoria?>(null) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Error states
    var nombreError by remember { mutableStateOf(false) }
    var precioCostoError by remember { mutableStateOf(false) }
    var precioListaError by remember { mutableStateOf(false) }
    var precioMayoristaError by remember { mutableStateOf(false) }
    var stockError by remember { mutableStateOf(false) }
    var stockMinimoError by remember { mutableStateOf(false) }
    var categoriaError by remember { mutableStateOf(false) }
    
    val isEditing = productId != 0
    
    // Cargar producto si estamos editando
    LaunchedEffect(productId) {
        if (productId != 0) {
            viewModel.getProductById(productId)?.let { producto ->
                nombre = producto.nombre
                descripcion = producto.descripcion ?: ""
                precioCosto = producto.precioCosto.toString()
                precioLista = producto.precioLista.toString()
                precioMayorista = producto.precioMayorista.toString()
                stock = producto.stockActual.toString()
                stockMinimo = producto.stockMinimo.toString()
                selectedCategoria = uiState.categorias.find { it.id == producto.categoriaId }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) stringResource(R.string.edit_product) else stringResource(R.string.label_new_product)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.action_delete)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // Dialog de confirmación de borrado
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.action_delete)) },
                text = { Text(stringResource(R.string.msg_confirm_delete_product)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val product = viewModel.getProductById(productId)
                                if (product != null) {
                                    viewModel.deleteProduct(product)
                                    onNavigateBack()
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.action_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    nombreError = false
                },
                label = { Text("${stringResource(R.string.product_name)} *") },
                isError = nombreError,
                supportingText = if (nombreError) {
                    { Text(stringResource(R.string.msg_field_required)) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text(stringResource(R.string.product_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            // Categoría
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedCategoria?.nombre ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("${stringResource(R.string.product_category)} *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    isError = categoriaError,
                    supportingText = if (categoriaError) {
                        { Text(stringResource(R.string.msg_field_required)) }
                    } else null,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                
                ExposedDropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    uiState.categorias.forEach { categoria ->
                        DropdownMenuItem(
                            text = { Text(categoria.nombre) },
                            onClick = {
                                selectedCategoria = categoria
                                showCategoryDropdown = false
                                categoriaError = false
                            }
                        )
                    }
                }
            }
            
            // Precios
            Text(
                text = stringResource(R.string.sale_total),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            OutlinedTextField(
                value = precioCosto,
                onValueChange = {
                    precioCosto = it
                    precioCostoError = false
                },
                label = { Text("Precio Costo *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = precioCostoError,
                supportingText = if (precioCostoError) {
                    { Text("Ingresa un precio válido") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("$") }
            )
            
            OutlinedTextField(
                value = precioLista,
                onValueChange = {
                    precioLista = it
                    precioListaError = false
                },
                label = { Text("Precio Lista *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = precioListaError,
                supportingText = if (precioListaError) {
                    { Text("Ingresa un precio válido") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("$") }
            )
            
            OutlinedTextField(
                value = precioMayorista,
                onValueChange = {
                    precioMayorista = it
                    precioMayoristaError = false
                },
                label = { Text("Precio Mayorista *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = precioMayoristaError,
                supportingText = if (precioMayoristaError) {
                    { Text("Ingresa un precio válido") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("$") }
            )
            
            // Stock
            Text(
                text = "Inventario",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            OutlinedTextField(
                value = stock,
                onValueChange = {
                    stock = it
                    stockError = false
                },
                label = { Text(if (isEditing) "Stock Actual *" else "Stock Inicial *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = stockError,
                supportingText = if (stockError) {
                    { Text("Ingresa una cantidad válida") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = stockMinimo,
                onValueChange = {
                    stockMinimo = it
                    stockMinimoError = false
                },
                label = { Text("Stock Mínimo *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = stockMinimoError,
                supportingText = if (stockMinimoError) {
                    { Text("Ingresa una cantidad válida") }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Botón guardar
            Button(
                onClick = {
                    // Validación
                    var hasErrors = false
                    
                    if (nombre.isBlank()) {
                        nombreError = true
                        hasErrors = true
                    }
                    
                    if (selectedCategoria == null) {
                        categoriaError = true
                        hasErrors = true
                    }
                    
                    val costoValue = precioCosto.toDoubleOrNull()
                    if (costoValue == null || costoValue <= 0) {
                        precioCostoError = true
                        hasErrors = true
                    }
                    
                    val listaValue = precioLista.toDoubleOrNull()
                    if (listaValue == null || listaValue <= 0) {
                        precioListaError = true
                        hasErrors = true
                    }
                    
                    val mayoristaValue = precioMayorista.toDoubleOrNull()
                    if (mayoristaValue == null || mayoristaValue <= 0) {
                        precioMayoristaError = true
                        hasErrors = true
                    }
                    
                    val stockValue = stock.toIntOrNull()
                    if (stockValue == null || stockValue < 0) {
                        stockError = true
                        hasErrors = true
                    }
                    
                    val stockMinimoValue = stockMinimo.toIntOrNull()
                    if (stockMinimoValue == null || stockMinimoValue < 0) {
                        stockMinimoError = true
                        hasErrors = true
                    }
                    
                    if (!hasErrors) {
                        scope.launch {
                            val producto = Producto(
                                id = if (isEditing) productId else 0,
                                nombre = nombre.trim(),
                                descripcion = descripcion.trim().ifBlank { null },
                                precioCosto = costoValue!!,
                                precioLista = listaValue!!,
                                precioMayorista = mayoristaValue!!,
                                stockActual = stockValue!!,
                                stockMinimo = stockMinimoValue!!,
                                categoriaId = selectedCategoria!!.id
                            )
                            
                            val result = viewModel.saveProduct(producto)
                            if (result.isSuccess) {
                                onNavigateBack()
                            } else {
                                snackbarHostState.showSnackbar(
                                    message = "Error al guardar el producto",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) stringResource(R.string.action_save) else stringResource(R.string.add_product))
            }
        }
    }
}
