const express = require('express');
const router = express.Router();
const verificarToken = require('../middleware/verificarToken');

// Importamos la lógica desde nuestro controlador
const authController = require('../controllers/auth_controller');

// ==========================================
// RUTAS DE AUTENTICACIÓN
// ==========================================

router.post('/registro', authController.registro);
router.post('/login', authController.login);
router.put('/cambiar-password/:idUsuario', verificarToken, authController.cambiarPassword);

module.exports = router;