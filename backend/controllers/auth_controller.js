require('dotenv').config();
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const Usuario = require('../models/usuario_model'); 

// Esta clave se usa para firmar los tokens.
const JWT_SECRET = process.env.JWT_SECRET; 

// ==========================================
// REGISTRO DE USUARIO
// ==========================================
exports.registro = async (req, res) => {
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
};

// ==========================================
// LOGIN DE USUARIO
// ==========================================
exports.login = async (req, res) => {
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
                poisFavoritos: usuario.poisFavoritos,
                foto_url: usuario.foto_url || "",
                rutasAccesibles: usuario.rutasAccesibles || false
            }
        });

    } catch (error) {
        res.status(500).json({ error: "Error al iniciar sesión" });
    }
};

// ==========================================
// CAMBIAR CONTRASEÑA
// ==========================================
exports.cambiarPassword = async (req, res) => {
    try {
        // Obtenemos el ID de la URL y las contraseñas del body de la petición
        const { idUsuario } = req.params;
        const { passwordAntigua, passwordNueva } = req.body;

        // 1. Buscamos al usuario en la base de datos
        const usuario = await Usuario.findOne({ idUsuario });
        if (!usuario) {
            return res.status(404).json({ error: "Usuario no encontrado" });
        }

        // 2. VERIFICAR LA CONTRASEÑA ANTIGUA
        // bcrypt coge la clave que ha escrito el usuario y la compara con la encriptada de la BD
        const esValida = await bcrypt.compare(passwordAntigua, usuario.password);
        if (!esValida) {
            // Error 401 (Unauthorized) si se ha equivocado de contraseña
            return res.status(401).json({ error: "La contraseña antigua es incorrecta" });
        }

        // 3. ENCRIPTAR LA NUEVA CONTRASEÑA
        const salt = await bcrypt.genSalt(10);
        const nuevaPasswordHasheada = await bcrypt.hash(passwordNueva, salt);

        // 4. GUARDAR LOS CAMBIOS
        usuario.password = nuevaPasswordHasheada;
        await usuario.save();

        res.status(200).json({ mensaje: "Contraseña actualizada correctamente" });

    } catch (error) {
        console.error("[ERROR] Fallo al cambiar la contraseña:", error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};