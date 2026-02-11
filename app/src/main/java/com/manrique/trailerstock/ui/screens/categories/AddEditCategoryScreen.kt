package com.manrique.trailerstock.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.manrique.trailerstock.data.local.entities.Categoria
import kotlinx.coroutines.launch

/**
 * Pantalla para agregar o editar categoría.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategoryScreen(
    categoryId: Int,
    viewModel: CategoriesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nombre by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#6200EE") }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val isEditMode = categoryId != 0
    
    // Cargar categoría si estamos en modo edición
    LaunchedEffect(categoryId) {
        if (isEditMode) {
            viewModel.getCategoryById(categoryId)?.let { categoria ->
                nombre = categoria.nombre
                selectedColor = categoria.color ?: "#6200EE"
            }
        }
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Categoría" else "Nueva Categoría") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar"
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
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Campo de nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Selector de color
                ColorSelector(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
                
                // Botón guardar
                Button(
                    onClick = {
                        scope.launch {
                            val categoria = Categoria(
                                id = categoryId,
                                nombre = nombre,
                                color = selectedColor,
                                eliminado = false
                            )
                            viewModel.saveCategory(categoria)
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = nombre.isNotBlank()
                ) {
                    Text("Guardar")
                }
            }
        }
    }
    
    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Categoría") },
            text = { Text("¿Estás seguro de que deseas eliminar esta categoría?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            viewModel.getCategoryById(categoryId)?.let { categoria ->
                                viewModel.deleteCategory(categoria)
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
}

/**
 * Selector de color con paleta predefinida.
 */
@Composable
private fun ColorSelector(
    selectedColor: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Color",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(predefinedColors) { colorHex ->
                ColorItem(
                    colorHex = colorHex,
                    isSelected = colorHex == selectedColor,
                    onClick = { onColorSelected(colorHex) }
                )
            }
        }
    }
}

/**
 * Item individual de color.
 */
@Composable
private fun ColorItem(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                color = parseColorFromHex(colorHex),
                shape = CircleShape
            )
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Seleccionado",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Parsea un color hex a Color.
 */
private fun parseColorFromHex(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        Color(android.graphics.Color.parseColor("#$cleanHex"))
    } catch (e: Exception) {
        Color(0xFF6200EE)
    }
}

/**
 * Paleta de colores predefinidos.
 */
private val predefinedColors = listOf(
    "#6200EE", // Purple
    "#3700B3", // Dark Purple
    "#03DAC6", // Teal
    "#018786", // Dark Teal
    "#E91E63", // Pink
    "#C2185B", // Dark Pink
    "#F44336", // Red
    "#D32F2F", // Dark Red
    "#FF9800", // Orange
    "#F57C00", // Dark Orange
    "#FFEB3B", // Yellow
    "#FBC02D", // Dark Yellow
    "#4CAF50", // Green
    "#388E3C", // Dark Green
    "#2196F3", // Blue
    "#1976D2", // Dark Blue
    "#9C27B0", // Deep Purple
    "#7B1FA2", // Dark Deep Purple
    "#00BCD4", // Cyan
    "#0097A7", // Dark Cyan
    "#795548", // Brown
    "#5D4037", // Dark Brown
    "#607D8B", // Blue Grey
    "#455A64"  // Dark Blue Grey
)
