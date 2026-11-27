package com.pandacubgames.codebuddy

/**
 * What the buddy is currently doing. [frame] is the SVG file name (without extension) in
 * `resources/buddy/`. Typing is not a mood: it briefly overrides the mood with the tap frames.
 *
 * To add a reaction: add a value here, drop `<frame>.svg` into `resources/buddy/`,
 * and make [BuddyMoodService.mood] return it.
 */
enum class BuddyMood(val frame: String, val label: String) {
    IDLE("idle", "idle"),
    RUNNING("running", "app running"),
    DEBUGGING("debugging", "debugging"),
    PAUSED("paused", "paused on a breakpoint"),
}

const val TAP_FRAME_1 = "tap1"
const val TAP_FRAME_2 = "tap2"
