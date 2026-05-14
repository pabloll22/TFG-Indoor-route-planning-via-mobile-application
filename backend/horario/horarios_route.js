const express = require('express');
const router = express.Router();
const Usuario = require('./usuario_model');
const Sesion = require('./sesion_model');
const Asignatura = require('./asignatura_model');

const verificarToken = require('../middleware/verificarToken');

// ==========================================
// 1. OBTENER HORARIO COMPLETO DE UN ALUMNO (PROTEGIDO)
// ==========================================
router.get('/alumno/:usuarioId/horario', verificarToken, async (req, res) => {
    try {
        const { usuarioId } = req.params;

        const alumno = await Usuario.findOne({ idUsuario: usuarioId });
        if (!alumno) {
            return res.status(404).json({ error: "Alumno no encontrado" });
        }

        const susAsignaturas = alumno.asignaturasMatriculadas;

        const horarioCompleto = await Sesion.find({
            asignaturaId: { $in: susAsignaturas }
        })
        .populate('asignaturaId')
        .populate('profesorId', 'nombre')
        .sort({ diaSemana: 1, horaInicio: 1 });

        console.log(`[EXITO] Enviando horario de ${horarioCompleto.length} clases al alumno ${alumno.nombre}`);
        res.json(horarioCompleto);

    } catch (error) {
        console.error(`[ERROR] Fallo al cargar el horario del alumno:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
});

// ==========================================
// 2. OBTENER HORARIO COMPLETO DE UN PROFESOR (PROTEGIDO)
// ==========================================
router.get('/profesor/:usuarioId/horario', verificarToken, async (req, res) => {
    try {
        const { usuarioId } = req.params;

        const profesor = await Usuario.findOne({ idUsuario: usuarioId, rol: 'PROFESOR' });
        if (!profesor) {
            return res.status(404).json({ error: "Profesor no encontrado" });
        }

        const horarioCompleto = await Sesion.find({ profesorId: profesor._id })
        .populate('asignaturaId')
        .populate('profesorId', 'nombre')
        .sort({ diaSemana: 1, horaInicio: 1 });

        console.log(`[EXITO] Enviando horario al profesor ${profesor.nombre}`);
        res.json(horarioCompleto);

    } catch (error) {
        console.error(`[ERROR] Fallo al cargar el horario del profesor:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
});

// ==========================================
// 3. WIDGET DASHBOARD: CLASES DE "HOY" (PROTEGIDO)
// ==========================================
router.get('/hoy/:usuarioId', verificarToken, async (req, res) => {
    try {
        const { usuarioId } = req.params;
        const usuario = await Usuario.findOne({ idUsuario: usuarioId });

        if (!usuario) {
            return res.status(404).json({ error: "Usuario no encontrado" });
        }

        let diaActual = req.query.dia ? parseInt(req.query.dia) : new Date().getDay();
        let clasesHoy = [];

        if (usuario.rol === 'PROFESOR') {
            clasesHoy = await Sesion.find({ profesorId: usuario._id, diaSemana: diaActual })
                .populate('asignaturaId')
                .sort({ horaInicio: 1 });
        } else {
            clasesHoy = await Sesion.find({ asignaturaId: { $in: usuario.asignaturasMatriculadas }, diaSemana: diaActual })
                .populate('asignaturaId')
                .populate('profesorId', 'nombre')
                .sort({ horaInicio: 1 });
        }

        console.log(`[EXITO] Enviando ${clasesHoy.length} clases de hoy para ${usuario.nombre}`);
        res.json(clasesHoy);

    } catch (error) {
        console.error(`[ERROR] Fallo al cargar las clases de hoy:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
});

// ==========================================
//  PÚBLICO: Invitado puede verlo
// ==========================================
router.get('/aula/:nodoId', async (req, res) => {
    try {
        const { nodoId } = req.params;

        let diaActual = req.query.dia ? parseInt(req.query.dia) : new Date().getDay();

        const clasesHoy = await Sesion.find({
            nodoAulaId: nodoId,
            diaSemana: diaActual
        })
        .populate('asignaturaId')
        .populate('profesorId', 'nombre')
        .sort({ horaInicio: 1 });

        res.json(clasesHoy);
    } catch (error) {
        console.error(`[ERROR] Fallo al consultar el aula ${req.params.nodoId}:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
});

// ==========================================
// 5. CANCELAR UNA CLASE UN DÍA ESPECÍFICO (PROTEGIDO)
// ==========================================
router.post('/sesion/:sesionId/cancelar', verificarToken, async (req, res) => {
    console.log(`[TEST] Han intentado cancelar la clase con ID: ${req.params.sesionId} para el día ${req.body.fecha}`);
    try {
        const { sesionId } = req.params;
        const { fecha } = req.body;

        if (!fecha) {
            return res.status(400).json({ error: "Falta la fecha a cancelar" });
        }

        const sesionActualizada = await Sesion.findByIdAndUpdate(
            sesionId,
            { $addToSet: { fechasCanceladas: fecha } },
            { new: true }
        );

        if (!sesionActualizada) {
            return res.status(404).json({ error: "Sesión no encontrada" });
        }

        console.log(`[EXITO] Clase ${sesionId} cancelada para el día ${fecha}`);
        res.json({ success: true, message: "Clase cancelada correctamente" });

    } catch (error) {
        console.error(`[ERROR] Fallo al cancelar la clase:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
});

// ==========================================
// 6. RESTAURAR UNA CLASE CANCELADA (PROTEGIDO)
// ==========================================
router.post('/sesion/:sesionId/restaurar', verificarToken, async (req, res) => {
    try {
        const { sesionId } = req.params;
        const { fecha } = req.body;

        if (!fecha) {
            return res.status(400).json({ error: "Falta la fecha a restaurar" });
        }

        const sesionActualizada = await Sesion.findByIdAndUpdate(
            sesionId,
            { $pull: { fechasCanceladas: fecha } },
            { new: true }
        );

        if (!sesionActualizada) {
            return res.status(404).json({ error: "Sesión no encontrada" });
        }

        console.log(`[EXITO] Clase ${sesionId} RESTAURADA para el día ${fecha}`);
        res.json({ success: true, message: "Clase restaurada correctamente" });

    } catch (error) {
        console.error(`[ERROR] Fallo al restaurar la clase:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
});

// ==========================================
// 7. CREAR UNA NUEVA CLASE (PROTEGIDO)
// ==========================================
router.post('/sesion/crear', verificarToken, async (req, res) => {
    try {
        const nuevaSesion = new Sesion(req.body);
        await nuevaSesion.save();

        const sesionCompleta = await Sesion.findById(nuevaSesion._id)
            .populate('asignaturaId')
            .populate('profesorId', 'nombre');

        console.log(`[EXITO] Nueva clase creada: ${nuevaSesion._id}`);
        res.status(201).json(sesionCompleta);

    } catch (error) {
        console.error(`[ERROR] Fallo al crear la clase:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
});

// ==========================================
// 8. SINCRONIZAR FAVORITOS (PROTEGIDO)
// ==========================================
router.put('/usuarios/:idUsuario/favoritos', verificarToken, async (req, res) => {
    try {
        const { idUsuario } = req.params;
        const { favoritos } = req.body;

        const usuarioActualizado = await Usuario.findOneAndUpdate(
            { idUsuario: idUsuario },
            { $set: { poisFavoritos: favoritos } },
            { new: true }
        );

        if (!usuarioActualizado) {
            return res.status(404).json({ error: 'Usuario no encontrado' });
        }

        res.status(200).json({
            mensaje: 'Favoritos actualizados',
            favoritos: usuarioActualizado.poisFavoritos
        });

    } catch (error) {
        console.error(`[ERROR] Fallo al actualizar favoritos del usuario ${req.params.idUsuario}:`, error);
        res.status(500).json({ error: 'Error interno del servidor' });
    }
});

// ==========================================
// 9. OBTENER PERFIL DE USUARIO (PROTEGIDO)
// ==========================================
router.get('/usuarios/:idUsuario', verificarToken, async (req, res) => {
    try {
        const usuario = await Usuario.findOne({ idUsuario: req.params.idUsuario });
        if (!usuario) return res.status(404).json({ error: "Usuario no encontrado" });

        res.json(usuario);
    } catch (error) {
        res.status(500).json({ error: "Error al obtener usuario" });
    }
});

// ==========================================
// 10. OBTENER HORARIO UNIFICADO (PROTEGIDO)
// ==========================================
router.get('/usuario/:usuarioId/horario', verificarToken, async (req, res) => {
    try {
        const usuario = await Usuario.findOne({ idUsuario: req.params.usuarioId });
        if (!usuario) return res.status(404).json({ error: "Usuario no encontrado" });

        let horario = [];
        if (usuario.rol === 'PROFESOR') {
            horario = await Sesion.find({ profesorId: usuario._id })
                .populate('asignaturaId').populate('profesorId', 'nombre').sort({ diaSemana: 1, horaInicio: 1 });
        } else {
            horario = await Sesion.find({ asignaturaId: { $in: usuario.asignaturasMatriculadas } })
                .populate('asignaturaId').populate('profesorId', 'nombre').sort({ diaSemana: 1, horaInicio: 1 });
        }
        res.json(horario);
    } catch (error) {
        console.error(`[ERROR] Fallo al cargar el horario del usuario:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
});

module.exports = router;