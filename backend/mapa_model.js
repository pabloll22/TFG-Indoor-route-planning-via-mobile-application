const mongoose = require('mongoose');

// 1. POI
const poiSchema = new mongoose.Schema({
    id: { type: String, required: true },
    nombre: { type: String, required: true },
    nodoId: { type: String, required: true },
    plantaId: { type: String, required: true }
}, { _id: false });

// 2. NODO
const nodoSchema = new mongoose.Schema({
    id: { type: String, required: true },
    name: { type: String, required: true },
    plantaId: { type: String, required: true },
    position: {
        x: { type: Number, required: true },
        y: { type: Number, required: true }
    },
    neighbors: [{ type: String }]
}, { _id: false });

// ESQUEMA DE PLANTA (El nuevo contenedor)
const plantaSchema = new mongoose.Schema({
    plantaId: { type: String, required: true },
    nombre: { type: String, required: true },
    nivel: { type: Number, required: true },
    imagenMapa: { type: String, required: true },

    // Movemos los diccionarios y arrays AQUÍ DENTRO
    knownBeacons: {
        type: Map,
        of: new mongoose.Schema({
            x: Number,
            y: Number
        }, { _id: false })
    },
    nodos: [nodoSchema],
    pois: [poiSchema]
}, { _id: false });

// 4. MAPA (Representa al EDIFICIO COMPLETO)
const mapaSchema = new mongoose.Schema({
    mapaId: { type: String, required: true, unique: true },
    nombre: { type: String, required: true },

    // El edificio ahora es simplemente una lista de plantas
    plantas: [plantaSchema]
});

// Exportamos el modelo para poder usarlo en otras partes de la app
module.exports = mongoose.model('Mapa', mapaSchema, 'mapas');