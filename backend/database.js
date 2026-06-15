const Database = require('better-sqlite3');
const db = new Database('ubicafii.db');

// Crear tabla si no existe
db.exec(`
  CREATE TABLE IF NOT EXISTS espacios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    tipo TEXT NOT NULL,
    piso INTEGER NOT NULL,
    descripcion TEXT,
    fotoUrl TEXT,
    indicaciones TEXT,
    bloque TEXT NOT NULL,
    coordenadaX REAL DEFAULT 0.5,
    coordenadaY REAL DEFAULT 0.3
  )
`);

module.exports = db;