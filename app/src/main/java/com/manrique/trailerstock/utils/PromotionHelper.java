package com.manrique.trailerstock.utils;

import com.manrique.trailerstock.database.AppDatabase;
import com.manrique.trailerstock.model.CartItem;
import com.manrique.trailerstock.model.Promocion;
import com.manrique.trailerstock.model.PromocionProducto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class para evaluar y aplicar promociones automáticamente en ventas.
 */
public class PromotionHelper {

    /**
     * Clase interna para representar una promoción aplicable con su descuento
     * calculado
     */
    public static class PromocionAplicable {
        public Promocion promocion;
        public double descuentoEnPesos;
        public Map<Integer, Integer> productosUsados;

        public PromocionAplicable(Promocion promocion, double descuentoEnPesos, Map<Integer, Integer> productosUsados) {
            this.promocion = promocion;
            this.descuentoEnPesos = descuentoEnPesos;
            this.productosUsados = productosUsados;
        }
    }

    /**
     * Evalúa todas las promociones activas y retorna la que ofrece mayor descuento.
     *
     * @param cartItems        Lista de items en el carrito
     * @param activePromotions Lista de promociones activas
     * @param db               Instancia de la base de datos para cargar productos
     *                         asociados
     * @return La mejor promoción aplicable o null si ninguna califica
     */
    public static PromocionAplicable evaluarMejorPromocion(
            List<CartItem> cartItems,
            List<Promocion> activePromotions,
            AppDatabase db) {

        PromocionAplicable mejorPromocion = null;
        double mejorDescuento = 0;

        // Evaluar cada promoción activa
        for (Promocion promo : activePromotions) {
            // Obtener productos requeridos para esta promoción
            List<PromocionProducto> requisitos = db.promocionProductoDao()
                    .obtenerProductosPorPromocion(promo.getId());

            // Verificar si el carrito cumple los requisitos
            if (cumpleRequisitos(cartItems, requisitos)) {
                // Calcular el descuento potencial
                double descuento = calcularDescuento(cartItems, promo, requisitos);

                // Si es mejor que la actual, guardarla
                if (descuento > mejorDescuento) {
                    mejorDescuento = descuento;
                    Map<Integer, Integer> productosUsados = extraerProductosUsados(requisitos);
                    mejorPromocion = new PromocionAplicable(promo, descuento, productosUsados);
                }
            }
        }

        return mejorPromocion;
    }

    /**
     * Verifica si el carrito cumple con los requisitos de cantidad de una
     * promoción.
     *
     * @param cartItems  Items en el carrito
     * @param requisitos Requisitos de la promoción (productos y cantidades)
     * @return true si cumple todos los requisitos, false en caso contrario
     */
    private static boolean cumpleRequisitos(List<CartItem> cartItems, List<PromocionProducto> requisitos) {
        // Crear un mapa de productos en el carrito para búsqueda rápida
        Map<Integer, Integer> cartMap = new HashMap<>();
        for (CartItem item : cartItems) {
            cartMap.put(item.getProducto().getId(), item.getCantidad());
        }

        // Verificar cada requisito
        for (PromocionProducto requisito : requisitos) {
            Integer cantidadEnCarrito = cartMap.get(requisito.getProductoId());

            // Si el producto no está en el carrito o no tiene cantidad suficiente
            if (cantidadEnCarrito == null || cantidadEnCarrito < requisito.getCantidadRequerida()) {
                return false;
            }
        }

        return true; // Todos los requisitos se cumplen
    }

    /**
     * Calcula el descuento en pesos que generaría aplicar una promoción.
     *
     * @param cartItems  Items en el carrito
     * @param promo      Promoción a aplicar
     * @param requisitos Requisitos de la promoción
     * @return Monto del descuento en pesos
     */
    private static double calcularDescuento(
            List<CartItem> cartItems,
            Promocion promo,
            List<PromocionProducto> requisitos) {

        // Calcular el subtotal de los productos que califican para la promoción
        double subtotalPromocionable = 0;

        // Crear mapa de requisitos para búsqueda rápida
        Map<Integer, Integer> requisitosMap = new HashMap<>();
        for (PromocionProducto req : requisitos) {
            requisitosMap.put(req.getProductoId(), req.getCantidadRequerida());
        }

        // Sumar solo los productos que forman parte de la promoción
        for (CartItem item : cartItems) {
            if (requisitosMap.containsKey(item.getProducto().getId())) {
                // Solo aplicar descuento sobre la cantidad requerida
                int cantidadRequerida = requisitosMap.get(item.getProducto().getId());
                int cantidadParaDescuento = Math.min(item.getCantidad(), cantidadRequerida);
                subtotalPromocionable += item.getPrecioUnitario() * cantidadParaDescuento;
            }
        }

        // Calcular descuento como porcentaje del subtotal promocionable
        return subtotalPromocionable * (promo.getPorcentajeDescuento() / 100.0);
    }

    /**
     * Extrae un mapa de productos usados en la promoción.
     *
     * @param requisitos Lista de requisitos de la promoción
     * @return Mapa de producto_id -> cantidad_requerida
     */
    private static Map<Integer, Integer> extraerProductosUsados(List<PromocionProducto> requisitos) {
        Map<Integer, Integer> productosUsados = new HashMap<>();
        for (PromocionProducto req : requisitos) {
            productosUsados.put(req.getProductoId(), req.getCantidadRequerida());
        }
        return productosUsados;
    }
}
