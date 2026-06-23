const express = require('express');
const router = express.Router();

// Importamos la lógica desde nuestro controlador
const verificarToken = require('../middleware/verificarToken');
const mapasController = require('../controllers/mapas_controller');

// ==========================================
// RUTAS DE EDIFICIOS (MAPAS)
// ==========================================
router.get('/', mapasController.getMapas);
router.get('/:id', mapasController.getMapaById);

router.post('/', verificarToken, mapasController.crearMapa);
router.put('/:mapaId', verificarToken, mapasController.editarMapa);
router.delete('/:mapaId', verificarToken, mapasController.eliminarMapa);

// ==========================================
// RUTAS DE PLANTAS
// ==========================================
router.post('/:mapaId/plantas', verificarToken, mapasController.crearPlanta);
router.delete('/:mapaId/plantas/:plantaId', verificarToken, mapasController.eliminarPlanta);

// ==========================================
// RUTAS DE ELEMENTOS DE LA PLANTA (EDITOR)
// ==========================================
router.put('/:mapaId/plantas/:plantaId/nodos', verificarToken, mapasController.actualizarNodos);
router.put('/:mapaId/plantas/:plantaId/pois', verificarToken,  mapasController.actualizarPOIs);
router.put('/:mapaId/plantas/:plantaId/beacons', verificarToken, mapasController.actualizarBeacons);

module.exports = router;