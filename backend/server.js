const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');

const app = express();
const PORT = process.env.PORT || 3001;

// Asegurar que exista la carpeta de subidas
if (!fs.existsSync('uploads')) fs.mkdirSync('uploads');

// Middlewares
app.use(cors());
app.use(express.json());
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// Rutas de la API
app.use('/api/espacios', require('./routes/espacios'));
app.use('/api/bloques', require('./routes/bloques'));
app.use('/api/upload', require('./routes/upload'));
app.use('/api/puntos-interes', require('./routes/puntosInteres'));

// Inicialización del servidor
app.listen(PORT, '0.0.0.0', () => {
  console.log(`✅ Backend corriendo en el puerto ${PORT}`);
});