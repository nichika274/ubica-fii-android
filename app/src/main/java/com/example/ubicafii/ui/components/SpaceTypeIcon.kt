package com.example.ubicafii.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ubicafii.ui.theme.*

fun getTypeIcon(type: String): ImageVector {
    return when (type.lowercase().trim()) {
        "aula" -> Icons.Default.School
        "laboratorio" -> Icons.Default.Science
        "oficina" -> Icons.Default.BusinessCenter
        "baño" -> Icons.Default.People
        "biblioteca" -> Icons.Default.LibraryBooks
        "cafetería", "cafeteria" -> Icons.Default.Restaurant
        "auditorio" -> Icons.Default.Weekend
        "otro" -> Icons.Default.Category
        else -> Icons.Default.Place
    }
}

fun getTypeColor(type: String): Color {
    return when (type.lowercase().trim()) {
        "aula" -> TypeAula
        "laboratorio" -> TypeLab
        "oficina" -> TypeOffice
        "baño" -> TypeBath
        "biblioteca" -> TypeLibrary
        "cafetería", "cafeteria" -> TypeCafe
        "otro" -> TypeOther
        else -> TypeOther
    }
}

fun getTypeBgColor(type: String): Color {
    return when (type.lowercase().trim()) {
        "aula" -> TypeBgAula
        "laboratorio" -> TypeBgLab
        "oficina" -> TypeBgOffice
        "baño" -> TypeBgBath
        "biblioteca" -> TypeBgLibrary
        "cafetería", "cafeteria" -> TypeBgCafe
        "otro" -> TypeBgOther
        else -> TypeBgOther
    }
}