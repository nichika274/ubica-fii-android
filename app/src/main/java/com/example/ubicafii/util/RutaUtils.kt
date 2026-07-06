package com.example.ubicafii.util


fun encontrarRuta(origenId: String, destinoId: String): List<Nodo>? {
    val nodos = GrafoNavegacion.nodos.associateBy { it.id }
    val grafo = mutableMapOf<String, MutableList<Pair<String, Float>>>()
    for (c in GrafoNavegacion.conexiones) {
        grafo.getOrPut(c.desde) { mutableListOf() }.add(c.hasta to c.peso)
        grafo.getOrPut(c.hasta) { mutableListOf() }.add(c.desde to c.peso)
    }

    val dist = mutableMapOf<String, Float>()
    val prev = mutableMapOf<String, String?>()
    val visitados = mutableSetOf<String>()

    for (nodo in nodos.keys) {
        dist[nodo] = Float.MAX_VALUE
        prev[nodo] = null
    }
    dist[origenId] = 0f

    while (true) {
        val u = dist.filterKeys { it !in visitados }.minByOrNull { it.value }?.key ?: break
        if (u == destinoId) break
        visitados.add(u)
        for ((v, peso) in grafo[u] ?: emptyList()) {
            if (v in visitados) continue
            val alt = dist[u]!! + peso
            if (alt < dist[v]!!) {
                dist[v] = alt
                prev[v] = u
            }
        }
    }

    val ruta = mutableListOf<Nodo>()
    var actual: String? = destinoId
    while (actual != null) {
        nodos[actual]?.let { ruta.add(0, it) }
        actual = prev[actual]
    }
    return if (ruta.firstOrNull()?.id == origenId) ruta else null
}