package de.krall.spreadsheets.ui.file

import de.krall.spreadsheets.ui.OS
import de.krall.spreadsheets.ui.util.window
import java.awt.Component
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

object FileChoosers {

    fun showFileChooser(invoker: Component?, title: String? = null, initial: File? = null): File? {
        return when {
            OS.isWindows -> {
                try {
                    showAwtFileChooser(invoker, title, initial)
                } catch (ignore: Throwable) {
                    showSwingFileChooser(invoker, title, initial)
                }
            }

            OS.isMac -> showAwtFileChooser(invoker, title, initial)
            else -> showSwingFileChooser(invoker, title, initial)
        }
    }

    private fun showAwtFileChooser(invoker: Component?, title: String?, initial: File?): File? {
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

    private fun showSwingFileChooser(invoker: Component?, title: String?, initial: File?): File? {
        val frame = invoker?.ownerFrame

        val fileChooser = JFileChooser(initial)
        fileChooser.dialogTitle = title
        fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY

        return when (fileChooser.showOpenDialog(frame)) {
            JFileChooser.APPROVE_OPTION -> fileChooser.selectedFile
            else -> null
        }
    }

    fun showDirectoryChooser(invoker: Component?, title: String? = null, initial: File? = null): File? {
        return when {
            OS.isWindows -> {
                try {
                    showAwtDirectoryChooser(invoker, title, initial)
                } catch (ignore: Throwable) {
                    showSwingDirectoryChooser(invoker, title, initial)
                }
            }

            OS.isMac -> showAwtDirectoryChooser(invoker, title, initial)
            else -> showSwingDirectoryChooser(invoker, title, initial)
        }
    }

    private fun showAwtDirectoryChooser(invoker: Component?, title: String?, initial: File?): File? {
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

    private fun showSwingDirectoryChooser(invoker: Component?, title: String?, initial: File?): File? {
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


