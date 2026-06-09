const mongoose = require('mongoose');

const asignaturaSchema = new mongoose.Schema({
    nombre: { type: String, required: true },
    curso: { type: Number },
    titulacion: { type: String },
    cuatrimestre: { type: Number, required: true, enum: [1, 2] },
    facultadId: { type: String, required: true }
});

module.exports = mongoose.model('Asignatura', asignaturaSchema, 'asignaturas');