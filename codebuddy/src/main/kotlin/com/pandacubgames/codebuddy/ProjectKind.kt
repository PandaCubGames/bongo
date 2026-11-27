package com.pandacubgames.codebuddy

import java.nio.file.Files
import java.nio.file.Path

/** Which game engine, if any, the opened project belongs to. */
enum class ProjectKind(val label: String?) {
    UNITY("Unity"),
    GODOT("Godot"),
    OTHER(null);

    companion object {
        /** Touches the file system, so call it off the UI thread. */
        fun detect(root: Path?): ProjectKind = when {
            root == null -> OTHER
            Files.isRegularFile(root.resolve("ProjectSettings/ProjectVersion.txt")) -> UNITY
            Files.isRegularFile(root.resolve("project.godot")) -> GODOT
            else -> OTHER
        }
    }
}
