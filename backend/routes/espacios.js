const express = require('express');
const router = express.Router();
const ctrl = require('../controllers/espaciosController');

// Rutas específicas antes de las parametrizadas
router.post('/restaurar-datos', ctrl.restaurarDatos);
//router.post('/renombrar-d', ctrl.renombrarBloqueD);
// router.post('/seed', ctrl.seed); // ← opcional si la necesitas

// CRUD
router.get('/', ctrl.getAll);
router.get('/:id', ctrl.getById);
router.post('/', ctrl.create);
router.put('/:id', ctrl.update);
router.delete('/:id', ctrl.delete); // o ctrl.remove, según tu exportación

module.exports = router;