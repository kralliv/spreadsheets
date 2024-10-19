package de.krall.spreadsheets.ui.components

import java.awt.Dimension
import java.lang.ref.WeakReference
import kotlin.math.max

class SSizedLabel(val sizingGroup: SizingGroup) : SLabel(), SizingGroupAware {

    init {
        sizingGroup.register(this)
    }

    override val unbiasedPreferredSize: Dimension
        get() = super.getPreferredSize()

    override fun getPreferredSize(): Dimension? {
        val size = super.getPreferredSize()
        size.width = sizingGroup.preferredSize.width
        return size
    }
}

interface SizingGroupAware {
    val unbiasedPreferredSize: Dimension
    fun isVisible(): Boolean
}

class SizingGroup {

    private val components = mutableListOf<WeakReference<SizingGroupAware>>()

    fun register(component: SizingGroupAware) {
        components.add(WeakReference(component))
    }

    val preferredSize: Dimension
        get() {
            val size = Dimension()
            val iterator = components.iterator()
            while (iterator.hasNext()) {
                val component = iterator.next().get()
                if (component == null) {
                    iterator.remove()
                    continue
                }

                if (!component.isVisible()) continue

                val preferredSize = component.unbiasedPreferredSize

                size.width = max(preferredSize.width, size.width)
                size.height = max(preferredSize.height, size.height)
            }
            return size
        }
}
