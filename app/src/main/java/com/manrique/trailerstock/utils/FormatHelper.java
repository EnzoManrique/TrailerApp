package com.manrique.trailerstock.utils;

import android.content.Context;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

/**
 * Helper estático para formatear moneda, números y fechas
 * de manera consistente en toda la aplicación.
 */
public class FormatHelper {

    /**
     * Formatea un monto como moneda según la configuración del usuario
     */
    public static String formatCurrency(Context context, double amount) {
        PreferencesManager prefs = PreferencesManager.getInstance(context);
        String currencyCode = prefs.getCurrencyCode();
        boolean useComma = prefs.isCommaDecimalFormat();

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();

        if (useComma) {
            // Formato: 1.234.567,89
            symbols.setGroupingSeparator('.');
            symbols.setDecimalSeparator(',');
        } else {
            // Formato: 1,234,567.89
            symbols.setGroupingSeparator(',');
            symbols.setDecimalSeparator('.');
        }

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00", symbols);
        String formattedAmount = decimalFormat.format(amount);

        // Agregar símbolo de moneda
        String symbol = getCurrencySymbol(currencyCode);
        return symbol + " " + formattedAmount;
    }

    /**
     * Formatea un número según la configuración del usuario
     */
    public static String formatNumber(Context context, double number) {
        PreferencesManager prefs = PreferencesManager.getInstance(context);
        boolean useComma = prefs.isCommaDecimalFormat();

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();

        if (useComma) {
            symbols.setGroupingSeparator('.');
            symbols.setDecimalSeparator(',');
        } else {
            symbols.setGroupingSeparator(',');
            symbols.setDecimalSeparator('.');
        }

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00", symbols);
        return decimalFormat.format(number);
    }

    /**
     * Formatea una fecha según la configuración del usuario
     */
    public static String formatDate(Context context, long timestamp) {
        PreferencesManager prefs = PreferencesManager.getInstance(context);
        String pattern = prefs.getDatePattern();
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }

    /**
     * Formatea una fecha con hora según la configuración del usuario
     */
    public static String formatDateTime(Context context, long timestamp) {
        PreferencesManager prefs = PreferencesManager.getInstance(context);
        String pattern = prefs.getDateTimePattern();
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }

    /**
     * Obtiene el símbolo de la moneda
     */
    public static String getCurrencySymbol(String currencyCode) {
        switch (currencyCode) {
            case "ARS":
                return "$";
            case "USD":
                return "US$";
            case "EUR":
                return "€";
            case "BRL":
                return "R$";
            case "CLP":
                return "CLP$";
            case "UYU":
                return "UYU$";
            default:
                return "$";
        }
    }

    /**
     * Obtiene el nombre completo de la moneda
     */
    public static String getCurrencyName(String currencyCode) {
        switch (currencyCode) {
            case "ARS":
                return "Peso Argentino";
            case "USD":
                return "Dólar Estadounidense";
            case "EUR":
                return "Euro";
            case "BRL":
                return "Real Brasileño";
            case "CLP":
                return "Peso Chileno";
            case "UYU":
                return "Peso Uruguayo";
            default:
                return "Desconocido";
        }
    }

    /**
     * Lista de códigos de moneda soportados
     */
    public static String[] getSupportedCurrencies() {
        return new String[] { "ARS", "USD", "EUR", "BRL", "CLP", "UYU" };
    }

    /**
     * Lista de nombres de monedas soportadas
     */
    public static String[] getSupportedCurrencyNames() {
        return new String[] {
                "Peso Argentino (ARS)",
                "Dólar Estadounidense (USD)",
                "Euro (EUR)",
                "Real Brasileño (BRL)",
                "Peso Chileno (CLP)",
                "Peso Uruguayo (UYU)"
        };
    }
}
