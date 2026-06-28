const { createClient } = require('@libsql/client');

const client = createClient({
  url: 'libsql://ubicafii-nichika274.aws-us-east-1.turso.io',
  authToken: 'eyJhbGciOiJFZERTQSIsInR5cCI6IkpXVCJ9.eyJhIjoicnciLCJpYXQiOjE3ODI1NjgzNTIsImlkIjoiMDE5ZjA5NTktMDAwMS03ZDk5LWEwMTQtZGU2ODBkYjVmNjJmIiwicmlkIjoiZjExY2M5MTEtYTlmZS00NmI1LWFjNjAtZGNkZDU0ZWRmYTQzIn0.vSoJ_nuJYOocSbrWKWAclDrbzoQ25Z6iZQskxwe0uuh-8Xf2OIC_aanve3uamO5_anuedeNjNkHkI7qEvRzYAA'
});

async function getDb() {
  // Crear tabla espacios
  await client.execute(`
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

  // Crear tabla bloques
  await client.execute(`
    CREATE TABLE IF NOT EXISTS bloques (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      bloque TEXT NOT NULL UNIQUE,
      mapaX REAL NOT NULL,
      mapaY REAL NOT NULL,
      mapaWidth REAL NOT NULL,
      mapaHeight REAL NOT NULL
    )
  `);

  // Crear tabla puntos_interes
  await client.execute(`
    CREATE TABLE IF NOT EXISTS puntos_interes (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      nombre TEXT NOT NULL,
      tipo TEXT NOT NULL,
      mapaX REAL NOT NULL,
      mapaY REAL NOT NULL
    )
  `);

  return client;
}

// En Turso no necesitas saveDb
async function saveDb() {}

module.exports = { getDb, saveDb };