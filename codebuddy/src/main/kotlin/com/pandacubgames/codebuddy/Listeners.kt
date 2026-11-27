package com.pandacubgames.codebuddy

import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.AnActionResult
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebugSessionListener
import com.intellij.xdebugger.XDebuggerManagerListener

/** Editor actions that count as "typing" even though no character is typed. */
private val TAP_ACTIONS = setOf(
    IdeActions.ACTION_EDITOR_BACKSPACE,
    IdeActions.ACTION_EDITOR_DELETE,
    IdeActions.ACTION_EDITOR_ENTER,
    IdeActions.ACTION_EDITOR_TAB,
    IdeActions.ACTION_EDITOR_PASTE,
)

/** Typed characters and the editing keys above make the buddy of that project tap. */
class TypingListener : AnActionListener {

    override fun beforeEditorTyping(c: Char, dataContext: DataContext) {
        tap(CommonDataKeys.PROJECT.getData(dataContext))
    }

    override fun afterActionPerformed(action: AnAction, event: AnActionEvent, result: AnActionResult) {
        if (ActionManager.getInstance().getId(action) in TAP_ACTIONS) tap(event.project)
    }

    private fun tap(project: Project?) {
        if (project == null || project.isDisposed) return
        project.service<BuddyOverlay>().tap()
    }
}

/** Anything started from the IDE with Run, Debug, Profile and so on. */
class RunListener(private val project: Project) : ExecutionListener {

    override fun processStarted(executorId: String, env: ExecutionEnvironment, handler: ProcessHandler) {
        project.service<BuddyMoodService>().processStarted(handler)
    }

    override fun processTerminated(
        executorId: String,
        env: ExecutionEnvironment,
        handler: ProcessHandler,
        exitCode: Int,
    ) {
        project.service<BuddyMoodService>().processTerminated(handler)
    }
}

/** Debug sessions, including ones attached to an already running process. */
class DebugListener(private val project: Project) : XDebuggerManagerListener {

    override fun processStarted(debugProcess: XDebugProcess) {
        val session: XDebugSession = debugProcess.session
        val moods = project.service<BuddyMoodService>()
        moods.debugStarted(session)
        session.addSessionListener(object : XDebugSessionListener {
            override fun sessionPaused() = moods.debugPaused(session)
            override fun sessionResumed() = moods.debugResumed(session)
            override fun sessionStopped() = moods.debugStopped(session)
        })
    }

    override fun processStopped(debugProcess: XDebugProcess) {
        project.service<BuddyMoodService>().debugStopped(debugProcess.session)
    }
}
