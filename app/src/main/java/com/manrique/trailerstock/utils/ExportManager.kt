package com.manrique.trailerstock.utils

import android.content.Context
import com.itextpdf.text.*
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.manrique.trailerstock.R
import com.manrique.trailerstock.data.local.entities.Producto
import com.manrique.trailerstock.data.local.entities.Venta
import com.manrique.trailerstock.data.local.entities.MetodoPago
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
            val document = Document(PageSize.A4, 36f, 36f, 72f, 72f)
            val writer = PdfWriter.getInstance(document, FileOutputStream(file))
            
            writer.pageEvent = object : com.itextpdf.text.pdf.PdfPageEventHelper() {
                override fun onEndPage(writer: PdfWriter, document: Document) {
                    val cb = writer.directContent
                    val footerFont = Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL, BaseColor.GRAY)
                    
                    val footerLeft = Phrase("Desarrollado por Enzo Manrique - TTM App", footerFont)
                    ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, footerLeft, document.left(), document.bottom() - 20, 0f)
                    
                    val footerRight = Phrase("Página ${writer.pageNumber}", footerFont)
                    ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, footerRight, document.right(), document.bottom() - 20, 0f)
                    
                    val waterFont = Font(Font.FontFamily.HELVETICA, 40f, Font.BOLD, BaseColor(200, 200, 200, 40))
                    val waterMark = Phrase("TTM GESTIÓN", waterFont)
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, waterMark, (document.right() + document.left()) / 2, (document.top() + document.bottom()) / 2, 45f)
                }
            }

            document.open()

            val primaryColor = BaseColor(74, 85, 104)
            val titleFont = Font(Font.FontFamily.HELVETICA, 20f, Font.BOLD, primaryColor)
            val subTitleFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor.DARK_GRAY)
            val normalFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)
            val boldFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
            val whiteFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, BaseColor.WHITE)

            val mainHeader = PdfPTable(2)
            mainHeader.widthPercentage = 100f
            mainHeader.setWidths(floatArrayOf(5f, 5f))
            
            val brandCell = PdfPCell(Phrase("TTM", Font(Font.FontFamily.HELVETICA, 24f, Font.BOLD, primaryColor)))
            brandCell.border = Rectangle.NO_BORDER
            brandCell.verticalAlignment = Element.ALIGN_MIDDLE
            mainHeader.addCell(brandCell)
            
            val titleCell = PdfPCell(Phrase(title, titleFont))
            titleCell.border = Rectangle.NO_BORDER
            titleCell.horizontalAlignment = Element.ALIGN_RIGHT
            titleCell.verticalAlignment = Element.ALIGN_MIDDLE
            mainHeader.addCell(titleCell)
            
            document.add(mainHeader)
            document.add(Paragraph(" ").apply { setSpacingAfter(10f) })

            val resumenTable = PdfPTable(1)
            resumenTable.widthPercentage = 100f
            val resumenCell = PdfPCell()
            resumenCell.setPadding(15f)
            resumenCell.backgroundColor = BaseColor(248, 250, 252)
            resumenCell.border = Rectangle.BOX
            resumenCell.setBorderColor(BaseColor.LIGHT_GRAY)
            
            var totalFacturado = 0.0
            val metodosPagoMap = mutableMapOf<String, Int>()
            val productosVenta = mutableMapOf<String, Int>()
            
            ventas.forEach { vp ->
                totalFacturado += vp.venta.total
                val metodoName = vp.venta.metodoPago.displayName
                metodosPagoMap[metodoName] = (metodosPagoMap[metodoName] ?: 0) + 1
                vp.detalles.forEach { dp ->
                    productosVenta[dp.producto.nombre] = (productosVenta[dp.producto.nombre] ?: 0) + dp.detalle.cantidad
                }
            }
            
            val productoEstrella = productosVenta.maxByOrNull { it.value }?.let { "${it.key} (${it.value} uds.)" } ?: "---"
            val totalVentas = ventas.size
            val metodoPredominante = metodosPagoMap.keys.joinToString(" / ") { key ->
                val count = metodosPagoMap[key] ?: 0
                val perc = if (totalVentas > 0) (count * 100 / totalVentas) else 0
                "$key: $perc%"
            }

            resumenCell.addElement(Paragraph("RESUMEN DE OPERACIONES", subTitleFont))
            resumenCell.addElement(Paragraph("Total Facturado: ${currencyFormat.format(totalFacturado)}", Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, primaryColor)))
            resumenCell.addElement(Paragraph("Producto más vendido: $productoEstrella", normalFont))
            resumenCell.addElement(Paragraph("Métodos de Pago: $metodoPredominante", normalFont))
            resumenCell.addElement(Paragraph("Generado: ${dateFormat.format(Date())}", Font(Font.FontFamily.HELVETICA, 8f, Font.ITALIC, BaseColor.GRAY)))
            
            resumenTable.addCell(resumenCell)
            document.add(resumenTable)
            document.add(Paragraph(" "))

            val table = PdfPTable(4)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(2f, 4f, 2f, 2f))

            val headers = listOf(
                context.getString(R.string.report_col_date),
                context.getString(R.string.report_col_detail),
                context.getString(R.string.report_col_payment),
                context.getString(R.string.report_col_total)
            )
            headers.forEach { h ->
                val cell = PdfPCell(Phrase(h, whiteFont))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                cell.setPadding(8f)
                cell.backgroundColor = primaryColor
                cell.border = Rectangle.NO_BORDER
                table.addCell(cell)
            }

            ventas.forEachIndexed { index, vp ->
                val isDark = index % 2 == 0
                val rowColor = if (isDark) BaseColor(240, 240, 240) else BaseColor.WHITE
                
                val dateStr = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(vp.venta.fecha))
                table.addCell(createStyledCell(dateStr, normalFont, rowColor, Element.ALIGN_CENTER))
                
                val detalles = vp.detalles.joinToString("\n") { "${it.detalle.cantidad}x ${it.producto.nombre}" }
                table.addCell(createStyledCell(detalles, normalFont, rowColor, Element.ALIGN_LEFT))
                
                table.addCell(createStyledCell(vp.venta.metodoPago.displayName, normalFont, rowColor, Element.ALIGN_CENTER))
                table.addCell(createStyledCell(currencyFormat.format(vp.venta.total), boldFont, rowColor, Element.ALIGN_RIGHT))
            }

            document.add(table)

            val granTotalTable = PdfPTable(1)
            granTotalTable.widthPercentage = 100f
            val totalFooterCell = PdfPCell(Phrase(context.getString(R.string.report_grand_total, currencyFormat.format(totalFacturado)), titleFont))
            totalFooterCell.border = Rectangle.NO_BORDER
            totalFooterCell.horizontalAlignment = Element.ALIGN_RIGHT
            totalFooterCell.setPaddingTop(20f)
            granTotalTable.addCell(totalFooterCell)
            document.add(granTotalTable)

            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Genera un PDF profesional para un presupuesto.
     */
    fun generateQuotePdf(
        items: List<com.manrique.trailerstock.model.CarritoItem>, 
        tipoCliente: String, 
        total: Double
    ): File? {
        try {
            val fileName = "Presupuesto_TTM_${fileDateFormat.format(Date())}.pdf"
            val file = File(context.cacheDir, fileName)
            val document = Document(PageSize.A4, 36f, 36f, 72f, 72f)
            val writer = PdfWriter.getInstance(document, FileOutputStream(file))
            
            writer.pageEvent = object : com.itextpdf.text.pdf.PdfPageEventHelper() {
                override fun onEndPage(writer: PdfWriter, document: Document) {
                    val cb = writer.directContent
                    val footerFont = Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL, BaseColor.GRAY)
                    
                    val footerLeft = Phrase("Presupuesto generado por TTM App", footerFont)
                    ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, footerLeft, document.left(), document.bottom() - 20, 0f)
                    
                    val footerRight = Phrase("Página ${writer.pageNumber}", footerFont)
                    ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, footerRight, document.right(), document.bottom() - 20, 0f)
                }
            }

            document.open()

            val primaryColor = BaseColor(74, 85, 104)
            val headerTitleFont = Font(Font.FontFamily.HELVETICA, 22f, Font.BOLD, primaryColor)
            val normalFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)
            val boldFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
            val whiteFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, BaseColor.WHITE)

            // Header
            val mainHeader = PdfPTable(2)
            mainHeader.widthPercentage = 100f
            mainHeader.setWidths(floatArrayOf(5f, 5f))
            
            val brandCell = PdfPCell(Phrase("TTM", Font(Font.FontFamily.HELVETICA, 28f, Font.BOLD, primaryColor)))
            brandCell.border = Rectangle.NO_BORDER
            mainHeader.addCell(brandCell)
            
            val titleCell = PdfPCell(Phrase("PRESUPUESTO", headerTitleFont))
            titleCell.border = Rectangle.NO_BORDER
            titleCell.horizontalAlignment = Element.ALIGN_RIGHT
            mainHeader.addCell(titleCell)
            
            document.add(mainHeader)
            document.add(Paragraph(" ").apply { setSpacingAfter(10f) })

            // Info de contacto / fecha
            val infoTable = PdfPTable(1)
            infoTable.widthPercentage = 100f
            val infoCell = PdfPCell()
            infoCell.setPadding(10f)
            infoCell.backgroundColor = BaseColor(245, 247, 250)
            infoCell.border = Rectangle.LEFT
            infoCell.setBorderColorLeft(primaryColor)
            infoCell.setBorderWidthLeft(3f)
            
            infoCell.addElement(Paragraph("Fecha: ${dateFormat.format(Date())}", normalFont))
            infoCell.addElement(Paragraph("Tipo de Cliente: $tipoCliente", normalFont))
            infoCell.addElement(Paragraph("Validez: 7 días corridos", Font(Font.FontFamily.HELVETICA, 8f, Font.ITALIC, BaseColor.GRAY)))
            
            infoTable.addCell(infoCell)
            document.add(infoTable)
            document.add(Paragraph(" "))

            // Tabla de productos
            val table = PdfPTable(4)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(1f, 5f, 2f, 2f))

            val headers = listOf("Cant.", "Producto", "Precio Unit.", "Subtotal")
            headers.forEach { h ->
                val cell = PdfPCell(Phrase(h, whiteFont))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                cell.setPadding(8f)
                cell.backgroundColor = primaryColor
                cell.border = Rectangle.NO_BORDER
                table.addCell(cell)
            }

            items.forEachIndexed { index, item ->
                val rowColor = if (index % 2 == 0) BaseColor(240, 240, 240) else BaseColor.WHITE
                
                table.addCell(createStyledCell("${item.cantidad}", normalFont, rowColor, Element.ALIGN_CENTER))
                
                // Celda de producto con promo
                val productCell = PdfPCell()
                productCell.backgroundColor = rowColor
                productCell.border = Rectangle.NO_BORDER
                productCell.setPadding(8f)
                productCell.addElement(Phrase(item.producto.nombre, normalFont))
                
                item.promocionAplicada?.let { promoConProd ->
                    val promoFont = Font(Font.FontFamily.HELVETICA, 8f, Font.ITALIC, BaseColor(76, 175, 80))
                    val promoText = "\nPromo: ${promoConProd.promocion.nombrePromo}"
                    productCell.addElement(Phrase(promoText, promoFont))
                    
                    promoConProd.promocion.descripcion?.let { desc ->
                        val descFont = Font(Font.FontFamily.HELVETICA, 7f, Font.ITALIC, BaseColor.GRAY)
                        productCell.addElement(Phrase("\n($desc)", descFont))
                    }
                }
                table.addCell(productCell)
                
                table.addCell(createStyledCell(currencyFormat.format(item.precioUnitario), normalFont, rowColor, Element.ALIGN_RIGHT))
                table.addCell(createStyledCell(currencyFormat.format(item.precioUnitario * item.cantidad), boldFont, rowColor, Element.ALIGN_RIGHT))
            }

            document.add(table)

            // Totales (Simplificado: Total vs Total con Descuento)
            val subtotalGeneral = items.sumOf { it.precioUnitario * it.cantidad }
            val descuentoGeneral = items.sumOf { it.descuentoAplicado }
            
            val totalTable = PdfPTable(2)
            totalTable.widthPercentage = 100f
            totalTable.setWidths(floatArrayOf(7f, 3f))
            
            val titleFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, primaryColor)
            
            if (descuentoGeneral > 0) {
                // Total Original
                totalTable.addCell(createLabelCell("TOTAL:"))
                totalTable.addCell(createValueCell(currencyFormat.format(subtotalGeneral), normalFont))
                
                // Total con Descuento
                val totalLabelCell = PdfPCell(Phrase("TOTAL CON DESCUENTO:", titleFont))
                totalLabelCell.border = Rectangle.NO_BORDER
                totalLabelCell.horizontalAlignment = Element.ALIGN_RIGHT
                totalLabelCell.setPadding(5f)
                totalTable.addCell(totalLabelCell)
                
                val totalValueCell = PdfPCell(Phrase(currencyFormat.format(total), titleFont))
                totalValueCell.border = Rectangle.NO_BORDER
                totalValueCell.horizontalAlignment = Element.ALIGN_RIGHT
                totalValueCell.setPadding(5f)
                totalTable.addCell(totalValueCell)
            } else {
                // Total (sin descuento)
                val totalLabelCell = PdfPCell(Phrase("TOTAL:", titleFont))
                totalLabelCell.border = Rectangle.NO_BORDER
                totalLabelCell.horizontalAlignment = Element.ALIGN_RIGHT
                totalLabelCell.setPadding(5f)
                totalTable.addCell(totalLabelCell)
                
                val totalValueCell = PdfPCell(Phrase(currencyFormat.format(total), titleFont))
                totalValueCell.border = Rectangle.NO_BORDER
                totalValueCell.horizontalAlignment = Element.ALIGN_RIGHT
                totalValueCell.setPadding(5f)
                totalTable.addCell(totalValueCell)
            }
            
            document.add(Paragraph(" ").apply { setSpacingBefore(10f) })
            document.add(totalTable)

            // Nota final
            document.add(Paragraph(" "))
            val note = Paragraph("Este presupuesto es informativo y no constituye una factura de venta. Los precios pueden variar sin previo aviso.", Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL, BaseColor.GRAY))
            note.alignment = Element.ALIGN_CENTER
            document.add(note)

            document.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun createStyledCell(text: String, font: Font, bgColor: BaseColor, align: Int): PdfPCell {
        val cell = PdfPCell(Phrase(text, font))
        cell.backgroundColor = bgColor
        cell.setPadding(8f)
        cell.border = Rectangle.NO_BORDER
        cell.horizontalAlignment = align
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        return cell
    }

    private fun createLabelCell(text: String): PdfPCell {
        val cell = PdfPCell(Phrase(text, Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD, BaseColor.DARK_GRAY)))
        cell.border = Rectangle.NO_BORDER
        cell.horizontalAlignment = Element.ALIGN_RIGHT
        cell.setPadding(5f)
        return cell
    }

    private fun createValueCell(text: String, font: Font): PdfPCell {
        val cell = PdfPCell(Phrase(text, font))
        cell.border = Rectangle.NO_BORDER
        cell.horizontalAlignment = Element.ALIGN_RIGHT
        cell.setPadding(5f)
        return cell
    }

    /**
     * Genera un Excel con el inventario actual.
     */
    fun generateInventoryExcel(productos: List<Producto>): File? {
        try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Inventario")

            val titleStyle = workbook.createCellStyle().apply {
                val font = workbook.createFont().apply {
                    bold = true
                    fontHeightInPoints = 16.toShort()
                    color = org.apache.poi.ss.usermodel.IndexedColors.DARK_BLUE.index
                }
                setFont(font)
            }

            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = org.apache.poi.ss.usermodel.IndexedColors.GREY_50_PERCENT.index
                fillPattern = org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND
                val font = workbook.createFont().apply {
                    color = org.apache.poi.ss.usermodel.IndexedColors.WHITE.index
                    bold = true
                }
                setFont(font)
                alignment = org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER
                verticalAlignment = org.apache.poi.ss.usermodel.VerticalAlignment.CENTER
            }

            val moneyStyle = workbook.createCellStyle().apply {
                val format = workbook.createDataFormat()
                dataFormat = format.getFormat("$ #,##0.00")
            }

            val warningStyle = workbook.createCellStyle().apply {
                fillForegroundColor = org.apache.poi.ss.usermodel.IndexedColors.ORANGE.index
                fillPattern = org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND
            }

            val rowTitle = sheet.createRow(0)
            rowTitle.createCell(0).apply {
                setCellValue("Reporte de Inventario - TTM")
                setCellStyle(titleStyle)
            }
            
            val rowMeta = sheet.createRow(1)
            rowMeta.createCell(0).setCellValue("Generado el ${dateFormat.format(Date())}")
            
            val rowAuthor = sheet.createRow(2)
            rowAuthor.createCell(0).setCellValue("Sistema desarrollado por Enzo Manrique")
            
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
                "Valor Inventario ($)"
            )
            
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.setCellStyle(headerStyle)
            }

            var capitalTotal = 0.0
            
            productos.forEachIndexed { index, producto ->
                val row = sheet.createRow(tableHeaderStart + index + 1)
                
                row.createCell(0).setCellValue(producto.id.toDouble())
                row.createCell(1).setCellValue(producto.nombre)
                row.createCell(2).setCellValue(producto.descripcion ?: "")
                
                row.createCell(3).apply {
                    setCellValue(producto.precioCosto)
                    setCellStyle(moneyStyle)
                }
                row.createCell(4).apply {
                    setCellValue(producto.precioLista)
                    setCellStyle(moneyStyle)
                }
                
                val stockCell = row.createCell(5).apply {
                    setCellValue(producto.stockActual.toDouble())
                }
                row.createCell(6).setCellValue(producto.stockMinimo.toDouble())
                
                if (producto.stockActual <= producto.stockMinimo) {
                    stockCell.setCellStyle(warningStyle)
                }
                
                val valorItem = producto.precioCosto * producto.stockActual
                row.createCell(7).apply {
                    setCellValue(valorItem)
                    setCellStyle(moneyStyle)
                }
                
                capitalTotal += valorItem
            }

            val totalRowIndex = tableHeaderStart + productos.size + 2
            val totalRow = sheet.createRow(totalRowIndex)
            totalRow.createCell(6).apply {
                setCellValue("CAPITAL TOTAL:")
                val fontBold = workbook.createFont().apply { bold = true }
                val styleBold = workbook.createCellStyle().apply { setFont(fontBold) }
                setCellStyle(styleBold)
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

            sheet.setAutoFilter(org.apache.poi.ss.util.CellRangeAddress(tableHeaderStart, tableHeaderStart, 0, 7))
            
            sheet.setColumnWidth(0, 3000)
            sheet.setColumnWidth(1, 8000)
            sheet.setColumnWidth(2, 10000)
            sheet.setColumnWidth(3, 4500)
            sheet.setColumnWidth(4, 4500)
            sheet.setColumnWidth(5, 4000)
            sheet.setColumnWidth(6, 4000)
            sheet.setColumnWidth(7, 5000)

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
}
