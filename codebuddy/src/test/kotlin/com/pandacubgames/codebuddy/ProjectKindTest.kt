package com.pandacubgames.codebuddy

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ProjectKindTest {
    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun `unity project is detected by its version file`() {
        temp.newFolder("ProjectSettings")
        temp.newFile("ProjectSettings/ProjectVersion.txt")
        assertEquals(ProjectKind.UNITY, ProjectKind.detect(temp.root.toPath()))
    }

    @Test
    fun `godot project is detected by its project file`() {
        temp.newFile("project.godot")
        assertEquals(ProjectKind.GODOT, ProjectKind.detect(temp.root.toPath()))
    }

    @Test
    fun `anything else is not a game project`() {
        assertEquals(ProjectKind.OTHER, ProjectKind.detect(temp.root.toPath()))
        assertEquals(ProjectKind.OTHER, ProjectKind.detect(null))
    }
}
