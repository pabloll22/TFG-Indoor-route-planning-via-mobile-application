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
router.post('/sesion/crear', verificarToken, horariosController.crearSesion);
router.delete('/sesiones/:id', verificarToken, horariosController.borrarSesion);
router.put('/sesiones/:id', verificarToken, horariosController.editarSesion);

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
router.get('/asignaturas/:asignaturaId/sesiones', horariosController.getSesionesAsignatura);

router.post('/asignaturas', verificarToken, horariosController.crearAsignatura);
router.put('/asignaturas/:id', verificarToken, horariosController.editarAsignatura);
router.delete('/asignaturas/:id', verificarToken, horariosController.borrarAsignatura);


module.exports = router;