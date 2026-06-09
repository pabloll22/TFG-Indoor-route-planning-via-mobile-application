const Usuario = require('../models/usuario_model');
const bcrypt = require('bcryptjs');

// Helper para sacar el ID del token
const obtenerIdUsuario = (req) => {
    return req.usuario.id || req.usuario._id || req.usuario.idUsuario;
};

// ==========================================
// 1. SUBIR / ACTUALIZAR FOTO DE PERFIL
// ==========================================
exports.subirFoto = async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ error: 'No se subió ninguna imagen' });
        }

        const urlFinal = req.file.secure_url;
        const idRealDelUsuario = obtenerIdUsuario(req);

        const usuarioActualizado = await Usuario.findByIdAndUpdate(
            idRealDelUsuario,
            { foto_url: urlFinal },
            { returnDocument: 'after' }
        );

        if (!usuarioActualizado) {
            return res.status(404).json({ error: 'Usuario no encontrado en la base de datos' });
        }

        console.log(`[ÉXITO] Foto subida permanentemente a Cloudinary: ${urlFinal}`);
        res.json({ mensaje: 'Foto actualizada permanentemente', url: urlFinal });

    } catch (error) {
        console.error("Error al guardar la foto en Cloudinary:", error);
        res.status(500).json({ error: 'Error interno del servidor al procesar la imagen' });
    }
};

// ==========================================
// 2. OBTENER TODOS LOS USUARIOS (Admin)
// ==========================================
exports.getTodosUsuarios = async (req, res) => {
    try {
        const todosLosUsuarios = await Usuario.find({}).select('-password');
        console.log(`[EXITO] Enviando ${todosLosUsuarios.length} usuarios al panel de administración.`);
        res.json(todosLosUsuarios);
    } catch (error) {
        console.error(`[ERROR] Fallo al cargar la lista de usuarios:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 3. CREAR UN NUEVO USUARIO (Admin)
// ==========================================
exports.crearUsuario = async (req, res) => {
    try {
        const { idUsuario, nombre, password, rol, foto_url } = req.body;

        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        const nuevoUsuario = new Usuario({
            idUsuario,
            nombre,
            password: hashedPassword,
            rol: rol || 'ALUMNO',
            foto_url: foto_url || ""
        });

        await nuevoUsuario.save();
        res.status(201).json({ message: "Usuario creado exitosamente" });
    } catch (error) {
        console.error(`[ERROR] Fallo al crear usuario:`, error);
        if (error.code === 11000) {
            return res.status(400).json({ error: "El ID de usuario ya existe en el sistema." });
        }
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 4. EDITAR UN USUARIO EXISTENTE (Admin)
// ==========================================
exports.editarUsuario = async (req, res) => {
    try {
        const { idUsuario } = req.params;
        const { nombre, password, rol, foto_url } = req.body;

        const datosAActualizar = { nombre, rol, foto_url };

        if (password && password.trim() !== "") {
            const salt = await bcrypt.genSalt(10);
            datosAActualizar.password = await bcrypt.hash(password, salt);
        }

        const usuarioActualizado = await Usuario.findOneAndUpdate(
            { idUsuario: idUsuario },
            { $set: datosAActualizar },
            { new: true }
        );

        if (!usuarioActualizado) {
            return res.status(404).json({ error: "Usuario no encontrado" });
        }

        res.status(200).json({ message: "Usuario actualizado correctamente" });
    } catch (error) {
        console.error(`[ERROR] Fallo al actualizar usuario:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 5. ELIMINAR UN USUARIO (Admin)
// ==========================================
exports.eliminarUsuario = async (req, res) => {
    try {
        const { idUsuario } = req.params;
        const resultado = await Usuario.deleteOne({ idUsuario: idUsuario });

        if (resultado.deletedCount === 0) {
            return res.status(404).json({ error: "Usuario no encontrado" });
        }

        res.status(200).json({ message: "Usuario eliminado correctamente" });
    } catch (error) {
        console.error(`[ERROR] Fallo al eliminar usuario:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// ACTUALIZAR PREFERENCIA DE ACCESIBILIDAD
// ==========================================
exports.actualizarAccesibilidad = async (req, res) => {
    try {
        const { idUsuario } = req.params;
        const { rutasAccesibles } = req.body;

        if (typeof rutasAccesibles !== 'boolean') {
            return res.status(400).json({ error: "El campo rutasAccesibles debe ser un booleano (true/false)" });
        }

        const usuarioActualizado = await Usuario.findOneAndUpdate(
            { idUsuario: idUsuario },
            { $set: { rutasAccesibles: rutasAccesibles } },
            { new: true }
        );

        if (!usuarioActualizado) {
            return res.status(404).json({ error: "Usuario no encontrado" });
        }

        res.status(200).json({ 
            mensaje: "Preferencia de accesibilidad guardada en BD", 
            rutasAccesibles: usuarioActualizado.rutasAccesibles 
        });

    } catch (error) {
        console.error("[ERROR] Fallo al actualizar la accesibilidad en BD:", error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};