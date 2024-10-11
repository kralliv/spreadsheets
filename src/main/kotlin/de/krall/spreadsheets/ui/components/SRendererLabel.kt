package de.krall.spreadsheets.ui.components

import fernice.reflare.light.FLabel
import java.awt.Rectangle

/**
 * Label specifically indented be exclusively used in renderers for performance reasons.
 * Will cause glitchy behaviour anywhere else.
 */
class SRendererLabel : FLabel() {

    override fun invalidate() {}

    override fun validate() {}

    override fun revalidate() {}

    override fun repaint(tm: Long, x: Int, y: Int, width: Int, height: Int) {}

    override fun repaint(r: Rectangle) {}

    override fun repaint() {}

    override fun firePropertyChange(propertyName: String, oldValue: Any?, newValue: Any?) {
        if (propertyName === "text"
            || propertyName === "labelFor"
            || propertyName === "displayedMnemonic"
            || ((propertyName === "font" || propertyName === "foreground")
                    && oldValue !== newValue
                    && getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey) != null)
        ) {

            super.firePropertyChange(propertyName, oldValue, newValue)
        }
    }

    override fun firePropertyChange(propertyName: String, oldValue: Boolean, newValue: Boolean) {}
}