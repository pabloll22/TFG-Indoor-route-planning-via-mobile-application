import pymongo
import sys
import base64

# Ruta de la imagen (usa una cadena raw para evitar errores con las barras invertidas)
image_path = r"C:\Users\anton\OneDrive\Escritorio\UNIVERSIDAD\4º AÑO\TFG\backend\images\plano_casa.jpg"

# Convertir la imagen a Base64
try:
    with open(image_path, "rb") as image_file:
        # Añadimos el prefijo estándar para que se reconozca como imagen web/app
        encoded_string = "data:image/jpeg;base64," + base64.b64encode(image_file.read()).decode("utf-8")
except Exception as e:
    print(f"❌ Error al leer la imagen de tu disco: {e}")
    sys.exit(1)

# 1. Configurar la conexión a MongoDB
MONGO_URI = "mongodb+srv://tfg_db_user:1234@cluster0.mjqbqvl.mongodb.net/?appName=Cluster0"
NOMBRE_BD = "Cluster0"
COLECCION = "mapas"

try:
    cliente = pymongo.MongoClient(MONGO_URI)
    db = cliente[NOMBRE_BD]
    coleccion = db[COLECCION]
    print("✅ Conectado a MongoDB desde Python.")
except Exception as e:
    print(f"❌ Error al conectar a Mongo: {e}")
    sys.exit(1)

# 2. Definir los datos completos de tu mapa
datos_mapa = {
    "mapaId": "1",
    "nombre": "Plano Casa",
    "imagenBase64": encoded_string, # <-- ¡AQUÍ GUARDAMOS LA IMAGEN EN MONGO!
    "knownBeacons": {
        "F0:DD:31:0E:CA:81": { "x": 1.0, "y": 1.0 },
        "CC:06:A8:C7:B1:65": { "x": 9.0, "y": 1.0 },
        "CA:C2:BA:EA:CD:C5": { "x": 4.5, "y": 10.0 }
    },
    "nodos": [
        # --- PASILLO ---
        { "id": "N1", "name": "Pasillo Inicio", "position": { "x": 2.25, "y": 2.25 }, "neighbors": ["N2", "N13"] },
        { "id": "N2", "name": "Pasillo Centro", "position": { "x": 4.5, "y": 2.25 }, "neighbors": ["N1", "N3", "N5"] },
        { "id": "N3", "name": "Pasillo Fondo", "position": { "x": 6.5, "y": 2.25 }, "neighbors": ["N2", "N4", "N8"] },
        { "id": "N4", "name": "Pasillo Final", "position": { "x": 8.75, "y": 2.25 }, "neighbors": ["N3", "N11"] },

        # --- HABITACIÓN 1 ---
        { "id": "N5", "name": "Hab1 Puerta", "position": { "x": 4.5, "y": 3.25 }, "neighbors": ["N2", "N6"] },
        { "id": "N6", "name": "Hab1 Centro", "position": { "x": 4.5, "y": 4.25 }, "neighbors": ["N5", "N7"] },
        { "id": "N7", "name": "Hab1 Fondo", "position": { "x": 4.5, "y": 5.25 }, "neighbors": ["N6"] },

        # --- HABITACIÓN 2 ---
        { "id": "N8", "name": "Hab2 Puerta", "position": { "x": 7, "y": 3.25 }, "neighbors": ["N3", "N9", "N11"] },
        { "id": "N9", "name": "Hab2 Centro", "position": { "x": 7, "y": 4.25 }, "neighbors": ["N8", "N10"] },
        { "id": "N10", "name": "Hab2 Fondo", "position": { "x": 7, "y": 5.25 }, "neighbors": ["N9"] },

        # --- ENTRADA ---
        { "id": "N11", "name": "ENTRADA Puerta", "position": { "x": 8.25, "y": 3.25 }, "neighbors": ["N4", "N8","N12"] },
        { "id": "N12", "name": "ENTRADA Fondo", "position": { "x": 8.25, "y": 4.25 }, "neighbors": ["N11", "N16"] },

        # --- SALON ---
        # CORREGIDO: N16 apuntaba a sí mismo. Ahora apunta a N17.
        { "id": "N16", "name": "SALON Puerta", "position": { "x": 8.25, "y": 5.25 }, "neighbors": ["N12", "N17"] },
        { "id": "N17", "name": "SALON MEDIO 1", "position": { "x": 8.25, "y": 6.25 }, "neighbors": ["N16", "N18"] },
        { "id": "N18", "name": "SALON MEDIO 2", "position": { "x": 8.25, "y": 7.25 }, "neighbors": ["N17", "N19"] },

        # --- TERRAZA ---
        { "id": "N19", "name": "TERRAZA Puerta", "position": { "x": 6.5, "y": 7.25 }, "neighbors": ["N18", "N20"] },
        # CORREGIDO: N20 ahora sabe que N23 existe.
        { "id": "N20", "name": "TERRAZA", "position": { "x": 4.5, "y": 7.25 }, "neighbors": ["N19", "N21", "N23"] },
        { "id": "N23", "name": "TERRAZA", "position": { "x": 4.5, "y": 8.25 }, "neighbors": ["N20", "N24"] },
        { "id": "N24", "name": "TERRAZA", "position": { "x": 2.25, "y": 8.25 }, "neighbors": ["N23", "N21"] },
        # CORREGIDO: N21 ahora sabe que N24 existe.
        { "id": "N21", "name": "TERRAZA", "position": { "x": 2.25, "y": 7.25 }, "neighbors": ["N20", "N22", "N24"] },
        { "id": "N22", "name": "TERRAZA-HAB3", "position": { "x": 2.25, "y": 6.25 }, "neighbors": ["N21", "N15"] },

        # --- HABITACIÓN 3 ---
        { "id": "N13", "name": "Hab3 Puerta", "position": { "x": 2.25, "y": 3.5 }, "neighbors": ["N1", "N14"] },
        { "id": "N14", "name": "Hab3 Centro", "position": { "x": 2.25, "y": 4.5 }, "neighbors": ["N13", "N15"] },
        { "id": "N15", "name": "Hab3 Fondo", "position": { "x": 2.25, "y": 5.5 }, "neighbors": ["N14", "N22"] }
    ],

    # PUNTOS DE INTERÉS
    "pois": [
        { "id": "POI1", "nombre": "Televisión (Salón)", "nodoId": "N18" },
        { "id": "POI2", "nombre": "Baño Principal", "nodoId": "N1" },
        { "id": "POI3", "nombre": "Mesa de la Terraza", "nodoId": "N24" }
    ]
}

# 3. Borrar el mapa si ya existía (para evitar duplicados al probar)
coleccion.delete_one({"mapaId": "1"})

# 4. Insertar los datos en la base de datos
resultado = coleccion.insert_one(datos_mapa)

if resultado.inserted_id:
    print(f"🚀 ¡Mapa insertado con éxito! ObjectID: {resultado.inserted_id}")
else:
    print("❌ Hubo un error al insertar el mapa.")