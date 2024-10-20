package de.krall.spreadsheets.ui.dialog

import java.awt.Component

abstract class SResultDialogContent<T> : SDialogContent() {

    var result: T? = null

    fun closeWithResult(result: T?) {
        this.result = result

        close(if (result != null) POSITIVE else NEUTRAL)
    }

    fun showAndGet(invoker: Component?): T? {
        showAndWait(invoker)

        return if (outcome != NEUTRAL) result else null
    }
}
