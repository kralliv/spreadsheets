package de.krall.spreadsheets.ui.env

import kotlin.io.path.Path

object Session {
    var lastDirectory = Path(System.getProperty("user.home"))
}
