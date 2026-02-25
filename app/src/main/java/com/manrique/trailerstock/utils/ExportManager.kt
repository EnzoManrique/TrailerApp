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

            // Header
            val headerRow = sheet.createRow(0)
            val headers = listOf(
                "ID", 
                context.getString(R.string.product_name), 
                context.getString(R.string.product_description), 
                context.getString(R.string.product_price_list), 
                context.getString(R.string.product_price_wholesale), 
                context.getString(R.string.product_stock_current), 
                context.getString(R.string.product_stock_minimum)
            )
            
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }

            // Data
            productos.forEachIndexed { index, producto ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(producto.id.toDouble())
                row.createCell(1).setCellValue(producto.nombre)
                row.createCell(2).setCellValue(producto.descripcion ?: "")
                row.createCell(3).setCellValue(producto.precioCosto)
                row.createCell(4).setCellValue(producto.precioLista)
                row.createCell(5).setCellValue(producto.precioMayorista)
                row.createCell(6).setCellValue(producto.stockActual.toDouble())
                row.createCell(7).setCellValue(producto.stockMinimo.toDouble())
            }

            // Set manual column widths (units: 1/256th of a character width)
            val columnWidths = intArrayOf(2000, 8000, 8000, 4000, 4000, 4000, 3000, 3000)
            columnWidths.forEachIndexed { i, width ->
                sheet.setColumnWidth(i, width)
            }

            val fileName = "Inventario_${fileDateFormat.format(Date())}.xlsx"
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
