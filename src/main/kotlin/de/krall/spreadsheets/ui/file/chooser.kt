package de.krall.spreadsheets.ui.file

import de.krall.spreadsheets.ui.OS
import java.awt.Component
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Window
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

fun FileChooser(invoker: Component, title: String? = null, initial: File? = null): File? {
    return when {
//        OS.isMac -> AwtFileChooser(invoker, title, initial)
        else -> SwingFileChooser(invoker, title, initial)
    }
}

private fun AwtFileChooser(invoker: Component, title: String?, initial: File?): File? {
    val frame = invoker.ownerFrame

    val fd = FileDialog(frame, title ?: "", FileDialog.LOAD)
    fd.directory = initial?.absolutePath
    fd.isMultipleMode = false
    fd.isVisible = true

    return when {
        fd.directory != null && fd.file != null -> File(fd.directory + fd.file)
        else -> null
    }
}

private fun SwingFileChooser(invoker: Component, title: String?, initial: File?): File? {
    val frame = invoker.ownerFrame

    val fileChooser = JFileChooser(initial)
    fileChooser.dialogTitle = title
    fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY

    return when (fileChooser.showOpenDialog(frame)) {
        JFileChooser.APPROVE_OPTION -> fileChooser.selectedFile
        else -> null
    }
}

fun DirectoryChooser(invoker: Component, title: String? = null, initial: File? = null): File? {
    return when {
        OS.isMac -> AwtDirectoryChooser(invoker, title, initial)
        else -> SwingDirectoryChooser(invoker, title, initial)
    }
}

private fun AwtDirectoryChooser(invoker: Component, title: String?, initial: File?): File? {
    val frame = invoker.ownerFrame

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

private fun SwingDirectoryChooser(invoker: Component, title: String?, initial: File?): File? {
    val frame = invoker.ownerFrame

    val chooser = JFileChooser(initial)
    chooser.dialogTitle = title
    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY

    return when (chooser.showOpenDialog(frame)) {
        JFileChooser.APPROVE_OPTION -> chooser.selectedFile
        else -> null
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

private val Component.window: Window?
    get() = when (this) {
        is Window -> this
        else -> SwingUtilities.getWindowAncestor(this)
    }
