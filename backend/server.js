require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const mapasRoutes = require('./routes/mapas_routes'); // Importamos las rutas
const horariosRoutes = require('./routes/horarios_route');
const authRoutes = require('./routes/auth_routes');
const usuarioRoutes = require('./routes/usuario_route')

const app = express();
const port = process.env.PORT||3000;

// URL de la base de datos MongoDB
const mongoURI = process.env.MONGO_URI;

// 1. Conexión a MongoDB usando Mongoose
mongoose.connect(mongoURI)
    .then(() => console.log('✅ Conectado a la base de datos MongoDB (tfg_indoor)'))
    .catch(err => console.error('❌ Error al conectar a MongoDB:', err));

const dir = './uploads';
if (!fs.existsSync(dir)){
    fs.mkdirSync(dir, { recursive: true });
    console.log('📁 Carpeta "uploads" creada en el servidor');
}

// 2. Middlewares (Para que entienda JSON si le enviamos datos)
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));
app.use(cors());

// 3. Rutas
app.use('/api/mapas', mapasRoutes);
app.use('/api/horarios', horariosRoutes);
app.use('/api/auth', authRoutes);
app.use('/api/usuario', usuarioRoutes);

app.use('/uploads', express.static('uploads'));

// 4. Arrancar el servidor
app.listen(port, '0.0.0.0', () => {
    console.log(`===================================================`);
    console.log(`🚀 Servidor Node.js corriendo en el puerto ${port}`);
    console.log(`===================================================`);
});