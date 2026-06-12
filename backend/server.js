const express = require('express');
const cors = require('cors');
const app = express();
const PORT = 3001;

app.use(cors());
app.use(express.json());

// Datos de ejemplo (simula la base de datos)
let espacios = [
  {
    id: 1,
    nombre: "Aula 101",
    tipo: "aula",
    piso: 1,
    descripcion: "Aula con proyector y pizarra digital.",
    fotoUrl: "https://via.placeholder.com/400?text=Aula+101",
    indicaciones: "Primer piso, girar a la derecha al salir de las escaleras.",
    bloque: "Bloque A"
  },
  {
    id: 2,
    nombre: "Laboratorio 201",
    tipo: "laboratorio",
    piso: 2,
    descripcion: "Laboratorio de cómputo con 30 equipos.",
    fotoUrl: "https://via.placeholder.com/400?text=Lab+201",
    indicaciones: "Segundo piso, frente al ascensor.",
    bloque: "Bloque B"
  },
  {
    id: 3,
    nombre: "Biblioteca Central",
    tipo: "biblioteca",
    piso: 1,
    descripcion: "Biblioteca principal con sala de lectura.",
    fotoUrl: "https://via.placeholder.com/400?text=Biblioteca",
    indicaciones: "Al fondo del pasillo principal.",
    bloque: "Bloque A"
  },
  {
    id: 4,
    nombre: "Auditorio Magno",
    tipo: "auditorio",
    piso: 1,
    descripcion: "Auditorio con capacidad para 200 personas.",
    fotoUrl: "https://via.placeholder.com/400?text=Auditorio",
    indicaciones: "Junto a la entrada principal.",
    bloque: "Bloque C"
  }
];

// GET /api/espacios (con filtros)
app.get('/api/espacios', (req, res) => {
  const { piso, tipo, q } = req.query;
  let resultado = espacios;
  if (piso) resultado = resultado.filter(e => e.piso == parseInt(piso));
  if (tipo) resultado = resultado.filter(e => e.tipo.toLowerCase() === tipo.toLowerCase());
  if (q) resultado = resultado.filter(e => e.nombre.toLowerCase().includes(q.toLowerCase()));
  res.json(resultado);
});

// GET /api/espacios/:id
app.get('/api/espacios/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const espacio = espacios.find(e => e.id === id);
  if (espacio) {
    res.json(espacio);
  } else {
    res.status(404).json({ mensaje: 'Espacio no encontrado' });
  }
});

// POST /api/espacios
app.post('/api/espacios', (req, res) => {
  const { nombre, tipo, piso, descripcion, fotoUrl, indicaciones, bloque } = req.body;
  const nuevoId = espacios.length > 0 ? Math.max(...espacios.map(e => e.id)) + 1 : 1;
  const nuevo = { id: nuevoId, nombre, tipo, piso, descripcion, fotoUrl, indicaciones, bloque };
  espacios.push(nuevo);
  res.status(201).json(nuevo);
});

// PUT /api/espacios/:id
app.put('/api/espacios/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const index = espacios.findIndex(e => e.id === id);
  if (index !== -1) {
    espacios[index] = { ...espacios[index], ...req.body, id };
    res.json(espacios[index]);
  } else {
    res.status(404).json({ mensaje: 'Espacio no encontrado' });
  }
});

// DELETE /api/espacios/:id
app.delete('/api/espacios/:id', (req, res) => {
  const id = parseInt(req.params.id);
  const index = espacios.findIndex(e => e.id === id);
  if (index !== -1) {
    espacios.splice(index, 1);
    res.status(204).send();
  } else {
    res.status(404).json({ mensaje: 'Espacio no encontrado' });
  }
});

// Iniciar servidor
app.listen(PORT, '0.0.0.0', () => {
  console.log(`✅ Backend corriendo en http://0.0.0.0:${PORT}`);
  console.log(`📱 Emulador Android puede usar: http://10.0.2.2:${PORT}`);
  console.log(`💻 Navegador: http://localhost:${PORT}`);
});