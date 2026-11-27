package com.pandacubgames.codebuddy

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

const val DEFAULT_SIZE_DIP = 128
const val MIN_SIZE_DIP = 32
const val MAX_SIZE_DIP = 512

/** Application-wide settings, so the buddy looks the same in every project. */
@Service(Service.Level.APP)
@State(name = "CodeBuddySettings", storages = [Storage("codeBuddy.xml")])
class BuddySettings : SimplePersistentStateComponent<BuddySettings.Options>(Options()) {

    class Options : BaseState() {
        var visible by property(true)
        var sizeDip by property(DEFAULT_SIZE_DIP)

        /** Top-left corner inside the IDE window; -1 means "bottom-right corner". */
        var xDip by property(-1)
        var yDip by property(-1)
    }

    companion object {
        fun options(): Options = service<BuddySettings>().state
    }
}
