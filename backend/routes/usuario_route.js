const express = require('express');
const router = express.Router();
const multer = require('multer');
const verificarToken = require('../middleware/verificarToken');

// Cloudinary
const cloudinary = require('cloudinary').v2;

const multerCloudinary = require('multer-storage-cloudinary');
const CloudinaryStorage = multerCloudinary.CloudinaryStorage || multerCloudinary;

// Importamos el controlador
const usuarioController = require('../controllers/usuario_controller');

// ===================================================
// CONFIGURACIÓN CLOUDINARY
// ===================================================

console.log("====================================");
console.log("CONFIGURACIÓN CLOUDINARY");
console.log("====================================");
console.log("CLOUD_NAME:", process.env.CLOUDINARY_CLOUD_NAME);
console.log("API_KEY:", process.env.CLOUDINARY_API_KEY ? "OK" : "NO ENCONTRADA");
console.log("API_SECRET:", process.env.CLOUDINARY_API_SECRET ? "OK" : "NO ENCONTRADA");
console.log("====================================");

cloudinary.config({
    cloud_name: process.env.CLOUDINARY_CLOUD_NAME,
    api_key: process.env.CLOUDINARY_API_KEY,
    api_secret: process.env.CLOUDINARY_API_SECRET
});

// Comprobación de conectividad con Cloudinary
(async () => {
    try {
        const result = await cloudinary.api.ping();
        console.log("✅ CLOUDINARY PING OK:", result);
    } catch (err) {
        console.error("❌ CLOUDINARY PING ERROR:");
        console.error(err);
    }
})();

// ===================================================
// STORAGE CLOUDINARY
// ===================================================

const storage = new CloudinaryStorage({
    cloudinary: cloudinary,
    params: async (req, file) => {

        console.log("🚀 INICIANDO SUBIDA A CLOUDINARY");
        console.log("Archivo:", file.originalname);

        return {
            folder: 'tfg_perfiles',
            allowed_formats: ['jpg', 'jpeg', 'png']
        };
    }
});

const upload = multer({
    storage
});

// ===================================================
// RUTA DE TEST
// ===================================================

router.get('/test-cloudinary', async (req, res) => {
    try {
        const result = await cloudinary.api.ping();

        console.log("✅ TEST CLOUDINARY:", result);

        res.json(result);

    } catch (error) {

        console.error("❌ ERROR TEST CLOUDINARY:");
        console.error(error);

        res.status(500).json(error);
    }
});

// ===================================================
// SUBIDA DE FOTO
// ===================================================

router.post(
    '/foto',
    verificarToken,

    (req, res, next) => {
        console.log("1️⃣ Entrando en ruta /foto");
        next();
    },

    (req, res, next) => {

        console.log("2️⃣ Ejecutando upload.single('foto')");

        const middlewareSubida = upload.single('foto');

        middlewareSubida(req, res, function (err) {

            if (err) {

                console.error("❌ ERROR DURANTE UPLOAD:");

                console.error(JSON.stringify(err, null, 2));

                if (err.message) {
                    console.error("Mensaje:", err.message);
                }

                return res.status(500).json({
                    error: "Fallo al subir imagen",
                    detalles: err
                });
            }

            console.log("3️⃣ Upload terminado correctamente");

            if (req.file) {
                console.log("Información del archivo:");
                console.log(req.file);
            } else {
                console.log("⚠️ req.file es NULL");
            }

            next();
        });
    },

    (req, res, next) => {
        console.log("4️⃣ Entrando al controlador");
        next();
    },

    usuarioController.subirFoto
);

// ===================================================
// GESTIÓN DE USUARIOS
// ===================================================

router.get('/todos', usuarioController.getTodosUsuarios);
router.post('/', usuarioController.crearUsuario);
router.put('/:idUsuario', usuarioController.editarUsuario);
router.delete('/:idUsuario', usuarioController.eliminarUsuario);

router.put(
    '/:idUsuario/accesibilidad',
    verificarToken,
    usuarioController.actualizarAccesibilidad
);

module.exports = router;