package de.krall.spreadsheets.ui.layout

import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager2

abstract class AbstractLayout : LayoutManager2 {

    override fun addLayoutComponent(component: Component, constraints: Any?) {}
    override fun addLayoutComponent(name: String?, component: Component) = addLayoutComponent(component, name)

    override fun removeLayoutComponent(component: Component) {}

    override fun minimumLayoutSize(target: Container): Dimension = Dimension(0, 0)
    abstract override fun preferredLayoutSize(target: Container): Dimension
    override fun maximumLayoutSize(target: Container): Dimension = Dimension(Short.MAX_VALUE.toInt(), Short.MAX_VALUE.toInt())

    abstract override fun layoutContainer(target: Container)

    override fun invalidateLayout(target: Container) {}

    override fun getLayoutAlignmentX(target: Container): Float = 0f
    override fun getLayoutAlignmentY(target: Container): Float = 0f
}
