package com.manrique.trailerstock.utils

import android.content.Context
import android.net.Uri
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.manrique.trailerstock.R
import com.manrique.trailerstock.data.local.entities.Producto
import com.manrique.trailerstock.data.local.entities.Venta
import com.manrique.trailerstock.model.VentaConDetalles
import com.manrique.trailerstock.model.VentaDetalleConProducto
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ExportManager(val context: Context) {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())

    /**
     * Genera un PDF con el reporte de ventas.
     */
    fun generateSalesReportPdf(ventas: List<VentaConDetalles>, title: String): File? {
        try {
            val fileName = "Reporte_Ventas_${fileDateFormat.format(Date())}.pdf"
            val file = File(context.cacheDir, fileName)
            val document = Document(PageSize.A4)
            PdfWriter.getInstance(document, FileOutputStream(file))

            document.open()

            // Header
            val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
            val header = Paragraph(title, titleFont)
            header.alignment = Element.ALIGN_CENTER
            header.spacingAfter = 20f
            document.add(header)

            // Info de generación
            val infoFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)
            val info = Paragraph(
                context.getString(R.string.report_generated_at, dateFormat.format(Date())) + "\n" +
                context.getString(R.string.report_total_sales_count, ventas.size), 
                infoFont
            )
            info.spacingAfter = 20f
            document.add(info)

            // Tabla de Ventas
            val table = PdfPTable(4)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(2f, 4f, 2f, 2f))

            // Headers de tabla
            val headFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)
            val headers = listOf(
                context.getString(R.string.report_col_date),
                context.getString(R.string.report_col_detail),
                context.getString(R.string.report_col_payment),
                context.getString(R.string.report_col_total)
            )
            headers.forEach { h ->
                val cell = PdfPCell(Phrase(h, headFont))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                cell.setPadding(5f)
                cell.backgroundColor = BaseColor.LIGHT_GRAY
                table.addCell(cell)
            }

            var granTotal = 0.0

            ventas.forEach { vp ->
                val dateStr = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(vp.venta.fecha))
                table.addCell(createCell(dateStr))
                
                val detalles = vp.detalles.joinToString("\n") { "${it.detalle.cantidad}x ${it.producto.nombre}" }
                table.addCell(createCell(detalles))
                
                table.addCell(createCell(vp.venta.metodoPago.name))
                table.addCell(createCell(currencyFormat.format(vp.venta.total)))
                
                granTotal += vp.venta.total
            }

            document.add(table)

            // Total General
            val totalPara = Paragraph(
                "\n" + context.getString(R.string.report_grand_total, currencyFormat.format(granTotal)), 
                titleFont
            )
            totalPara.alignment = Element.ALIGN_RIGHT
            document.add(totalPara)

            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Genera un Excel con el inventario actual.
     */
    fun generateInventoryExcel(productos: List<Producto>): File? {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Inventario")

            // --- ESTILOS ---
            // Estilo para el título principal
            val titleStyle = workbook.createCellStyle().apply {
                val font = workbook.createFont().apply {
                    bold = true
                    fontHeightInPoints = 16.toShort()
                    color = org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.index
                }
                setFont(font)
            }

            // Estilo para el encabezado de la tabla (Azul Acero de la app)
            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = org.apache.poi.ss.usermodel.IndexedColors.GREY_50_PERCENT.index // Usando un gris similar al Primary (4A5568)
                fillPattern = org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND
                val font = workbook.createFont().apply {
                    color = org.apache.poi.ss.usermodel.IndexedColors.WHITE.index
                    bold = true
                }
                setFont(font)
                alignment = org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER
                verticalAlignment = org.apache.poi.ss.usermodel.VerticalAlignment.CENTER
            }

            // Estilo de moneda
            val moneyStyle = workbook.createCellStyle().apply {
                val format = workbook.createDataFormat()
                dataFormat = format.getFormat("$ #,##0.00")
            }

            // Estilo para Stock Bajo (Alerta Naranja/Rojo)
            val warningStyle = workbook.createCellStyle().apply {
                fillForegroundColor = org.apache.poi.ss.usermodel.IndexedColors.ORANGE.index
                fillPattern = org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND
            }

            // --- CABECERA DE METADATOS (Marketing & Info) ---
            val rowTitle = sheet.createRow(0)
            rowTitle.createCell(0).apply {
                setCellValue("Reporte de Inventario - TTM")
                setCellStyle(titleStyle)
            }
            
            val rowMeta = sheet.createRow(1)
            rowMeta.createCell(0).setCellValue("Generado el ${dateFormat.format(Date())}")
            
            val rowAuthor = sheet.createRow(2)
            rowAuthor.createCell(0).setCellValue("Sistema desarrollado por Enzo Manrique")
            
            // --- ENCABEZADOS DE TABLA ---
            val tableHeaderStart = 4
            val headerRow = sheet.createRow(tableHeaderStart)
            val headers = listOf(
                "ID", 
                context.getString(R.string.product_name), 
                context.getString(R.string.product_description), 
                context.getString(R.string.product_price_cost),
                context.getString(R.string.product_price_list), 
                context.getString(R.string.product_stock_current), 
                context.getString(R.string.product_stock_minimum),
                "Valor Inventario ($)" // Nueva columna
            )
            
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.setCellStyle(headerStyle)
            }

            // --- DATOS ---
            var capitalTotal = 0.0
            
            productos.forEachIndexed { index, producto ->
                val row = sheet.createRow(tableHeaderStart + index + 1)
                
                // ID e Info básica
                row.createCell(0).setCellValue(producto.id.toDouble())
                row.createCell(1).setCellValue(producto.nombre)
                row.createCell(2).setCellValue(producto.descripcion ?: "")
                
                // Precios con formato moneda
                row.createCell(3).apply {
                    setCellValue(producto.precioCosto)
                    setCellStyle(moneyStyle)
                }
                row.createCell(4).apply {
                    setCellValue(producto.precioLista)
                    setCellStyle(moneyStyle)
                }
                
                // Stocks
                val stockCell = row.createCell(5).apply {
                    setCellValue(producto.stockActual.toDouble())
                }
                row.createCell(6).setCellValue(producto.stockMinimo.toDouble())
                
                // ALERTA DE STOCK BAJO
                if (producto.stockActual <= producto.stockMinimo) {
                    stockCell.setCellStyle(warningStyle)
                }
                
                // VALOR DE INVENTARIO (Lógica de negocio)
                val valorItem = producto.precioCosto * producto.stockActual
                row.createCell(7).apply {
                    setCellValue(valorItem)
                    setCellStyle(moneyStyle)
                }
                
                capitalTotal += valorItem
            }

            // --- FILA DE TOTALES ---
            val totalRowIndex = tableHeaderStart + productos.size + 2
            val totalRow = sheet.createRow(totalRowIndex)
            totalRow.createCell(6).apply {
                setCellValue("CAPITAL TOTAL:")
                val boldFont = workbook.createFont().apply { bold = true }
                val boldStyle = workbook.createCellStyle().apply { setFont(boldFont) }
                setCellStyle(boldStyle)
            }
            totalRow.createCell(7).apply {
                setCellValue(capitalTotal)
                val totalMoneyStyle = workbook.createCellStyle().apply {
                    cloneStyleFrom(moneyStyle)
                    val font = workbook.createFont().apply { bold = true; color = org.apache.poi.ss.usermodel.IndexedColors.RED.index }
                    setFont(font)
                }
                setCellStyle(totalMoneyStyle)
            }

            // --- FINALIZACIÓN (Ajustes de visualización) ---
            // Filtros automáticos
            sheet.setAutoFilter(org.apache.poi.ss.util.CellRangeAddress(tableHeaderStart, tableHeaderStart, 0, 7))
            
            // Ajuste de anchos manual (Reemplaza autoSizeColumn que falla en Android)
            // Unidades: 1/256 de un carácter
            sheet.setColumnWidth(0, 3000)  // ID
            sheet.setColumnWidth(1, 8000)  // Nombre
            sheet.setColumnWidth(2, 10000) // Descripción
            sheet.setColumnWidth(3, 4500)  // Precio Costo
            sheet.setColumnWidth(4, 4500)  // Precio Lista
            sheet.setColumnWidth(5, 4000)  // Stock Actual
            sheet.setColumnWidth(6, 4000)  // Stock Mínimo
            sheet.setColumnWidth(7, 5000)  // Valor Inventario

            val fileName = "Inventario_TTM_${fileDateFormat.format(Date())}.xlsx"
            val file = File(context.cacheDir, fileName)
            val fos = FileOutputStream(file)
            workbook.write(fos)
            fos.close()
            workbook.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun createCell(text: String): PdfPCell {
        val cell = PdfPCell(Phrase(text, Font(Font.FontFamily.HELVETICA, 10f)))
        cell.setPadding(5f)
        return cell
    }
}
