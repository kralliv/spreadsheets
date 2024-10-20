package de.krall.spreadsheets.ui.icon

import java.awt.Image
import java.awt.image.BaseMultiResolutionImage
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon

object Icons {
    object Actions {
        val Add = loadIcon("/actions/add.png")
        val Open = loadIcon("/actions/open.png")
    }

    object Symbols {
        val Directory = loadIcon("/symbols/directory.png")
    }
}

private fun loadIcon(path: String): Icon {
    val resource = "/spreadsheets/icons$path"
    val url = Icons::class.java.getResource(resource) ?: error("cannot locate icon '$resource'")

    val hiresResource = createResourceVariant(resource)
    val hiresUrl = Icons::class.java.getResource(hiresResource)

    val image = createImage(url, hiresUrl)

    return ImageIcon(image)
}

private fun createResourceVariant(resource: String): String {
    val fileName = resource.substringBeforeLast('.')
    val extension = resource.substringAfterLast('.', missingDelimiterValue = "")

    return buildString {
        append(fileName)
        append("@2x")
        if (extension.isNotEmpty()) {
            append('.')
            append(extension)
        }
    }
}

private fun createImage(url: URL, hiresUrl: URL?): Image {
    val image = ImageIO.read(url)
    if (hiresUrl == null) return image

    val hiresImage = ImageIO.read(hiresUrl)

    return BaseMultiResolutionImage(image, hiresImage)
}
