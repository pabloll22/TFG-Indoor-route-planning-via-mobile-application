const express = require('express');
const router = express.Router();
const verificarToken = require('../middleware/verificarToken');

// Importamos TODA la lógica desde nuestro controlador
const horariosController = require('../controllers/horarios_controller');

// ==========================================
// RUTAS HORARIOS ALUMNO Y PROFESOR (MÓVIL)
// ==========================================
router.get('/alumno/:usuarioId/horario', verificarToken, horariosController.getHorarioAlumno);
router.get('/profesor/:usuarioId/horario', verificarToken, horariosController.getHorarioProfesor);
router.get('/hoy/:usuarioId', verificarToken, horariosController.getHorarioProfesor); // Dashboard widget

// ==========================================
// RUTAS PÚBLICAS Y SESIONES INDIVIDUALES
// ==========================================
router.get('/aula/:nodoId', horariosController.getClasesAula);
router.post('/sesion/:sesionId/cancelar', verificarToken, horariosController.cancelarSesion);
router.post('/sesion/:sesionId/restaurar', verificarToken, horariosController.restaurarSesion);
router.post('/sesion/crear', horariosController.crearSesion); // Sin verificarToken
router.delete('/sesiones/:id', horariosController.borrarSesion);
router.put('/sesiones/:id', horariosController.editarSesion);

// ==========================================
// RUTAS DE USUARIOS
// ==========================================
router.put('/usuarios/:idUsuario/favoritos', verificarToken, horariosController.sincronizarFavoritos);
router.get('/usuarios/:idUsuario', verificarToken, horariosController.getPerfilUsuario);
router.get('/usuario/:usuarioId/horario', verificarToken, horariosController.getHorarioUnificado);
router.put('/usuarios/:idUsuario/matricula', verificarToken, horariosController.actualizarMatricula);

// ==========================================
// RUTAS ADMINISTRACIÓN (PANEL WEB)
// ==========================================
router.get('/asignaturas', horariosController.getAsignaturas);
router.post('/asignaturas', horariosController.crearAsignatura);
router.put('/asignaturas/:id', horariosController.editarAsignatura);
router.delete('/asignaturas/:id', horariosController.borrarAsignatura);
router.get('/asignaturas/:asignaturaId/sesiones', horariosController.getSesionesAsignatura);

module.exports = router;