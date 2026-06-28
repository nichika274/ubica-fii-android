package com.example.ubicafii.util

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {
    private const val PREFS_NAME = "ubicafii_prefs"
    private const val KEY_FAVORITES = "favorites"
    private const val KEY_RECENTS = "recents"
    private const val MAX_RECENTS = 20

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ---------- Favoritos ----------
    fun getFavorites(context: Context): Set<Int> {
        val prefs = getPrefs(context)
        return prefs.getStringSet(KEY_FAVORITES, emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet() ?: emptySet()
    }

    fun isFavorite(context: Context, id: Int): Boolean = id in getFavorites(context)

    fun addFavorite(context: Context, id: Int) {
        val favs = getFavorites(context).toMutableSet()
        favs.add(id)
        getPrefs(context).edit().putStringSet(KEY_FAVORITES, favs.map { it.toString() }.toSet()).apply()
    }

    fun removeFavorite(context: Context, id: Int) {
        val favs = getFavorites(context).toMutableSet()
        favs.remove(id)
        getPrefs(context).edit().putStringSet(KEY_FAVORITES, favs.map { it.toString() }.toSet()).apply()
    }

    // ---------- Recientes ----------
    fun getRecents(context: Context): List<Int> {
        val prefs = getPrefs(context)
        return prefs.getString(KEY_RECENTS, null)
            ?.split(",")
            ?.mapNotNull { it.toIntOrNull() }
            ?: emptyList()
    }

    fun addRecent(context: Context, id: Int) {
        val recents = getRecents(context).toMutableList()
        recents.remove(id) // Evita duplicados
        recents.add(0, id) // Añade al principio
        if (recents.size > MAX_RECENTS) recents.removeAt(recents.size - 1)
        getPrefs(context).edit().putString(KEY_RECENTS, recents.joinToString(",")).apply()
    }
}