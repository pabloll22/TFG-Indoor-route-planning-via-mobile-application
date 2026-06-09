const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');
const verificarToken = require('../middleware/verificarToken');

// Importamos el controlador
const usuarioController = require('../controllers/usuario_controller');

// Pequeño helper necesario para configurar Multer aquí
const obtenerIdUsuario = (req) => {
    return req.usuario.id || req.usuario._id || req.usuario.idUsuario;
};

// Configuración de Multer
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, 'uploads/perfiles/');
    },
    filename: (req, file, cb) => {
        const id = obtenerIdUsuario(req);
        const ext = path.extname(file.originalname);
        cb(null, `${id}_${Date.now()}${ext}`);
    }
});
const upload = multer({ storage: storage });

// ==========================================
// RUTAS DE USUARIOS
// ==========================================

// Subir foto de perfil (Requiere token y el middleware de Multer)
router.post('/foto', verificarToken, upload.single('foto'), usuarioController.subirFoto);

// Gestión de usuarios (Panel Admin)
router.get('/todos', usuarioController.getTodosUsuarios);
router.post('/', usuarioController.crearUsuario);
router.put('/:idUsuario', usuarioController.editarUsuario);
router.delete('/:idUsuario', usuarioController.eliminarUsuario);
router.put('/:idUsuario/accesibilidad', verificarToken, usuarioController.actualizarAccesibilidad);

module.exports = router;