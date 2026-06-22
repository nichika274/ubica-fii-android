const initSqlJs = require('sql.js');
const fs = require('fs');
const path = require('path');

const DB_PATH = path.join(__dirname, 'ubicafii.db');

let db;

async function getDb() {
  if (db) return db;
  const SQL = await initSqlJs();
  if (fs.existsSync(DB_PATH)) {
    const fileBuffer = fs.readFileSync(DB_PATH);
    db = new SQL.Database(fileBuffer);
  } else {
    db = new SQL.Database();
  }
  // Crear tabla si no existe
  db.run(`
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
  return db;
}

// Guardar cambios en disco
async function saveDb() {
  const d = await getDb();
  const data = d.export();
  const buffer = Buffer.from(data);
  fs.writeFileSync(DB_PATH, buffer);
}

module.exports = { getDb, saveDb };