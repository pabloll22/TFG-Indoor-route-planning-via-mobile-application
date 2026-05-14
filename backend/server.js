const express = require('express');
const mongoose = require('mongoose');
const mapasRoutes = require('./mapas_routes'); // Importamos las rutas
const horariosRoutes = require('./horario/horarios_route');
const authRoutes = require('./auth_routes');

const app = express();
const port = 3000;

// URL base de datos MongoDB local
const mongoURI = 'mongodb+srv://tfg_db_user:1234@cluster0.mjqbqvl.mongodb.net/Cluster0?appName=Cluster0';

// 1. Conexión a MongoDB usando Mongoose
mongoose.connect(mongoURI)
    .then(() => console.log('✅ Conectado a la base de datos MongoDB (tfg_indoor)'))
    .catch(err => console.error('❌ Error al conectar a MongoDB:', err));

// 2. Middlewares (Para que entienda JSON si le enviamos datos)
app.use(express.json());

// 3. Montar las rutas
app.use('/api/mapas', mapasRoutes);
app.use('/api/horarios', horariosRoutes);
app.use('/api/auth', authRoutes);

// 4. Arrancar el servidor
app.listen(port, '0.0.0.0', () => {
    console.log(`===================================================`);
    console.log(`🚀 Servidor Node.js corriendo en el puerto ${port}`);
    console.log(`===================================================`);
});