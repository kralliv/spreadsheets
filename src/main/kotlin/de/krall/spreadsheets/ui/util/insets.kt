package de.krall.spreadsheets.ui.util

import org.fernice.reflare.ui.FlareBorder
import java.awt.Component
import java.awt.Insets
import javax.swing.JComponent

val Component.marginBorderInsets: Insets
    get() {
        if (this !is JComponent) return Insets(0, 0, 0, 0)
        val border = border
        if (border !is FlareBorder) return Insets(0, 0, 0, 0)
        return border.getMarginAndBorderInsets()
    }

val Component.paddingInsets: Insets
    get() {
        if (this !is JComponent) return Insets(0, 0, 0, 0)
        val border = border
        if (border !is FlareBorder) return Insets(0, 0, 0, 0)
        return border.getPaddingInsets()
    }
