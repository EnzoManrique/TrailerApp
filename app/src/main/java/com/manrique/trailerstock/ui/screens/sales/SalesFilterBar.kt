package com.manrique.trailerstock.ui.screens.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manrique.trailerstock.data.local.entities.MetodoPago

/**
 * Barra de filtros para la pantalla de ventas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filterMetodoPago: MetodoPago?,
    onFilterMetodoPagoChange: (MetodoPago?) -> Unit,
    filterTipoCliente: String?,
    onFilterTipoClienteChange: (String?) -> Unit,
    hasDateFilter: Boolean,
    onDateFilterClick: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMetodoPagoMenu by remember { mutableStateOf(false) }
    var showTipoClienteMenu by remember { mutableStateOf(false) }
    
    val hasFilters = filterMetodoPago != null || 
                     filterTipoCliente != null || 
                     hasDateFilter ||
                     searchQuery.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Fila de chips de filtro
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filtro de método de pago
            item {
                Box {
                    FilterChip(
                        selected = filterMetodoPago != null,
                        onClick = { showMetodoPagoMenu = true },
                        label = {
                            Text(
                                text = when (filterMetodoPago) {
                                    MetodoPago.EFECTIVO -> "Efectivo"
                                    MetodoPago.TARJETA_DEBITO -> "Débito"
                                    MetodoPago.TARJETA_CREDITO -> "Crédito"
                                    MetodoPago.TRANSFERENCIA -> "Transf."
                                    null -> "Método"
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (filterMetodoPago != null) 
                                    Icons.Default.CheckCircle 
                                else 
                                    Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = showMetodoPagoMenu,
                        onDismissRequest = { showMetodoPagoMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos") },
                            onClick = {
                                onFilterMetodoPagoChange(null)
                                showMetodoPagoMenu = false
                            },
                            leadingIcon = {
                                if (filterMetodoPago == null) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        MetodoPago.values().forEach { metodo ->
                            DropdownMenuItem(
                                text = { Text(metodo.displayName) },
                                onClick = {
                                    onFilterMetodoPagoChange(metodo)
                                    showMetodoPagoMenu = false
                                },
                                leadingIcon = {
                                    if (filterMetodoPago == metodo) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Filtro de tipo de cliente
            item {
                Box {
                    FilterChip(
                        selected = filterTipoCliente != null,
                        onClick = { showTipoClienteMenu = true },
                        label = {
                            Text(
                                text = filterTipoCliente ?: "Cliente"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (filterTipoCliente != null) 
                                    Icons.Default.CheckCircle 
                                else 
                                    Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = showTipoClienteMenu,
                        onDismissRequest = { showTipoClienteMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos") },
                            onClick = {
                                onFilterTipoClienteChange(null)
                                showTipoClienteMenu = false
                            },
                            leadingIcon = {
                                if (filterTipoCliente == null) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lista") },
                            onClick = {
                                onFilterTipoClienteChange("LISTA")
                                showTipoClienteMenu = false
                            },
                            leadingIcon = {
                                if (filterTipoCliente == "LISTA") {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mayorista") },
                            onClick = {
                                onFilterTipoClienteChange("MAYORISTA")
                                showTipoClienteMenu = false
                            },
                            leadingIcon = {
                                if (filterTipoCliente == "MAYORISTA") {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                    }
                }
            }

            // Filtro de fecha
            item {
                FilterChip(
                    selected = hasDateFilter,
                    onClick = onDateFilterClick,
                    label = { Text("Fecha") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (hasDateFilter) 
                                Icons.Default.CheckCircle 
                            else 
                                Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            // Botón limpiar filtros (ahora como icono de refresh para ahorrar espacio)
            if (hasFilters) {
                item {
                    IconButton(
                        onClick = onClearFilters,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Limpiar filtros",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
