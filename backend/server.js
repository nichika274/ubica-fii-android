const express = require('express');
const cors = require('cors');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { getDb, saveDb } = require('./database');

const app = express();
const PORT = process.env.PORT || 3001;

if (!fs.existsSync('uploads')) {
  fs.mkdirSync('uploads');
}

app.use(cors());
app.use(express.json());
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, 'uploads/');
  },
  filename: function (req, file, cb) {
    const uniqueName = Date.now() + '-' + path.basename(file.originalname);
    cb(null, uniqueName);
  }
});
const upload = multer({ storage: storage });

const formatFotoUrl = (fotoUrl, req) => {
  if (!fotoUrl) return '';
  const baseUrl = `${req.protocol}://${req.get('host')}`;
  if (fotoUrl.startsWith('http')) {
    return fotoUrl.replace(/^https?:\/\/[^\/]+/, baseUrl);
  }
  return `${baseUrl}${fotoUrl}`;
};

// --- RUTAS API (ADAPTADAS A SQL.JS ASÍNCRONO) ---

// GET todos los espacios (con filtros)
app.get('/api/espacios', async (req, res) => {
  const { piso, tipo, q } = req.query;

  try {
    const db = await getDb();
    let query = 'SELECT * FROM espacios WHERE 1=1';
    const params = {};

    if (piso) {
      query += ' AND piso = :piso';
      params[':piso'] = parseInt(piso);
    }
    if (tipo) {
      query += ' AND tipo = :tipo';
      params[':tipo'] = tipo;
    }
    if (q) {
      query += ' AND nombre LIKE :q';
      params[':q'] = `%${q}%`;
    }

    const stmt = db.prepare(query);
    stmt.bind(params);

    const espacios = [];
    while (stmt.step()) {
      espacios.push(stmt.getAsObject());
    }
    stmt.free();

    const result = espacios.map(e => ({
      ...e,
      fotoUrl: formatFotoUrl(e.fotoUrl, req)
    }));

    res.json(result);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// GET espacio por ID
app.get('/api/espacios/:id', async (req, res) => {
  try {
    const db = await getDb();
    const stmt = db.prepare('SELECT * FROM espacios WHERE id = :id');
    stmt.bind({ ':id': req.params.id });

    let espacio = null;
    if (stmt.step()) {
      espacio = stmt.getAsObject();
    }
    stmt.free();

    if (espacio && espacio.id) {
      espacio.fotoUrl = formatFotoUrl(espacio.fotoUrl, req);
      res.json(espacio);
    } else {
      res.status(404).json({ mensaje: 'Espacio no encontrado' });
    }
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// POST crear espacio
app.post('/api/espacios', async (req, res) => {
  const { nombre, tipo, piso, descripcion, fotoUrl, indicaciones, bloque, coordenadaX, coordenadaY } = req.body;

  try {
    const db = await getDb();
    const stmt = db.prepare(`
      INSERT INTO espacios (nombre, tipo, piso, descripcion, fotoUrl, indicaciones, bloque, coordenadaX, coordenadaY)
      VALUES (:nombre, :tipo, :piso, :descripcion, :fotoUrl, :indicaciones, :bloque, :coordenadaX, :coordenadaY)
    `);

    stmt.run({
      ':nombre': nombre,
      ':tipo': tipo,
      ':piso': parseInt(piso),
      ':descripcion': descripcion || '',
      ':fotoUrl': fotoUrl || '',
      ':indicaciones': indicaciones || '',
      ':bloque': bloque,
      ':coordenadaX': coordenadaX || 0.5,
      ':coordenadaY': coordenadaY || 0.5
    });
    stmt.free();

    // Guardar cambios en el almacenamiento persistente
    await saveDb();

    // Obtener el último ID insertado usando la función last_insert_rowid() de SQLite
    const idStmt = db.prepare('SELECT * FROM espacios WHERE id = last_insert_rowid()');
    let nuevo = null;
    if (idStmt.step()) {
      nuevo = idStmt.getAsObject();
    }
    idStmt.free();

    if (nuevo) {
      nuevo.fotoUrl = formatFotoUrl(nuevo.fotoUrl, req);
    }

    res.status(201).json(nuevo);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// PUT actualizar espacio
app.put('/api/espacios/:id', async (req, res) => {
  const { nombre, tipo, piso, descripcion, fotoUrl, indicaciones, bloque, coordenadaX, coordenadaY } = req.body;

  try {
    const db = await getDb();
    const stmt = db.prepare(`
      UPDATE espacios
      SET nombre = :nombre, tipo = :tipo, piso = :piso, descripcion = :descripcion,
          fotoUrl = :fotoUrl, indicaciones = :indicaciones, bloque = :bloque,
          coordenadaX = :coordenadaX, coordenadaY = :coordenadaY
      WHERE id = :id
    `);

    stmt.run({
      ':nombre': nombre,
      ':tipo': tipo,
      ':piso': parseInt(piso),
      ':descripcion': descripcion,
      ':fotoUrl': fotoUrl,
      ':indicaciones': indicaciones,
      ':bloque': bloque,
      ':coordenadaX': coordenadaX,
      ':coordenadaY': coordenadaY,
      ':id': req.params.id
    });
    stmt.free();

    // Guardar cambios en el almacenamiento persistente
    await saveDb();

    // Buscar el elemento actualizado para responder
    const checkStmt = db.prepare('SELECT * FROM espacios WHERE id = :id');
    checkStmt.bind({ ':id': req.params.id });
    let actualizado = null;
    if (checkStmt.step()) {
      actualizado = checkStmt.getAsObject();
    }
    checkStmt.free();

    if (actualizado && actualizado.id) {
      actualizado.fotoUrl = formatFotoUrl(actualizado.fotoUrl, req);
      res.json(actualizado);
    } else {
      res.status(404).json({ mensaje: 'Espacio no encontrado' });
    }
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DELETE eliminar espacio
app.delete('/api/espacios/:id', async (req, res) => {
  try {
    const db = await getDb();

    // Verificamos primero si existe para poder simular las filas afectadas
    const checkStmt = db.prepare('SELECT id FROM espacios WHERE id = :id');
    checkStmt.bind({ ':id': req.params.id });
    const existe = checkStmt.step();
    checkStmt.free();

    if (!existe) {
      return res.status(404).json({ mensaje: 'Espacio no encontrado' });
    }

    const stmt = db.prepare('DELETE FROM espacios WHERE id = :id');
    stmt.run({ ':id': req.params.id });
    stmt.free();

    // Guardar cambios en el almacenamiento persistente
    await saveDb();

    res.status(204).send();
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// POST subir imagen de forma independiente y devolver la URL pública
app.post('/api/upload', upload.single('foto'), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ error: 'No se ha adjuntado ningún archivo.' });
  }

  const url = `${req.protocol}://${req.get('host')}/uploads/${req.file.filename}`;
  res.json({ url });
});
// Endpoint para sembrar la base de datos (puedes visitarlo en el navegador: /api/seed)
// Endpoint para sembrar la base de datos (visita /api/seed)
app.get('/api/seed', async (req, res) => {
  try {
    const db = await getDb();

    // 1. Asegurar que la tabla exista
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
        coordenadaY REAL DEFAULT 0.5
      )
    `);

    // 2. Limpiar la tabla para evitar duplicados
    db.run("DELETE FROM espacios");

    // 3. Datos completos
    const espacios = [
      // Planta baja (piso 0)
      { nombre: 'Administración de edificio facu', tipo: 'Oficina', piso: 0, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Dirección de carrera', tipo: 'Oficina', piso: 0, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Departamento de talentos humanos', tipo: 'Oficina', piso: 0, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Sala de docentes', tipo: 'Oficina', piso: 0, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Área de investigación', tipo: 'Oficina', piso: 0, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Decanato', tipo: 'Oficina', piso: 0, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Sub decanato', tipo: 'Oficina', piso: 0, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Secretaría', tipo: 'Oficina', piso: 0, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Sala de conferencias', tipo: 'Oficina', piso: 0, bloque: 'B', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Laboratorio física 1 (14b-001)', tipo: 'Laboratorio', piso: 0, bloque: 'B', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Baños Bloque B (Planta baja)', tipo: 'Baño', piso: 0, bloque: 'B', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Laboratorio física 2 (14b-001)', tipo: 'Laboratorio', piso: 0, bloque: 'B', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14C-004', tipo: 'Aula', piso: 0, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14C-007', tipo: 'Aula', piso: 0, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14C-006', tipo: 'Aula', piso: 0, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14C-005', tipo: 'Aula', piso: 0, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Baños Bloque C (Planta baja)', tipo: 'Baño', piso: 0, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14C-003', tipo: 'Aula', piso: 0, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Fueiss', tipo: 'Oficina', piso: 0, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-001', tipo: 'Aula', piso: 0, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-002', tipo: 'Aula', piso: 0, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-003', tipo: 'Aula', piso: 0, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-004', tipo: 'Aula', piso: 0, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-005', tipo: 'Aula', piso: 0, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-006', tipo: 'Aula', piso: 0, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-007', tipo: 'Aula', piso: 0, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-008', tipo: 'Aula', piso: 0, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },

      // Primer piso (piso 1)
      { nombre: 'Baños Bloque A (Piso 1)', tipo: 'Baño', piso: 1, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Laboratorio de cómputo 14A-101', tipo: 'Laboratorio', piso: 1, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Instituto de posgrado e investigación educación continua', tipo: 'Oficina', piso: 1, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14B-101', tipo: 'Aula', piso: 1, bloque: 'B', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Laboratorio de cómputo 14B-101', tipo: 'Laboratorio', piso: 1, bloque: 'B', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Formación académica', tipo: 'Oficina', piso: 1, bloque: 'B', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14B-102', tipo: 'Aula', piso: 1, bloque: 'B', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14B-103', tipo: 'Aula', piso: 1, bloque: 'B', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14C-101', tipo: 'Aula', piso: 1, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14C-102', tipo: 'Aula', piso: 1, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Laboratorio de cómputo 14C-101', tipo: 'Laboratorio', piso: 1, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Laboratorio de cómputo 14C-102', tipo: 'Laboratorio', piso: 1, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Baños Bloque C (Piso 1)', tipo: 'Baño', piso: 1, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Secretaría', tipo: 'Oficina', piso: 1, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14C-103', tipo: 'Aula', piso: 1, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Vinculación con la sociedad y bienestar estudiantil', tipo: 'Oficina', piso: 1, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Sala de profesores', tipo: 'Oficina', piso: 1, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Departamento técnico', tipo: 'Oficina', piso: 1, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-101', tipo: 'Aula', piso: 1, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-102', tipo: 'Aula', piso: 1, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-103', tipo: 'Aula', piso: 1, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-104', tipo: 'Aula', piso: 1, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-105', tipo: 'Aula', piso: 1, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-106', tipo: 'Aula', piso: 1, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-107', tipo: 'Aula', piso: 1, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14D-108', tipo: 'Aula', piso: 1, bloque: 'D', descripcion: '', fotoUrl: '', indicaciones: '' },

      // Segundo piso (piso 2)
      { nombre: 'Sala de docentes (Piso 2)', tipo: 'Oficina', piso: 2, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14A-203', tipo: 'Aula', piso: 2, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14A-204', tipo: 'Aula', piso: 2, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14A-205', tipo: 'Aula', piso: 2, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14A-206', tipo: 'Aula', piso: 2, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Sala de reuniones y tutorías de telemática', tipo: 'Oficina', piso: 2, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Baños Bloque A (Piso 2)', tipo: 'Baño', piso: 2, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Laboratorio de cómputo 14A-201', tipo: 'Laboratorio', piso: 2, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14A-201', tipo: 'Aula', piso: 2, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Dirección de carrera de telemática', tipo: 'Oficina', piso: 2, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Laboratorio de cómputo 14A-202', tipo: 'Laboratorio', piso: 2, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14A-202', tipo: 'Aula', piso: 2, bloque: 'A', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Laboratorio de cómputo 14B-201', tipo: 'Laboratorio', piso: 2, bloque: 'B', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14B-202', tipo: 'Aula', piso: 2, bloque: 'B', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Laboratorio de vinci de proyecto', tipo: 'Laboratorio', piso: 2, bloque: 'B', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14B-201', tipo: 'Aula', piso: 2, bloque: 'B', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Laboratorio de cómputo 14B-202', tipo: 'Laboratorio', piso: 2, bloque: 'B', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14C-201', tipo: 'Aula', piso: 2, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14C-202', tipo: 'Aula', piso: 2, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Laboratorio de cómputo 14C-201', tipo: 'Laboratorio', piso: 2, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Laboratorio de cómputo 14C-202', tipo: 'Laboratorio', piso: 2, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Baños Bloque C (Piso 2)', tipo: 'Baño', piso: 2, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14C-205', tipo: 'Aula', piso: 2, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Aula 14C-206', tipo: 'Aula', piso: 2, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Laboratorio de cómputo 14C-203', tipo: 'Laboratorio', piso: 2, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' },
      { nombre: 'Laboratorio de cómputo 14C-204', tipo: 'Laboratorio', piso: 2, bloque: 'C', descripcion: '', fotoUrl: '', indicaciones: '' }
    ];

    // 4. Insertar todos los registros
    const stmt = db.prepare(`
      INSERT INTO espacios (nombre, tipo, piso, descripcion, fotoUrl, indicaciones, bloque, coordenadaX, coordenadaY)
      VALUES (:nombre, :tipo, :piso, :descripcion, :fotoUrl, :indicaciones, :bloque, :coordenadaX, :coordenadaY)
    `);

    for (const e of espacios) {
      stmt.run({
        ':nombre': e.nombre,
        ':tipo': e.tipo,
        ':piso': e.piso,
        ':descripcion': e.descripcion || '',
        ':fotoUrl': e.fotoUrl || '',
        ':indicaciones': e.indicaciones || '',
        ':bloque': e.bloque,
        ':coordenadaX': 0.5,
        ':coordenadaY': 0.3
      });
    }
    stmt.free();

    // 5. Guardar los cambios
    await saveDb();

    res.send(`✅ Base de datos sembrada con ${espacios.length} espacios.`);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});
// Iniciar servidor
app.listen(PORT, '0.0.0.0', () => {
  console.log(`✅ Backend corriendo en el puerto: ${PORT}`);
});