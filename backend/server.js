const express = require('express');
const cors = require('cors');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const db = require('./database'); // ◄ Importa la base de datos (Paso 4)

const app = express();
const PORT = 3001;

// Asegurar que la carpeta 'uploads' exista para evitar errores al subir archivos
if (!fs.existsSync('uploads')) {
  fs.mkdirSync('uploads');
}

app.use(cors());
app.use(express.json()); // ◄ ¡Importante! Permite procesar JSON en las peticiones

// Servir archivos estáticos (imágenes subidas)
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// Configurar almacenamiento de imágenes con Multer
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

// --- RUTAS API (CONECTADAS A LA BASE DE DATOS) ---

// GET todos los espacios (con filtros)
app.get('/api/espacios', (req, res) => {
  const { piso, tipo, q } = req.query;
  let query = 'SELECT * FROM espacios WHERE 1=1';
  const params = [];

  if (piso) {
    query += ' AND piso = ?';
    params.push(parseInt(piso));
  }
  if (tipo) {
    query += ' AND tipo = ?';
    params.push(tipo);
  }
  if (q) {
    query += ' AND nombre LIKE ?';
    params.push(`%${q}%`);
  }

  try {
    const espacios = db.prepare(query).all(...params);
    res.json(espacios);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// GET espacio por ID
app.get('/api/espacios/:id', (req, res) => {
  try {
    const espacio = db.prepare('SELECT * FROM espacios WHERE id = ?').get(req.params.id);
    if (espacio) {
      res.json(espacio);
    } else {
      res.status(404).json({ mensaje: 'Espacio no encontrado' });
    }
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// POST crear espacio (Los datos vienen en JSON enviados por tu App)
app.post('/api/espacios', (req, res) => {
  const { nombre, tipo, piso, descripcion, fotoUrl, indicaciones, bloque, coordenadaX, coordenadaY } = req.body;

  try {
    const stmt = db.prepare(`
      INSERT INTO espacios (nombre, tipo, piso, descripcion, fotoUrl, indicaciones, bloque, coordenadaX, coordenadaY)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    `);

    const info = stmt.run(
      nombre,
      tipo,
      parseInt(piso),
      descripcion || '',
      fotoUrl || '',
      indicaciones || '',
      bloque,
      coordenadaX || 0.5,
      coordenadaY || 0.5
    );

    const nuevo = db.prepare('SELECT * FROM espacios WHERE id = ?').get(info.lastInsertRowid);
    res.status(201).json(nuevo);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// PUT actualizar espacio
app.put('/api/espacios/:id', (req, res) => {
  const { nombre, tipo, piso, descripcion, fotoUrl, indicaciones, bloque, coordenadaX, coordenadaY } = req.body;

  try {
    const stmt = db.prepare(`
      UPDATE espacios
      SET nombre=?, tipo=?, piso=?, descripcion=?, fotoUrl=?, indicaciones=?, bloque=?, coordenadaX=?, coordenadaY=?
      WHERE id=?
    `);

    stmt.run(nombre, tipo, parseInt(piso), descripcion, fotoUrl, indicaciones, bloque, coordenadaX, coordenadaY, req.params.id);

    const actualizado = db.prepare('SELECT * FROM espacios WHERE id = ?').get(req.params.id);
    if (actualizado) {
      res.json(actualizado);
    } else {
      res.status(404).json({ mensaje: 'Espacio no encontrado' });
    }
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DELETE eliminar espacio
app.delete('/api/espacios/:id', (req, res) => {
  try {
    const resultado = db.prepare('DELETE FROM espacios WHERE id = ?').run(req.params.id);
    if (resultado.changes > 0) {
      res.status(204).send();
    } else {
      res.status(404).json({ mensaje: 'Espacio no encontrado' });
    }
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// POST subir imagen de forma independiente y devolver la URL pública
app.post('/api/upload', upload.single('foto'), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ error: 'No se ha adjuntado ningún archivo.' });
  }

  // Construye la URL pública del archivo subido
  const url = `${req.protocol}://${req.get('host')}/uploads/${req.file.filename}`;
  res.json({ url });
});

// Iniciar servidor
app.listen(PORT, '0.0.0.0', () => {
  console.log(`✅ Backend corriendo en http://0.0.0.0:${PORT}`);
  console.log(`📱 Emulador Android puede usar: http://10.0.2.2:${PORT}`);
  console.log(`💻 Navegador: http://localhost:${PORT}`);
});