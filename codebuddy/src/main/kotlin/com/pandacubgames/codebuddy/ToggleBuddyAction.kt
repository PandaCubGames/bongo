package com.pandacubgames.codebuddy

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareToggleAction

class ToggleBuddyAction : DumbAwareToggleAction() {

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun isSelected(e: AnActionEvent): Boolean = BuddySettings.options().visible

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        BuddySettings.options().visible = state
        BuddyOverlay.refreshAll()
    }
}
