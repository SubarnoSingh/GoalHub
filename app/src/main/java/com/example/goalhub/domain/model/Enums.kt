package com.example.goalhub.domain.model

/**
 * Lifecycle status of a goal / project / target / task.
 *
 * [OVERDUE] is a *derived* status: it is never written to the database. The mapper
 * computes it at read time when the item is still open but its end date has passed.
 * The other values are stored as-is.
 */
enum class Status(val label: String, val isStored: Boolean = true) {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    PAUSED("Paused"),
    ARCHIVED("Archived"),
    OVERDUE("Overdue", isStored = false);

    val isOpen: Boolean
        get() = this == NOT_STARTED || this == IN_PROGRESS || this == OVERDUE

    companion object {
        fun fromName(name: String?): Status = entries.firstOrNull { it.name == name } ?: NOT_STARTED
    }
}

/** Simple priority ladder. Visually kept subtle in the UI. */
enum class Priority(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical");

    companion object {
        fun fromName(name: String?): Priority = entries.firstOrNull { it.name == name } ?: MEDIUM
        /** Higher = more urgent. */
        val order: (Priority) -> Int get() = { it.ordinal }
    }
}

/**
 * User-facing category. The system is intentionally easy to modify:
 *  - add a value here,
 *  - give it a [presetIndex] pointing into `Gradients.Presets` (ui/theme/Gradients.kt),
 *  - optionally add an icon mapping in `CategoryIcons` (ui/components/CategoryIcons.kt).
 */
enum class Category(val label: String, val presetIndex: Int) {
    STUDY("Study", 0),
    PROJECTS("Projects", 0),
    WORK("Work", 2),
    PERSONAL("Personal", 1),
    FITNESS("Fitness", 3),
    FINANCE("Finance", 4),
    OTHER("Other", 5);

    companion object {
        fun fromName(name: String?): Category = entries.firstOrNull { it.name == name } ?: OTHER
    }
}

/** How [progress] is maintained for an item. */
enum class ProgressMode(val label: String) {
    /** Progress is derived automatically from subtasks / milestones / tasks. */
    AUTO("Auto"),
    /** Progress is set by hand. */
    MANUAL("Manual");

    companion object {
        fun fromName(name: String?): ProgressMode = entries.firstOrNull { it.name == name } ?: AUTO
    }
}
