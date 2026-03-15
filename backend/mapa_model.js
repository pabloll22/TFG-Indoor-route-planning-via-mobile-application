const mongoose = require('mongoose');

const poiSchema = new mongoose.Schema({
    id: { type: String, required: true },
    nombre: { type: String, required: true },
    nodoId: { type: String, required: true } // El nodo al que nos llevará el A*
}, { _id: false });

// Definimos la estructura de un Nodo
const nodoSchema = new mongoose.Schema({
    id: { type: String, required: true },
    name: { type: String, required: true },
    position: {
        x: { type: Number, required: true },
        y: { type: Number, required: true }
    },
    neighbors: [{ type: String }] // Array de IDs de otros nodos
}, { _id: false }); // _id: false evita que Mongo cree un ID interno por cada nodo

// Definimos la estructura principal del Mapa
const mapaSchema = new mongoose.Schema({
    mapaId: { type: String, required: true, unique: true },
    nombre: { type: String, required: true },
    // knownBeacons será un diccionario (Map) donde la clave es la MAC (String)
    // y el valor es un objeto con { x, y }
    knownBeacons: {
        type: Map,
        of: new mongoose.Schema({
            x: Number,
            y: Number
        }, { _id: false })
    },
    nodos: [nodoSchema],
    pois: [poiSchema]
});

// Exportamos el modelo para poder usarlo en otras partes de la app
module.exports = mongoose.model('Mapa', mapaSchema, 'mapas');