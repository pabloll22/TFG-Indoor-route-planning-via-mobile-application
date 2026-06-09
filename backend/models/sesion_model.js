const mongoose = require('mongoose');

const sesionSchema = new mongoose.Schema({
    asignaturaId: { type: mongoose.Schema.Types.ObjectId, ref: 'Asignatura', required: true },
    profesorId: { type: mongoose.Schema.Types.ObjectId, ref: 'Usuario', required: true },
    diaSemana: { type: Number, required: true }, // 1 = Lunes, 5 = Viernes
    horaInicio: { type: String, required: true }, // Ej: "09:30"
    horaFin: { type: String, required: true },    // Ej: "11:30"
    nodoAulaId: { type: String, required: true } ,
    aulaNombre: { type: String, required: true },
    grupo: { type: String, required: true },
    fechasCanceladas: [{ type: String, default: [] }],
    fechaEspecifica: { 
        type: String, 
        default: null 
    },
});

module.exports = mongoose.model('Sesion', sesionSchema, 'sesiones');