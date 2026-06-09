const mongoose = require('mongoose');

const usuarioSchema = new mongoose.Schema({
    idUsuario: { type: String, required: true, unique: true },
    nombre: { type: String, required: true },
    password: { type: String, required: true },
    rol: { type: String, enum: ['ALUMNO', 'PROFESOR', 'ADMIN'], default: 'ALUMNO' },
    asignaturasMatriculadas: [{
        _id: false, 
        asignaturaId: { type: mongoose.Schema.Types.ObjectId, ref: 'Asignatura', required: true },
        grupo: { type: String, required: true }
    }],
    poisFavoritos: [{
        _id: false,
        idPoi: { type: String, required: true },
        nombrePoi: { type: String, required: true },
        facultad: { type: String, default: "UMA" }
    }],
    foto_url: {
        type: String,
        default: ""
    },
    rutasAccesibles: { type: Boolean, default: false }
});

module.exports = mongoose.model('Usuario', usuarioSchema, 'usuarios');