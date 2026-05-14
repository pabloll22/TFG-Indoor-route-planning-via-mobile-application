const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const Usuario = require('./horario/usuario_model'); 

// Esta clave se usa para firmar los tokens.
const JWT_SECRET = "clave_secreta_tfg_uma_2026"; 

// ==========================================
// ENDPOINT: REGISTRO
// ==========================================
router.post('/registro', async (req, res) => {
    try {
        const { idUsuario, nombre, password, rol } = req.body;

        // 1. Verificamos si el usuario ya existe
        const existe = await Usuario.findOne({ idUsuario });
        if (existe) return res.status(400).json({ error: "El usuario ya existe" });

        // 2. ENCRIPTAR CONTRASEÑA
        // El "salt" hace que la encriptación sea más segura
        const salt = await bcrypt.genSalt(10);
        const passwordHasheada = await bcrypt.hash(password, salt);

        // 3. Guardar en la BD
        const nuevoUsuario = new Usuario({
            idUsuario,
            nombre,
            password: passwordHasheada,
            rol: rol || 'ALUMNO'
        });

        await nuevoUsuario.save();
        res.status(201).json({ mensaje: "Usuario creado con éxito" });

    } catch (error) {
        res.status(500).json({ error: "Error al registrar usuario" });
    }
});

// ==========================================
// ENDPOINT: LOGIN
// ==========================================
router.post('/login', async (req, res) => {
    try {
        const { idUsuario, password } = req.body;

        // 1. ¿Existe el usuario?
        const usuario = await Usuario.findOne({ idUsuario });
        if (!usuario) return res.status(404).json({ error: "Credenciales inválidas" });

        // 2. COMPARAR CONTRASEÑAS
        // bcrypt compara la clave escrita con la encriptada en la BD
        const esValida = await bcrypt.compare(password, usuario.password);
        if (!esValida) return res.status(401).json({ error: "Credenciales inválidas" });

        // 3. GENERAR TOKEN JWT
        // Este token identifica al usuario en cada llamada posterior
        const token = jwt.sign(
            { id: usuario._id, rol: usuario.rol },
            JWT_SECRET,
            { expiresIn: '30d' } // El login dura 30 días
        );

        res.json({
            token,
            usuario: {
                idUsuario: usuario.idUsuario,
                nombre: usuario.nombre,
                rol: usuario.rol,
                poisFavoritos: usuario.poisFavoritos
            }
        });

    } catch (error) {
        res.status(500).json({ error: "Error al iniciar sesión" });
    }
});

module.exports = router;