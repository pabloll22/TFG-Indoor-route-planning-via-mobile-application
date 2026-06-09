const mongoose = require('mongoose');

// 1. POI
const poiSchema = new mongoose.Schema({
    id: { type: String, required: true },
    nombre: { type: String, required: true },
    nodoId: { type: String, required: true },
    plantaId: { type: String, required: true },
    tipo: { 
        type: String, 
        enum: ['AULA', 'LABORATORIO', 'ASEO', 'CAFETERIA', 'SECRETARIA', 'BIBLIOTECA', 'CONSERJERIA', 'SALON_ACTOS', 'OTRO'],
        default: 'OTRO' 
    },
    horario: { type: String, default: null }, // Ej: "09:00 - 14:00"
    telefono: { type: String, default: null }, // Ej: "952 13 14 15"
    esAccesible: { type: Boolean, default: null }, // Para baños o accesos
    enlaceExtra: { type: String, default: null }, // URL para Cita Previa o Menú
    capacidad: { type: Number, default: null } // Para Biblioteca o Salón de Actos 
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
    neighbors: [{ type: String }],
    tipo: { 
        type: String, 
        enum: ['NORMAL', 'ESCALERA', 'ASCENSOR'], 
        default: 'NORMAL' 
    }
}, { _id: false });

// 3. ESQUEMA DE PLANTA
const plantaSchema = new mongoose.Schema({
    plantaId: { type: String, required: true },   
    nombre: { type: String, required: true },     
    nivel: { type: Number, required: true },     
    imagenBase64: { type: String, required: true }, 
    
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

// 4. MAPA (EDIFICIO COMPLETO)
const mapaSchema = new mongoose.Schema({
    mapaId: { type: String, required: true, unique: true },
    nombre: { type: String, required: true },
    urlWeb: { type: String, default: null },

    dimensiones: {
        ancho: { type: Number, required: true, default: 0 },
        largo: { type: Number, required: true, default: 0 }
    },
    
    // El edificio es simplemente una lista de plantas
    plantas: [plantaSchema] 
});

module.exports = mongoose.model('Mapa', mapaSchema, 'mapas');