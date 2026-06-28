// seed_espacios.js
const { getDb, saveDb } = require('./config/db'); // <-- Ajusta la ruta según dónde tengas tu config de BD

async function ejecutarSeed() {
  console.log("🌱 Iniciando el seed independiente de espacios...");
  try {
    const db = await getDb();

    console.log("🗑️ Reseteando tabla 'espacios'...");
    await db.run("DROP TABLE IF EXISTS espacios");
    await db.run(`
      CREATE TABLE espacios (
        id INTEGER PRIMARY KEY,
        nombre TEXT,
        tipo TEXT,
        piso INTEGER,
        descripcion TEXT,
        fotoUrl TEXT,
        indicaciones TEXT,
        bloque TEXT,
        coordenadaX REAL,
        coordenadaY REAL
      )
    `);

    const espacios = [
      // ... AQUÍ PEGAS TU ARRAY COMPLETO DE ~80 ESPACIOS QUE ME MOSTRASTE ANTES ...
      { id: 1, nombre: 'Administración de edificio facu', tipo: 'Oficina', piso: 0, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      // ... (agrega todos los demás objetos)
    ];

    console.log(`📥 Insertando ${espacios.length} espacios...`);
    
    for (const e of espacios) {
      await db.run(
        `INSERT INTO espacios
        (id, nombre, tipo, piso, descripcion, fotoUrl, indicaciones, bloque, coordenadaX, coordenadaY)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
        [
          e.id,
          e.nombre,
          e.tipo,
          e.piso,
          e.descripcion || "",
          e.fotoUrl || "",
          e.indicaciones || "",
          e.bloque,
          e.coordenadaX ?? 0.5,
          e.coordenadaY ?? 0.3
        ]
      );
    }

    await saveDb();
    console.log("✅ Base de datos poblada con éxito.");
    process.exit(0); // Cierra el script correctamente
  } catch (error) {
    console.error("❌ ERROR EJECUTANDO EL SEED:", error);
    process.exit(1); // Cierra con código de error
  }
}

ejecutarSeed();