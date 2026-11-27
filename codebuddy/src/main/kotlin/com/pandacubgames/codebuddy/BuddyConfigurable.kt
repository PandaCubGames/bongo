package com.pandacubgames.codebuddy

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindIntValue
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel

class BuddyConfigurable : BoundConfigurable("Code Buddy") {

    override fun createPanel(): DialogPanel {
        val options = BuddySettings.options()
        return panel {
            row {
                checkBox("Show the buddy").bindSelected(options::visible)
            }
            row("Size:") {
                spinner(MIN_SIZE_DIP..MAX_SIZE_DIP, 8).bindIntValue(options::sizeDip)
            }
            row {
                button("Reset Position") {
                    options.xDip = -1
                    options.yDip = -1
                    BuddyOverlay.refreshAll()
                }
            }
        }
    }

    override fun apply() {
        super.apply()
        BuddyOverlay.refreshAll()
    }
}
