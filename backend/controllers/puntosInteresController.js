const { getDb } = require('../database');

exports.getAll = async (req, res) => {
  try {
    const db = await getDb();
    const result = await db.execute('SELECT * FROM puntos_interes');
    res.json(result.rows);
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
};

exports.seed = async (req, res) => {
  try {
    const db = await getDb();
    await db.execute('DELETE FROM puntos_interes');

    const puntos = [
      { nombre: 'Entrada Principal', tipo: 'entrada', mapaX: 0.3891, mapaY: 0.6138 },  // ajusta según tu imagen
      { nombre: 'Cafetería Central', tipo: 'cafeteria', mapaX: 0.2597, mapaY: 0.380 },
    ];

    for (const p of puntos) {
      await db.execute({
        sql: 'INSERT INTO puntos_interes (nombre, tipo, mapaX, mapaY) VALUES (?, ?, ?, ?)',
        args: [p.nombre, p.tipo, p.mapaX, p.mapaY]
      });
    }

    res.json({ mensaje: 'Puntos de interés creados', cantidad: puntos.length });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
};