const express = require('express');
const router = express.Router();
const multer = require('multer');
const verificarToken = require('../middleware/verificarToken');

const cloudinary = require('cloudinary');
const multerCloudinary = require('multer-storage-cloudinary');
const CloudinaryStorage = multerCloudinary.CloudinaryStorage || multerCloudinary;

// Importamos el controlador
const usuarioController = require('../controllers/usuario_controller');

cloudinary.config({
    cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
    api_key: process.env.CLOUDINARY_API_KEY,
    api_secret: process.env.CLOUDINARY_API_SECRET
});

// Configuración del almacenamiento en la nube
const storage = new CloudinaryStorage({
    cloudinary: cloudinary,
    params: async (req, file) => {
        console.log("🚀 Iniciando subida a Cloudinary...");

        return {
            folder: 'tfg_perfiles',
            allowed_formats: ['jpg', 'png', 'jpeg']
            //public_id: 'foto_prueba_' + Date.now()
        };
    }
});

const upload = multer({ storage: storage });

// ==========================================
// RUTAS DE USUARIOS
// ==========================================

// Subir foto de perfil (upload.single envía la foto directamente a Cloudinary)
//router.post('/foto', verificarToken, upload.single('foto'), usuarioController.subirFoto);

router.post('/foto', verificarToken, (req, res, next) => {
    const middlewareSubida = upload.single('foto');

    middlewareSubida(req, res, function (err) {
        if (err) {
            console.error("❌ ERROR REAL DE CLOUDINARY:", JSON.stringify(err, null, 2));
            if (err.message) console.error("Detalle:", err.message);
            return res.status(500).json({ error: "Fallo al subir imagen", detalles: err });
        }
        // Si no hay error, pasamos al controlador
        next();
    });
}, usuarioController.subirFoto);

// Gestión de usuarios (Panel Admin)
router.get('/todos', usuarioController.getTodosUsuarios);
router.post('/', usuarioController.crearUsuario);
router.put('/:idUsuario', usuarioController.editarUsuario);
router.delete('/:idUsuario', usuarioController.eliminarUsuario);
router.put('/:idUsuario/accesibilidad', verificarToken, usuarioController.actualizarAccesibilidad);

module.exports = router;