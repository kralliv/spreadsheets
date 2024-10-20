package de.krall.spreadsheets.ui.components

import de.krall.spreadsheets.ui.action.isDefaultAction
import fernice.reflare.classes
import javax.swing.Action
import javax.swing.JButton

open class SButton(precedence: Precedence = Precedence.NORMAL) : JButton() {

    enum class Precedence {
        NORMAL,
        HIGH,
    }

    var precedence: Precedence = precedence
        set(precedence) {
            val previousPrecedence = field
            if (precedence != previousPrecedence) {
                previousPrecedence.toCssClass()?.let { cssClass ->
                    classes.remove(cssClass)
                }

                field = precedence

                precedence.toCssClass()?.let { cssClass ->
                    classes.add(cssClass)
                }

                firePropertyChange("precedence", previousPrecedence, precedence)
            }
        }

    init {
        precedence.toCssClass()?.let { cssClass ->
            classes.add(cssClass)
        }
    }

    private fun Precedence.toCssClass(): String? {
        return when (this) {
            Precedence.NORMAL -> null
            Precedence.HIGH -> "s-button-high"
        }
    }

    override fun configurePropertiesFromAction(action: Action?) {
        super.configurePropertiesFromAction(action)

        val isDefault = action?.isDefaultAction == true
        precedence = when {
            isDefault -> Precedence.HIGH
            else -> Precedence.NORMAL
        }
    }

    override fun actionPropertyChanged(action: Action, propertyName: String) {
        super.actionPropertyChanged(action, propertyName)

        val isDefault = action.isDefaultAction
        precedence = when {
            isDefault -> Precedence.HIGH
            else -> Precedence.NORMAL
        }
    }
}
