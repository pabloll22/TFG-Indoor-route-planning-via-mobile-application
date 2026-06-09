require('dotenv').config();
const jwt = require('jsonwebtoken');

const SECRET_KEY = process.env.JWT_SECRET; 

const verificarToken = (req, res, next) => {
    // 1. Buscamos la cabecera "Authorization"
    const authHeader = req.headers['authorization'];
    
    // El formato es "Bearer eyJhbGc...", así que lo partimos por el espacio y cogemos la parte 2
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        return res.status(401).json({ error: "Acceso denegado." });
    }

    try {
        // 2. Comprobamos si el token es real y no ha caducado
        const verificado = jwt.verify(token, SECRET_KEY);
        
        // 3. Si es válido, guardamos los datos del usuario en la petición (req)
        // para que la ruta sepa quién está llamando.
        req.usuario = verificado; 
        
        // 4. Le dejamos pasar a la ruta final
        next(); 
    } catch (error) {
        res.status(403).json({ error: "Token no válido o caducado." });
    }
};

module.exports = verificarToken;