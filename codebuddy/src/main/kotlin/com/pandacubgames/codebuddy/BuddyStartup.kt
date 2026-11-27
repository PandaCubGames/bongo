package com.pandacubgames.codebuddy

import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

class BuddyStartup : ProjectActivity {
    override suspend fun execute(project: Project) {
        val kind = ProjectKind.detect(project.basePath?.let(Path::of))
        withContext(Dispatchers.EDT) {
            project.service<BuddyOverlay>().attach()
        }
        project.service<BuddyMoodService>().projectKind = kind
    }
}
