package de.krall.spreadsheets.ui.layout

import java.awt.Container
import java.awt.Dimension
import kotlin.math.max

class HorizontalLayout(val gap: Int = 0) : AbstractLayout() {

    override fun preferredLayoutSize(target: Container): Dimension {
        val size = Dimension()

        var visibleCount = 0
        for (index in 0..<target.componentCount) {
            val component = target.getComponent(index)
            if (!component.isVisible) continue

            val preferredSize = component.preferredSize

            size.width += preferredSize.width
            size.height = max(preferredSize.height, size.height)

            visibleCount++
        }

        size.width += max(visibleCount - 1, 0) * gap

        val insets = target.insets
        size.width += insets.left + insets.right
        size.height += insets.top + insets.bottom

        return size
    }

    override fun layoutContainer(target: Container) {
        val insets = target.insets
        val bounds = target.bounds
        bounds.x = insets.left
        bounds.y = insets.top
        bounds.width -= insets.left + insets.right
        bounds.height -= insets.top + insets.bottom

        var offset = 0
        for (index in 0..<target.componentCount) {
            val component = target.getComponent(index)
            if (!component.isVisible) continue

            val preferredSize = component.preferredSize

            component.setBounds(bounds.x + offset, bounds.y, preferredSize.width, bounds.height)

            offset += preferredSize.width + gap
        }
    }
}
