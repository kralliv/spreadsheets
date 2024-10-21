package de.krall.spreadsheets.ui.dialog

import de.krall.spreadsheets.ui.env.OS
import de.krall.spreadsheets.ui.env.Session
import de.krall.spreadsheets.ui.util.window
import java.awt.Component
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

object FileDialogs {

    fun selectFile(
        invoker: Component?,
        title: String? = null,
        initial: File? = Session.lastDirectory.toFile(),
    ): File? {
        val file = when {
            OS.isMac -> selectFileAwt(invoker, title, initial)
            else -> selectFileSwing(invoker, title, initial)
        }
        if (file != null && file.parentFile != null) {
            Session.lastDirectory = file.parentFile.toPath()
        }
        return file
    }

    private fun selectFileAwt(invoker: Component?, title: String?, initial: File?): File? {
        val frame = invoker?.ownerFrame

        val fd = FileDialog(frame, title ?: "", FileDialog.LOAD)
        fd.directory = initial?.absolutePath
        fd.isMultipleMode = false
        fd.isVisible = true

        return when {
            fd.directory != null && fd.file != null -> File(fd.directory + fd.file)
            else -> null
        }
    }

    private fun selectFileSwing(invoker: Component?, title: String?, initial: File?): File? {
        val frame = invoker?.ownerFrame

        val fileChooser = JFileChooser(initial)
        fileChooser.dialogTitle = title
        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY

        return when (fileChooser.showOpenDialog(frame)) {
            JFileChooser.APPROVE_OPTION -> fileChooser.selectedFile
            else -> null
        }
    }

    fun selectDirectory(
        invoker: Component?,
        title: String? = null,
        initial: File? = Session.lastDirectory.toFile(),
    ): File? {
        val file = when {
            OS.isMac -> selectDirectoryAwt(invoker, title, initial)
            else -> selectDirectorySwing(invoker, title, initial)
        }
        if (file != null) {
            Session.lastDirectory = file.toPath()
        }
        return file
    }

    private fun selectDirectoryAwt(invoker: Component?, title: String?, initial: File?): File? {
        val frame = invoker?.ownerFrame

        val fd = FileDialog(frame, title ?: "", FileDialog.LOAD)
        fd.directory = initial?.absolutePath
        fd.isMultipleMode = false
        System.setProperty("apple.awt.fileDialogForDirectories", "true")
        try {
            fd.isVisible = true
        } finally {
            System.setProperty("apple.awt.fileDialogForDirectories", "false")
        }

        return when {
            fd.directory != null && fd.file != null -> File(fd.directory + fd.file)
            else -> null
        }
    }

    private fun selectDirectorySwing(invoker: Component?, title: String?, initial: File?): File? {
        val frame = invoker?.ownerFrame

        val chooser = JFileChooser(initial)
        chooser.dialogTitle = title
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY

        return when (chooser.showOpenDialog(frame)) {
            JFileChooser.APPROVE_OPTION -> chooser.selectedFile
            else -> null
        }
    }
}

private val Component.ownerFrame: Frame?
    get() {
        var window = window
        while (window != null) {
            if (window is Frame) return window
            window = window.owner
        }
        return null
    }


