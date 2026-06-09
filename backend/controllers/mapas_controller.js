const Mapa = require('../models/mapa_model');

// ==========================================
// 1. OBTENER UN MAPA POR SU ID
// ==========================================
exports.getMapaById = async (req, res) => {
    try {
        const mapaId = req.params.id;
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
};

// ==========================================
// 2. OBTENER LISTA DE TODOS LOS MAPAS
// ==========================================
exports.getMapas = async (req, res) => {
    try {
        // Seleccionamos todo EXCEPTO el array 'plantas'
        const listaMapas = await Mapa.find({}).select('-plantas');
        
        console.log(`[EXITO] Enviando lista de ${listaMapas.length} mapas al móvil.`);
        res.json(listaMapas);
    } catch (error) {
        console.error(`[ERROR] Fallo al cargar la lista:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 3. CREAR NUEVO EDIFICIO (MAPA)
// ==========================================
exports.crearMapa = async (req, res) => {
    try {
        const { mapaId, nombre, dimensiones, urlWeb } = req.body;

        const existe = await Mapa.findOne({ mapaId: mapaId });
        if (existe) {
            return res.status(400).json({ error: "Ya existe un edificio con ese código ID" });
        }

        const nuevoMapa = new Mapa({
            mapaId,
            nombre,
            dimensiones: {
                ancho: dimensiones.ancho,
                largo: dimensiones.largo
            },
            urlWeb: urlWeb || "",
            plantas: [] 
        });

        await nuevoMapa.save();
        console.log(`[EXITO] Nuevo edificio creado: ${nombre} (${mapaId})`);
        res.status(201).json(nuevoMapa);

    } catch (error) {
        console.error(`[ERROR] Fallo al crear el edificio:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 4. EDITAR UN EDIFICIO EXISTENTE
// ==========================================
exports.editarMapa = async (req, res) => {
    try {
        const { mapaId } = req.params;
        const { nombre, dimensiones, urlWeb } = req.body;

        const edificioActualizado = await Mapa.findOneAndUpdate(
            { mapaId: mapaId },
            { 
                $set: { 
                    nombre: nombre, 
                    dimensiones: dimensiones,
                    urlWeb: urlWeb 
                } 
            },
            { new: true }
        );

        if (!edificioActualizado) {
            return res.status(404).json({ error: "Edificio no encontrado" });
        }

        res.status(200).json({ message: "Edificio actualizado correctamente" });
    } catch (error) {
        console.error(`[ERROR] Fallo al actualizar edificio:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 5. ELIMINAR UN EDIFICIO COMPLETO
// ==========================================
exports.eliminarMapa = async (req, res) => {
    try {
        const { mapaId } = req.params;
        const resultado = await Mapa.deleteOne({ mapaId: mapaId });

        if (resultado.deletedCount === 0) {
            return res.status(404).json({ error: "Edificio no encontrado" });
        }

        res.status(200).json({ message: "Edificio eliminado correctamente" });
    } catch (error) {
        console.error(`[ERROR] Fallo al eliminar edificio:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 6. AÑADIR UNA PLANTA AL EDIFICIO
// ==========================================
exports.crearPlanta = async (req, res) => {
    try {
        const { mapaId } = req.params;
        const nuevaPlanta = req.body;

        nuevaPlanta.nodos = [];
        nuevaPlanta.pois = [];
        nuevaPlanta.knownBeacons = {};

        const resultado = await Mapa.updateOne(
            { mapaId: mapaId },
            { $push: { plantas: nuevaPlanta } }
        );

        res.status(201).json({ message: "Planta añadida correctamente" });
    } catch (error) {
        console.error(`[ERROR] Fallo al añadir planta:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 7. ELIMINAR UNA PLANTA
// ==========================================
exports.eliminarPlanta = async (req, res) => {
    try {
        const { mapaId, plantaId } = req.params;

        const resultado = await Mapa.updateOne(
            { mapaId: mapaId },
            { $pull: { plantas: { plantaId: plantaId } } }
        );

        if (resultado.modifiedCount === 0) {
            return res.status(404).json({ error: "Planta no encontrada" });
        }

        res.status(200).json({ message: "Planta eliminada correctamente" });
    } catch (error) {
        console.error(`[ERROR] Fallo al eliminar planta:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 8. ACTUALIZAR NODOS DE UNA PLANTA
// ==========================================
exports.actualizarNodos = async (req, res) => {
    try {
        const { mapaId, plantaId } = req.params;
        const { nodos } = req.body;

        const resultado = await Mapa.updateOne(
            { mapaId: mapaId, "plantas.plantaId": plantaId },
            { $set: { "plantas.$.nodos": nodos } } 
        );

        if (resultado.modifiedCount === 0) {
            return res.status(400).json({ error: "No se encontró la planta o no hubo cambios." });
        }

        res.status(200).json({ message: "Nodos actualizados correctamente" });
    } catch (error) {
        console.error(`[ERROR] Fallo al guardar nodos:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 9. ACTUALIZAR POIS DE UNA PLANTA
// ==========================================
exports.actualizarPOIs = async (req, res) => {
    try {
        const { mapaId, plantaId } = req.params;
        const { pois } = req.body;

        const resultado = await Mapa.updateOne(
            { mapaId: mapaId, "plantas.plantaId": plantaId },
            { $set: { "plantas.$.pois": pois } } 
        );

        res.status(200).json({ message: "POIs actualizados correctamente" });
    } catch (error) {
        console.error(`[ERROR] Fallo al guardar POIs:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};

// ==========================================
// 10. ACTUALIZAR BEACONS DE UNA PLANTA
// ==========================================
exports.actualizarBeacons = async (req, res) => {
    try {
        const { mapaId, plantaId } = req.params;
        const { beacons } = req.body;

        await Mapa.updateOne(
            { mapaId: mapaId, "plantas.plantaId": plantaId },
            { $set: { "plantas.$.knownBeacons": beacons } }
        );

        res.status(200).json({ message: "Beacons actualizados correctamente" });
    } catch (error) {
        console.error(`[ERROR] Fallo al guardar beacons:`, error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
};