package com.pandacubgames.codebuddy

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.XDebugSession
import java.util.concurrent.ConcurrentHashMap

/**
 * Tracks what is happening in one project and derives the buddy's [mood] from it.
 * Listeners call the mutators from any thread; the overlay is refreshed on the UI thread.
 */
@Service(Service.Level.PROJECT)
class BuddyMoodService(private val project: Project) {

    private val runningProcesses = ConcurrentHashMap.newKeySet<ProcessHandler>()
    private val debugSessions = ConcurrentHashMap.newKeySet<XDebugSession>()
    private val pausedSessions = ConcurrentHashMap.newKeySet<XDebugSession>()

    @Volatile
    var projectKind: ProjectKind = ProjectKind.OTHER
        set(value) {
            field = value
            notifyOverlay()
        }

    /** The most urgent thing wins. */
    val mood: BuddyMood
        get() = when {
            pausedSessions.isNotEmpty() -> BuddyMood.PAUSED
            debugSessions.isNotEmpty() -> BuddyMood.DEBUGGING
            runningProcesses.isNotEmpty() -> BuddyMood.RUNNING
            else -> BuddyMood.IDLE
        }

    fun processStarted(handler: ProcessHandler) = change { runningProcesses.add(handler) }
    fun processTerminated(handler: ProcessHandler) = change { runningProcesses.remove(handler) }

    fun debugStarted(session: XDebugSession) = change { debugSessions.add(session) }
    fun debugPaused(session: XDebugSession) = change { pausedSessions.add(session) }
    fun debugResumed(session: XDebugSession) = change { pausedSessions.remove(session) }
    fun debugStopped(session: XDebugSession) = change {
        debugSessions.remove(session)
        pausedSessions.remove(session)
    }

    private inline fun change(block: () -> Unit) {
        block()
        notifyOverlay()
    }

    private fun notifyOverlay() {
        ApplicationManager.getApplication().invokeLater {
            if (!project.isDisposed) project.service<BuddyOverlay>().moodChanged()
        }
    }
}
