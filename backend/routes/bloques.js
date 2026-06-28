const express=require('express');
const router=express.Router();
const ctrl=require('../controllers/bloquesController');


// seed arriba
router.get('/seed', ctrl.seed);
router.get('/', ctrl.getAll);
router.get('/:bloque', ctrl.getByBloque);

module.exports=router;