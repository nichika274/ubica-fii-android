const router = require('express').Router();
const ctrl = require('../controllers/puntosInteresController');

router.get('/', ctrl.getAll);
router.post('/seed', ctrl.seed);   // <-- esta línea es clave

module.exports = router;