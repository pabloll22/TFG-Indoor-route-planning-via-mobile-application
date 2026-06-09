const express = require('express');
const router = express.Router();

// Importamos la lógica desde nuestro controlador
const mapasController = require('../controllers/mapas_controller');

// ==========================================
// RUTAS DE EDIFICIOS (MAPAS)
// ==========================================
router.get('/', mapasController.getMapas);
router.post('/', mapasController.crearMapa);
router.get('/:id', mapasController.getMapaById);
router.put('/:mapaId', mapasController.editarMapa);
router.delete('/:mapaId', mapasController.eliminarMapa);

// ==========================================
// RUTAS DE PLANTAS
// ==========================================
router.post('/:mapaId/plantas', mapasController.crearPlanta);
router.delete('/:mapaId/plantas/:plantaId', mapasController.eliminarPlanta);

// ==========================================
// RUTAS DE ELEMENTOS DE LA PLANTA (EDITOR)
// ==========================================
router.put('/:mapaId/plantas/:plantaId/nodos', mapasController.actualizarNodos);
router.put('/:mapaId/plantas/:plantaId/pois', mapasController.actualizarPOIs);
router.put('/:mapaId/plantas/:plantaId/beacons', mapasController.actualizarBeacons);

module.exports = router;