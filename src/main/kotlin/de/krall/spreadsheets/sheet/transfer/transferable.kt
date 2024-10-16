package de.krall.spreadsheets.sheet.transfer

import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.ClipboardOwner
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException

class SpreadsheetTransferable(
    data: TransferableSpreadsheet,
) : Transferable, ClipboardOwner {

    private var data: TransferableSpreadsheet? = data

    override fun getTransferDataFlavors(): Array<out DataFlavor> {
        return arrayOf(Flavor, DataFlavor.stringFlavor)
    }

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean {
        return transferDataFlavors.any { it == flavor }
    }

    override fun getTransferData(flavor: DataFlavor): Any {
        val data = this@SpreadsheetTransferable.data ?: throw IOException("data no longer available")
        return when (flavor) {
            Flavor -> data
            DataFlavor.stringFlavor -> data.toCsv()
            else -> throw UnsupportedFlavorException(flavor)
        }
    }

    override fun lostOwnership(clipboard: Clipboard?, contents: Transferable?) {
        data = null
    }

    companion object {
        val Flavor = DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "spreadsheet")
    }
}
