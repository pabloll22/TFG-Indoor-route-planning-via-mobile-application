const express = require('express');
const router = express.Router();
const Mapa = require('./mapa_model'); // Importamos el esquema

// ENDPOINT: Obtener un mapa por su ID (ej. /api/mapas/etsi_planta_1)
router.get('/:id', async (req, res) => {
    try {
        const mapaId = req.params.id;
        // Buscamos en Mongo el documento que coincida con ese ID
        const mapaEncontrado = await Mapa.findOne({ mapaId: mapaId });

        if (mapaEncontrado) {
            console.log(`[EXITO] Mapa enviado: ${mapaId}`);
            res.json(mapaEncontrado);
        } else {
            console.log(`[AVISO] Mapa no encontrado: ${mapaId}`);
            res.status(404).json({ error: "Mapa no encontrado en la base de datos" });
        }
    } catch (error) {
        console.error(`[ERROR] Fallo en la base de datos:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
});

// ENDPOINT 1: Obtener la lista de todos los mapas (¡Versión ligera!)
router.get('/', async (req, res) => {
    try {
        // Buscamos todos los mapas ({}), pero usamos .select() para decirle a Mongo
        // que SOLO nos devuelva el mapaId y el nombre. 
        // ¡Así nos ahorramos descargar megas de imágenes Base64 y nodos para la lista!
        const listaMapas = await Mapa.find({}).select('mapaId nombre -_id');
        
        console.log(`[EXITO] Enviando lista de ${listaMapas.length} mapas al móvil.`);
        res.json(listaMapas);
    } catch (error) {
        console.error(`[ERROR] Fallo al cargar la lista:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
});

// Puedes añadir más en el futuro: router.post('/', ...) para crear mapas desde una web

module.exports = router;