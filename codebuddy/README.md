# Code Buddy (working title)

A JetBrains IDE plugin: a small character that sits on top of the IDE window and reacts to
what you do. It only uses platform APIs, so it works in Rider, IntelliJ IDEA and the other
JetBrains IDEs from 2024.3 on.

The name, plugin id (`com.pandacubgames.codebuddy`) and art are placeholders. Settle the id
before the first Marketplace upload; it cannot change afterwards.

## Reactions

| What happens | Frame in `src/main/resources/buddy/` |
|---|---|
| Nothing | `idle.svg` |
| Typing, backspace, delete, enter, tab, paste | `tap1.svg` / `tap2.svg`, alternating |
| A process started from the IDE is running | `running.svg` |
| A debug session is active | `debugging.svg` |
| The debugger is paused on a breakpoint | `paused.svg` |

The tooltip also says whether the project is a Unity or Godot project.

## Commands

```bash
./gradlew runIde        # sandbox IDE with the plugin installed
./gradlew test          # unit tests
./gradlew buildPlugin   # installable zip in build/distributions/
./gradlew verifyPlugin  # API compatibility check against IDEA and Rider
```

To try the zip in your own Rider: Settings | Plugins | gear icon | Install Plugin from Disk.

## Where things are

| File | Role |
|---|---|
| `Listeners.kt` | Turns IDE events (typing, run, debug) into buddy events |
| `BuddyMoodService.kt` | Per-project state; decides the current mood |
| `BuddyMood.kt` | The list of moods and their frames |
| `BuddyOverlay.kt` | Draws the character on the IDE window; dragging |
| `BuddySettings.kt`, `BuddyConfigurable.kt` | Saved settings and their page under Appearance & Behavior |
| `ProjectKind.kt` | Unity / Godot project detection |

To add a reaction: add a value to `BuddyMood`, add its SVG, feed the state into
`BuddyMoodService` from a new listener, and register that listener in `plugin.xml`.
