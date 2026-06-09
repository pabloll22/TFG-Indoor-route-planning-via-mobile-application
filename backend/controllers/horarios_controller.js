const Usuario = require('../models/usuario_model');
const Sesion = require('../models/sesion_model');
const Asignatura = require('../models/asignatura_model');

// ==========================================
// 1. OBTENER HORARIO COMPLETO DE UN ALUMNO 
// ==========================================
exports.getHorarioAlumno = async (req, res) => {
    try {
        const { usuarioId } = req.params;
        const alumno = await Usuario.findOne({ idUsuario: usuarioId });
        if (!alumno) return res.status(404).json({ error: "Alumno no encontrado" });

        const condiciones = alumno.asignaturasMatriculadas.map(matricula => ({
            asignaturaId: matricula.asignaturaId,
            grupo: matricula.grupo
        }));

        if (condiciones.length === 0) return res.json([]); 

        const horarioCompleto = await Sesion.find({ $or: condiciones })
            .populate('asignaturaId') 
            .populate('profesorId', 'nombre') 
            .sort({ diaSemana: 1, horaInicio: 1 }); 

        console.log(`[EXITO] Enviando horario de ${horarioCompleto.length} clases al alumno ${alumno.nombre}`);
        res.json(horarioCompleto);
    } catch (error) {
        console.error(`[ERROR] Fallo al cargar el horario del alumno:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 2. OBTENER HORARIO COMPLETO DE UN PROFESOR
// ==========================================
exports.getHorarioProfesor = async (req, res) => {
    try {
        const { usuarioId } = req.params;
        const profesor = await Usuario.findOne({ idUsuario: usuarioId, rol: 'PROFESOR' });
        if (!profesor) return res.status(404).json({ error: "Profesor no encontrado" });

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
};

// ==========================================
// 3. WIDGET DASHBOARD: CLASES DE "HOY"
// ==========================================
exports.getClasesHoy = async (req, res) => {
    try {
        const { usuarioId } = req.params;
        const usuario = await Usuario.findOne({ idUsuario: usuarioId });
        if (!usuario) return res.status(404).json({ error: "Usuario no encontrado" });

        let diaActual = req.query.dia ? parseInt(req.query.dia) : new Date().getDay();
        let clasesHoy = [];

        if (usuario.rol === 'PROFESOR') {
            clasesHoy = await Sesion.find({ profesorId: usuario._id, diaSemana: diaActual })
                .populate('asignaturaId')
                .sort({ horaInicio: 1 }); 
        } else {
            const condiciones = usuario.asignaturasMatriculadas.map(matricula => ({
                asignaturaId: matricula.asignaturaId,
                grupo: matricula.grupo,
                diaSemana: diaActual
            }));

            if (condiciones.length > 0) {
                clasesHoy = await Sesion.find({ $or: condiciones })
                    .populate('asignaturaId')
                    .populate('profesorId', 'nombre')
                    .sort({ horaInicio: 1 });
            }
        }
        res.json(clasesHoy);
    } catch (error) {
        console.error(`[ERROR] Fallo al cargar las clases de hoy:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 4. AULA PÚBLICO
// ==========================================
exports.getClasesAula = async (req, res) => {
    try {
        const { nodoId } = req.params;
        let diaActual = req.query.dia ? parseInt(req.query.dia) : new Date().getDay();

        const clasesHoy = await Sesion.find({ nodoAulaId: nodoId, diaSemana: diaActual })
            .populate('asignaturaId')
            .populate('profesorId', 'nombre')
            .sort({ horaInicio: 1 }); 

        res.json(clasesHoy);
    } catch (error) {
        console.error(`[ERROR] Fallo al consultar el aula ${req.params.nodoId}:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 5. CANCELAR Y RESTAURAR SESIÓN
// ==========================================
exports.cancelarSesion = async (req, res) => {
    try {
        const { sesionId } = req.params;
        const { fecha } = req.body; 
        if (!fecha) return res.status(400).json({ error: "Falta la fecha a cancelar" });

        const sesionActualizada = await Sesion.findByIdAndUpdate(
            sesionId, { $addToSet: { fechasCanceladas: fecha } }, { new: true }
        );
        if (!sesionActualizada) return res.status(404).json({ error: "Sesión no encontrada" });

        res.json({ success: true, message: "Clase cancelada correctamente" });
    } catch (error) {
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

exports.restaurarSesion = async (req, res) => {
    try {
        const { sesionId } = req.params;
        const { fecha } = req.body;
        if (!fecha) return res.status(400).json({ error: "Falta la fecha a restaurar" });

        const sesionActualizada = await Sesion.findByIdAndUpdate(
            sesionId, { $pull: { fechasCanceladas: fecha } }, { new: true }
        );
        if (!sesionActualizada) return res.status(404).json({ error: "Sesión no encontrada" });

        res.json({ success: true, message: "Clase restaurada correctamente" });
    } catch (error) {
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 6. CREAR SESIÓN
// ==========================================
exports.crearSesion = async (req, res) => {
    try {
        const nuevaSesion = new Sesion(req.body);
        await nuevaSesion.save();
        const sesionCompleta = await Sesion.findById(nuevaSesion._id)
            .populate('asignaturaId')
            .populate('profesorId', 'nombre');
        res.status(201).json(sesionCompleta);
    } catch (error) {
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 7. USUARIOS (FAVORITOS, PERFIL, HORARIO UNIFICADO, MATRICULA)
// ==========================================
exports.sincronizarFavoritos = async (req, res) => {
    try {
        const usuarioActualizado = await Usuario.findOneAndUpdate(
            { idUsuario: req.params.idUsuario },
            { $set: { poisFavoritos: req.body.favoritos } },
            { new: true } 
        );
        if (!usuarioActualizado) return res.status(404).json({ error: 'Usuario no encontrado' }); 
        res.status(200).json({ mensaje: 'Favoritos actualizados', favoritos: usuarioActualizado.poisFavoritos });
    } catch (error) {
        res.status(500).json({ error: 'Error interno del servidor' });
    }
};

exports.getPerfilUsuario = async (req, res) => {
    try {
        const usuario = await Usuario.findOne({ idUsuario: req.params.idUsuario });
        if (!usuario) return res.status(404).json({ error: "Usuario no encontrado" });
        res.json(usuario);
    } catch (error) {
        res.status(500).json({ error: "Error al obtener usuario" });
    }
};

exports.getHorarioUnificado = async (req, res) => {
    try {
        const usuario = await Usuario.findOne({ idUsuario: req.params.usuarioId });
        if (!usuario) return res.status(404).json({ error: "Usuario no encontrado" });

        let horario = [];
        if (usuario.rol === 'PROFESOR') {
            horario = await Sesion.find({ profesorId: usuario._id })
                .populate('asignaturaId').populate('profesorId', 'nombre').sort({ diaSemana: 1, horaInicio: 1 });
        } else {
            const condiciones = usuario.asignaturasMatriculadas.map(matricula => ({
                asignaturaId: matricula.asignaturaId,
                grupo: matricula.grupo
            }));
            if (condiciones.length > 0) {
                horario = await Sesion.find({ $or: condiciones })
                    .populate('asignaturaId').populate('profesorId', 'nombre').sort({ diaSemana: 1, horaInicio: 1 });
            }
        }
        res.json(horario);
    } catch (error) { 
        res.status(500).json({ error: "Error interno del servidor" }); 
    }
};

exports.actualizarMatricula = async (req, res) => {
    try {
        const usuarioActualizado = await Usuario.findOneAndUpdate(
            { idUsuario: req.params.idUsuario },
            { $set: { asignaturasMatriculadas: req.body } },
            { new: true }
        );
        if (!usuarioActualizado) return res.status(404).json({ error: 'Usuario no encontrado' });
        res.status(200).json({ mensaje: 'Matrícula actualizada correctamente' });
    } catch (error) {
        res.status(500).json({ error: 'Error interno del servidor' });
    }
};

// ==========================================
// 8. ASIGNATURAS Y SESIONES (ADMIN)
// ==========================================
exports.getAsignaturas = async (req, res) => {
    try {
        const asignaturas = await Asignatura.find({}).lean();
        for (let asig of asignaturas) {
            const gruposUnicos = await Sesion.distinct('grupo', { asignaturaId: asig._id });
            asig.grupos = gruposUnicos.sort(); 
        }
        res.json(asignaturas);
    } catch (error) {
        res.status(500).json({ error: "Error al obtener asignaturas" });
    }
};

exports.crearAsignatura = async (req, res) => {
    try {
        const nuevaAsignatura = new Asignatura(req.body);
        await nuevaAsignatura.save();
        res.status(201).json(nuevaAsignatura);
    } catch (error) {
        res.status(500).json({ error: "Error al crear asignatura" });
    }
};

exports.borrarAsignatura = async (req, res) => {
    try {
        await Asignatura.findByIdAndDelete(req.params.id);
        await Sesion.deleteMany({ asignaturaId: req.params.id });
        res.json({ message: "Asignatura eliminada" });
    } catch (error) {
        res.status(500).json({ error: "Error al eliminar asignatura" });
    }
};

exports.editarAsignatura = async (req, res) => {
    try {
        const asignaturaActualizada = await Asignatura.findByIdAndUpdate(req.params.id, req.body, { new: true });
        res.json(asignaturaActualizada);
    } catch (error) {
        res.status(500).json({ error: "Error al actualizar asignatura" });
    }
};

exports.getSesionesAsignatura = async (req, res) => {
    try {
        const sesiones = await Sesion.find({ asignaturaId: req.params.asignaturaId })
            .populate('profesorId', 'nombre idUsuario'); 
        res.json(sesiones);
    } catch (error) {
        res.status(500).json({ error: "Error al obtener sesiones" });
    }
};

exports.borrarSesion = async (req, res) => {
    try {
        await Sesion.findByIdAndDelete(req.params.id);
        res.json({ message: "Sesión eliminada" });
    } catch (error) {
        res.status(500).json({ error: "Error al eliminar sesión" });
    }
};

exports.editarSesion = async (req, res) => {
    try {
        const sesionActualizada = await Sesion.findByIdAndUpdate(req.params.id, req.body, { new: true });
        if (!sesionActualizada) return res.status(404).json({ error: "Sesión no encontrada" });
        res.json(sesionActualizada);
    } catch (error) {
        res.status(500).json({ error: "Error al actualizar sesión" });
    }
};