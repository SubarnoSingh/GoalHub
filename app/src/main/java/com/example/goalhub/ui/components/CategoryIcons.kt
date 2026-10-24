package com.example.goalhub.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.School
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.goalhub.domain.model.Category

/** Category → icon. Add a new category value here to give it an icon. */
val CategoryIcons: Map<Category, ImageVector> = mapOf(
    Category.STUDY to Icons.Rounded.School,
    Category.PROJECTS to Icons.Rounded.Code,
    Category.WORK to Icons.Rounded.Business,
    Category.PERSONAL to Icons.Rounded.Favorite,
    Category.FITNESS to Icons.Rounded.FitnessCenter,
    Category.FINANCE to Icons.Rounded.AttachMoney,
    Category.OTHER to Icons.Rounded.Label,
)
