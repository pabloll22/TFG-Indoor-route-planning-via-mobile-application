const mongoose = require('mongoose');

const usuarioSchema = new mongoose.Schema({
    idUsuario: { type: String, required: true, unique: true },
    nombre: { type: String, required: true },
    password: { type: String, required: true },
    rol: { type: String, enum: ['ALUMNO', 'PROFESOR', 'ADMIN'], default: 'ALUMNO' },
    asignaturasMatriculadas: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Asignatura' }],
    poisFavoritos: [{ type: String, default: [] }]
});

module.exports = mongoose.model('Usuario', usuarioSchema, 'usuarios');