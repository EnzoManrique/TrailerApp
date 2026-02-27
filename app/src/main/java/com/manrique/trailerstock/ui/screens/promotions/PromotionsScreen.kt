package com.manrique.trailerstock.ui.screens.promotions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.manrique.trailerstock.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Pantalla principal de promociones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromotionsScreen(
    viewModel: PromotionsViewModel,
    onAddPromotion: () -> Unit,
    onEditPromotion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPromotion,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_promotion)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error ?: stringResource(R.string.msg_error_unknown),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.promociones.isEmpty() -> {
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    PromotionsList(
                        promociones = uiState.promociones,
                        onPromotionClick = onEditPromotion,
                        onToggleStatus = { id, activa ->
                            viewModel.togglePromotionStatus(id, activa)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PromotionsList(
    promociones: List<com.manrique.trailerstock.model.PromocionConProductos>,
    onPromotionClick: (Int) -> Unit,
    onToggleStatus: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = promociones,
            key = { it.promocion.id }
        ) { promocionConProductos ->
            PromotionListItem(
                promocionConProductos = promocionConProductos,
                onEdit = { onPromotionClick(promocionConProductos.promocion.id) },
                onToggleStatus = { onToggleStatus(promocionConProductos.promocion.id, !promocionConProductos.promocion.estaActiva) }
            )
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocalOffer,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.msg_empty_promotions),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.msg_empty_promotions_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.msg_error_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}
