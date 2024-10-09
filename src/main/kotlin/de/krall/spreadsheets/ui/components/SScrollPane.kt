package de.krall.spreadsheets.ui.components

import fernice.reflare.light.FScrollPane
import java.awt.Component

class SScrollPane : FScrollPane {

    constructor()
    constructor(component: Component?) : super(component)
    constructor(component: Component?, vsbPolicy: Int, hsbPolicy: Int) : super(component, vsbPolicy, hsbPolicy)
    constructor(vsbPolicy: Int, hsbPolicy: Int) : super(vsbPolicy, hsbPolicy)

    init {
        verticalScrollBar.unitIncrement = 12
        horizontalScrollBar.unitIncrement = 12

        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED)
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED)
    }
}