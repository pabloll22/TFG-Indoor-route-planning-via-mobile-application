const express = require('express');
const router = express.Router();
const multer = require('multer');
const verificarToken = require('../middleware/verificarToken');

const cloudinary = require('cloudinary').v2;
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
    params: {
        folder: 'tfg_perfiles', // Nombre de la carpeta que se creará en tu panel de Cloudinary
        allowed_formats: ['jpg', 'png', 'jpeg'], // Formatos permitidos
        public_id: (req, file) => {
            // Sacamos el ID para nombrar el archivo en la nube de forma única
            const id = req.usuario.id || req.usuario._id || req.usuario.idUsuario || 'usuario';
            return `${id}_${Date.now()}`;
        }
    }
});

const upload = multer({ storage: storage });

// ==========================================
// RUTAS DE USUARIOS
// ==========================================

// Subir foto de perfil (upload.single envía la foto directamente a Cloudinary)
router.post('/foto', verificarToken, upload.single('foto'), usuarioController.subirFoto);

// Gestión de usuarios (Panel Admin)
router.get('/todos', usuarioController.getTodosUsuarios);
router.post('/', usuarioController.crearUsuario);
router.put('/:idUsuario', usuarioController.editarUsuario);
router.delete('/:idUsuario', usuarioController.eliminarUsuario);
router.put('/:idUsuario/accesibilidad', verificarToken, usuarioController.actualizarAccesibilidad);

module.exports = router;