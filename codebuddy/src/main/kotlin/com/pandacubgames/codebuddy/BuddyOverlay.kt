package com.pandacubgames.codebuddy

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.IconUtil
import com.intellij.util.ui.JBUI
import java.awt.Cursor
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JLayeredPane
import javax.swing.SwingUtilities
import javax.swing.Timer
import kotlin.math.max

/** How long after the last keystroke the tap frames stay before the mood frame returns. */
private const val TAP_HOLD_MS = 1500

/**
 * Draws the buddy on top of one project's IDE window and lets the user drag it around.
 * Every method must be called on the UI thread.
 */
@Service(Service.Level.PROJECT)
class BuddyOverlay(private val project: Project) : Disposable {

    private val options get() = BuddySettings.options()

    private var layeredPane: JLayeredPane? = null
    private var label: JLabel? = null
    private val icons = HashMap<String, Icon>()

    private var tapping = false
    private var tapToggle = false
    private val tapTimer = Timer(TAP_HOLD_MS) {
        tapping = false
        updateIcon()
    }.apply { isRepeats = false }

    private val resizeListener = object : ComponentAdapter() {
        override fun componentResized(e: ComponentEvent) = place()
    }

    /** Re-reads the settings: shows, hides, resizes and repositions as needed. */
    fun refresh() {
        detach()
        attach()
    }

    fun attach() {
        if (!options.visible || label != null || project.isDisposed) return
        val frame = WindowManager.getInstance().getFrame(project) ?: return
        val pane = frame.rootPane?.layeredPane ?: return

        val sizePx = JBUI.scale(options.sizeDip)
        val buddy = JLabel().apply {
            setSize(sizePx, sizePx)
            cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
        }
        installDragging(buddy, pane)

        layeredPane = pane
        label = buddy
        pane.add(buddy, JLayeredPane.POPUP_LAYER, 0)
        pane.addComponentListener(resizeListener)
        place()
        updateIcon()
    }

    fun tap() {
        if (label == null) return
        tapping = true
        tapToggle = !tapToggle
        tapTimer.restart()
        updateIcon()
    }

    fun moodChanged() = updateIcon()

    override fun dispose() = detach()

    private fun detach() {
        tapTimer.stop()
        tapping = false
        icons.clear()
        val pane = layeredPane
        val buddy = label
        layeredPane = null
        label = null
        if (pane != null && buddy != null) {
            pane.removeComponentListener(resizeListener)
            pane.remove(buddy)
            pane.repaint(buddy.bounds)
        }
    }

    private fun updateIcon() {
        val buddy = label ?: return
        val moods = project.service<BuddyMoodService>()
        val mood = moods.mood
        val frame = when {
            !tapping -> mood.frame
            tapToggle -> TAP_FRAME_1
            else -> TAP_FRAME_2
        }
        buddy.icon = icons.getOrPut(frame) { loadIcon(frame) }
        buddy.toolTipText = listOfNotNull("Code Buddy", moods.projectKind.label?.let { "$it project" }, mood.label)
            .joinToString(" · ")
    }

    private fun loadIcon(frame: String): Icon {
        val raw = runCatching { IconLoader.getIcon("/buddy/$frame.svg", BuddyOverlay::class.java) }
            .getOrDefault(AllIcons.General.Information)
        val scale = JBUI.scale(options.sizeDip).toFloat() / max(1, raw.iconHeight)
        return IconUtil.scale(raw, null, scale)
    }

    /** Puts the buddy at its saved spot, kept inside the window. */
    private fun place() {
        val pane = layeredPane ?: return
        val buddy = label ?: return
        val maxX = max(0, pane.width - buddy.width)
        val maxY = max(0, pane.height - buddy.height)
        val x = if (options.xDip >= 0) JBUI.scale(options.xDip) else maxX
        val y = if (options.yDip >= 0) JBUI.scale(options.yDip) else maxY
        buddy.setLocation(x.coerceIn(0, maxX), y.coerceIn(0, maxY))
    }

    private fun installDragging(buddy: JLabel, pane: JLayeredPane) {
        val handler = object : MouseAdapter() {
            private var grabX = 0
            private var grabY = 0

            override fun mousePressed(e: MouseEvent) {
                grabX = e.x
                grabY = e.y
            }

            override fun mouseDragged(e: MouseEvent) {
                val point = SwingUtilities.convertPoint(buddy, e.point, pane)
                val x = (point.x - grabX).coerceIn(0, max(0, pane.width - buddy.width))
                val y = (point.y - grabY).coerceIn(0, max(0, pane.height - buddy.height))
                buddy.setLocation(x, y)
                options.xDip = JBUI.unscale(x)
                options.yDip = JBUI.unscale(y)
            }
        }
        buddy.addMouseListener(handler)
        buddy.addMouseMotionListener(handler)
    }

    companion object {
        /** Applies changed settings to every open IDE window. */
        fun refreshAll() {
            for (project in ProjectManager.getInstance().openProjects) {
                if (!project.isDisposed) project.service<BuddyOverlay>().refresh()
            }
        }
    }
}
