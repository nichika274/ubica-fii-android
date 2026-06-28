const { getDb } = require('../database');

exports.getAll = async (req, res) => {
  try {
    const db = await getDb();
    const result = await db.execute('SELECT * FROM bloques ORDER BY id ASC');
    // result.rows ya es un array de objetos
    res.json(result.rows);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.getByBloque = async (req, res) => {
  try {
    const db = await getDb();
    const result = await db.execute({
      sql: 'SELECT * FROM bloques WHERE bloque = ?',
      args: [req.params.bloque]
    });

    if (result.rows.length === 0) {
      return res.status(404).json({ mensaje: 'Bloque no encontrado' });
    }

    res.json(result.rows[0]);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};

exports.seed = async (req, res) => {
  try {
    const db = await getDb();
    await db.execute('DELETE FROM bloques');

const bloques = [
  { bloque: 'A',     mapaX: 0.2790, mapaY: 0.2430, mapaWidth: 0.0590, mapaHeight: 0.2577 },
  { bloque: 'B',     mapaX: 0.3690, mapaY: 0.2415, mapaWidth: 0.0584, mapaHeight: 0.1156 },
  { bloque: 'C',     mapaX: 0.459,  mapaY: 0.241,  mapaWidth: 0.0658, mapaHeight: 0.259  },
  { bloque: 'D',     mapaX: 0.0733, mapaY: 0.3507, mapaWidth: 0.1623, mapaHeight: 0.2204 },
  { bloque: 'E',     mapaX: 0.0733, mapaY: 0.3507, mapaWidth: 0.0800, mapaHeight: 0.2204 },
  { bloque: 'F',     mapaX: 0.1533, mapaY: 0.3507, mapaWidth: 0.0800, mapaHeight: 0.2204 },
  { bloque: 'G',     mapaX: 0.2011, mapaY: 0.0708, mapaWidth: 0.0351, mapaHeight: 0.2814 }
];

    for (const b of bloques) {
      await db.execute({
        sql: 'INSERT INTO bloques (bloque, mapaX, mapaY, mapaWidth, mapaHeight) VALUES (?, ?, ?, ?, ?)',
        args: [b.bloque, b.mapaX, b.mapaY, b.mapaWidth, b.mapaHeight]
      });
    }

    res.json({ mensaje: 'Seed de bloques creado', cantidad: bloques.length });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
};