package com.example.goalhub.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.goalhub.domain.model.Category

/**
 * All card gradients live here. The visual language:
 *  - blue/cyan   → study / project work
 *  - purple/pink → personal
 *  - indigo      → work
 *  - green/teal  → fitness & success states
 *  - orange      → finance
 *  - slate       → everything else
 */
object Gradients {

    val Study = listOf(Color(0xFF4FACFE), Color(0xFF00C6FB))
    val Projects = listOf(Color(0xFF2B6DE0), Color(0xFF19C3E7))
    val Personal = listOf(Color(0xFFA18CD1), Color(0xFFF2A6D0))
    val Work = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
    val Fitness = listOf(Color(0xFF2FC58E), Color(0xFF38E8C2))
    val Finance = listOf(Color(0xFFF6B868), Color(0xFFF88C5F))
    val Other = listOf(Color(0xFF8FA3B8), Color(0xFF6E87A3))
    val Success = listOf(Color(0xFF0FB59A), Color(0xFF5BE08B))

    /** Indexable presets; `Category.presetIndex` points into this list. */
    val Presets: List<List<Color>> = listOf(
        Study,      // 0
        Personal,   // 1
        Work,       // 2
        Fitness,    // 3
        Finance,    // 4
        Other,      // 5
    )

    val PresetNames = listOf("Ocean", "Bloom", "Violet", "Mint", "Ember", "Slate")

    fun forCategory(category: Category): List<Color> = Presets[category.presetIndex]

    fun forGoal(gradientIndex: Int, completed: Boolean): List<Color> =
        if (completed) Success else Presets[gradientIndex.coerceIn(0, Presets.lastIndex)]

    fun brush(colors: List<Color>): Brush = Brush.linearGradient(colors)

    fun progressTrack(color: Color): Color = color.copy(alpha = 0.28f)

    /** Soft chip background for a category, based on its gradient's first color. */
    fun chipBackground(color: Color): Color = color.copy(alpha = 0.14f)
}
