import pymongo
import sys
import base64

# Ruta de la imagen (usa una cadena raw para evitar errores con las barras invertidas)
image_path = r"C:\Users\anton\OneDrive\Escritorio\UNIVERSIDAD\4º AÑO\TFG\backend\images\plano_casa.jpg"
image_path2 = r"C:\Users\anton\OneDrive\Escritorio\UNIVERSIDAD\4º AÑO\TFG\backend\images\plano_casa_planta1.jpg"
image_path3 = r"C:\Users\anton\OneDrive\Escritorio\UNIVERSIDAD\4º AÑO\TFG\backend\images\ETSI-Facultad.jpg"

# Convertir la imagen a Base64
try:
    with open(image_path, "rb") as image_file:
        # Añadimos el prefijo estándar para que se reconozca como imagen web/app
        encoded_string = "data:image/jpeg;base64," + base64.b64encode(image_file.read()).decode("utf-8")
except Exception as e:
    print(f"❌ Error al leer la imagen de tu disco: {e}")
    sys.exit(1)

try:
    with open(image_path2, "rb") as image_file:
        # Añadimos el prefijo estándar para que se reconozca como imagen web/app
        encoded_string2 = "data:image/jpeg;base64," + base64.b64encode(image_file.read()).decode("utf-8")
except Exception as e:
    print(f"❌ Error al leer la imagen de tu disco: {e}")
    sys.exit(1)

try:
    with open(image_path3, "rb") as image_file:
        # Añadimos el prefijo estándar para que se reconozca como imagen web/app
        encoded_string3 = "data:image/jpeg;base64," + base64.b64encode(image_file.read()).decode("utf-8")
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
    "nombre": "Casa Completa",
    "dimensiones": {
        "ancho": 10.7,
        "largo": 10.7
    },
    "plantas": [
        # ==========================================
        # PLANTA 0: MAPA ORIGINAL
        # ==========================================
        {
            "plantaId": "planta_0",
            "nombre": "Planta Baja",
            "nivel": 0,
            "imagenBase64": encoded_string, # Imagen de la planta baja
            "knownBeacons": {
                "F0:DD:31:0E:CA:81": { "x": 2.0, "y": 2.0 }, #3 Hab 1
                "CC:06:A8:C7:B1:65": { "x": 9.0, "y": 1.5 }, #2 Cocina
                "CA:C2:BA:EA:CD:C5": { "x": 5.0, "y": 2.2 }, #1 Pasillo
                "C6:88:FA:09:0B:5A": { "x": 4.5, "y": 10.0 }, #4 Terraza
                "C1:AA:14:D5:B8:73": { "x": 8.5, "y": 7.0 }, #5 Salon
                #"EC:08:40:2E:6F:62": { "x": 8.75, "y": 5.0 }, #6 Entrada
                "D1:68:63:DB:B6:8A": { "x": 1.20, "y": 5.8 }, #7 Hab1 fondo
                #"EC:08:40:2E:6F:62": { "x": 9.25, "y": 5.0 }, #8 Entrada
            },
            "nodos": [
                # --- PASILLO ---
                { "id": "N1", "name": "Pasillo Inicio", "plantaId": "planta_0", "position": { "x": 2.25, "y": 2.25 }, "neighbors": ["N2", "N13"] },
                { "id": "N2", "name": "Pasillo Centro", "plantaId": "planta_0", "position": { "x": 4.5, "y": 2.25 }, "neighbors": ["N1", "N3", "N5"] },
                { "id": "N3", "name": "Pasillo Fondo",  "plantaId": "planta_0", "position": { "x": 6.5, "y": 2.25 }, "neighbors": ["N2", "N4", "N8"] },
                { "id": "N4", "name": "Pasillo Final",  "plantaId": "planta_0", "position": { "x": 8.75, "y": 2.25 }, "neighbors": ["N3", "N11"] },

                # --- HABITACIÓN 1 ---
                { "id": "N5", "name": "Hab1 Puerta",  "plantaId": "planta_0", "position": { "x": 4.5, "y": 3.25 }, "neighbors": ["N2", "N6"] },
                { "id": "N6", "name": "Hab1 Centro",  "plantaId": "planta_0", "position": { "x": 4.5, "y": 4.25 }, "neighbors": ["N5", "N7"] },
                { "id": "N7", "name": "Hab1 Fondo",   "plantaId": "planta_0", "position": { "x": 4.5, "y": 5.25 }, "neighbors": ["N6"] },

                # --- HABITACIÓN 2 ---
                { "id": "N8",  "name": "Hab2 Puerta", "plantaId": "planta_0", "position": { "x": 7, "y": 3.25 }, "neighbors": ["N3", "N9", "N11"] },
                { "id": "N9",  "name": "Hab2 Centro", "plantaId": "planta_0", "position": { "x": 7, "y": 4.25 }, "neighbors": ["N8", "N10"] },
                { "id": "N10", "name": "Hab2 Fondo",  "plantaId": "planta_0", "position": { "x": 7, "y": 5.25 }, "neighbors": ["N9"] },

                # --- ENTRADA (AQUÍ CONECTAMOS CON LA PLANTA DE ARRIBA) ---
                { "id": "N11", "name": "ENTRADA Puerta", "plantaId": "planta_0", "position": { "x": 8.25, "y": 3.25 }, "neighbors": ["N4", "N8","N12", "N_P1_1"] },
                { "id": "N12", "name": "ENTRADA Fondo",  "plantaId": "planta_0", "position": { "x": 8.25, "y": 4.25 }, "neighbors": ["N11", "N16"] },

                # --- SALON ---
                { "id": "N16", "name": "SALON Puerta",  "plantaId": "planta_0", "position": { "x": 8.25, "y": 5.25 }, "neighbors": ["N12", "N17"] },
                { "id": "N17", "name": "SALON MEDIO 1", "plantaId": "planta_0", "position": { "x": 8.25, "y": 6.25 }, "neighbors": ["N16", "N18"] },
                { "id": "N18", "name": "SALON MEDIO 2", "plantaId": "planta_0", "position": { "x": 8.25, "y": 7.25 }, "neighbors": ["N17", "N19"] },

                # --- TERRAZA ---
                { "id": "N19", "name": "TERRAZA Puerta", "plantaId": "planta_0", "position": { "x": 6.5, "y": 7.25 }, "neighbors": ["N18", "N20"] },
                { "id": "N20", "name": "TERRAZA", "plantaId": "planta_0", "position": { "x": 4.5, "y": 7.25 }, "neighbors": ["N19", "N21", "N23"] },
                { "id": "N23", "name": "TERRAZA", "plantaId": "planta_0", "position": { "x": 4.5, "y": 8.25 }, "neighbors": ["N20", "N24"] },
                { "id": "N24", "name": "TERRAZA", "plantaId": "planta_0", "position": { "x": 2.25, "y": 8.25 }, "neighbors": ["N23", "N21"] },
                { "id": "N21", "name": "TERRAZA", "plantaId": "planta_0", "position": { "x": 2.25, "y": 7.25 }, "neighbors": ["N20", "N22", "N24"] },
                { "id": "N22", "name": "TERRAZA-HAB3","plantaId": "planta_0", "position": { "x": 2.25, "y": 6.25 }, "neighbors": ["N21", "N15"] },

                # --- HABITACIÓN 3 ---
                { "id": "N13", "name": "Hab3 Puerta", "plantaId": "planta_0", "position": { "x": 2.25, "y": 3.5 }, "neighbors": ["N1", "N14"] },
                { "id": "N14", "name": "Hab3 Centro", "plantaId": "planta_0", "position": { "x": 2.25, "y": 4.5 }, "neighbors": ["N13", "N15"] },
                { "id": "N15", "name": "Hab3 Fondo",  "plantaId": "planta_0", "position": { "x": 2.25, "y": 5.5 }, "neighbors": ["N14", "N22"] }
            ],
            "pois": [
                { "id": "POI1", "nombre": "Televisión (Salón)", "nodoId": "N18", "plantaId": "planta_0" },
                { "id": "POI2", "nombre": "Baño Principal", "nodoId": "N1", "plantaId": "planta_0" },
                { "id": "POI3", "nombre": "Mesa de la Terraza", "nodoId": "N24", "plantaId": "planta_0" }
            ]
        },

        # ==========================================
        # PLANTA 1: PLANTA SUPERIOR (Minimalista)
        # ==========================================
        {
            "plantaId": "planta_1",
            "nombre": "Primera Planta",
            "nivel": 1,
            "imagenBase64": encoded_string2, # Imagen del piso de arriba
            "knownBeacons": {
                "F0:DD:31:0E:CA:81": { "x": 1.0, "y": 1.0 },
                "CC:06:A8:C7:B1:65": { "x": 9.0, "y": 1.0 },
                "CA:C2:BA:EA:CD:C5": { "x": 4.5, "y": 10.0 }
            },
            "nodos": [
                { "id": "N_P1_1", "name": "Escalera Arriba", "plantaId": "planta_1", "position": { "x": 8.25, "y": 3.25 }, "neighbors": ["N11", "N_P1_2"] },
                { "id": "N_P1_2", "name": "Pasillo P1", "plantaId": "planta_1", "position": { "x": 5.0, "y": 3.25 }, "neighbors": ["N_P1_1", "N_P1_3"] },
                { "id": "N_P1_3", "name": "Trastero", "plantaId": "planta_1", "position": { "x": 5.0, "y": 5.0 }, "neighbors": ["N_P1_2"] }
            ],
            "pois": [
                { "id": "POI4", "nombre": "Trastero", "nodoId": "N_P1_2", "plantaId": "planta_1" }
            ]
        }
    ]
}

datos_mapa2 = {
    "mapaId": "2",
    "nombre": "Facultad ETSII",
    "dimensiones": {
        "ancho": 211.0,
        "largo": 160.0
    },
    "plantas": [
        # ==========================================
        # PLANTA 0: MAPA ORIGINAL
        # ==========================================
        {
            "plantaId": "planta_0",
            "nombre": "Planta Baja",
            "nivel": 0,
            "imagenBase64": encoded_string3, # Imagen de la planta baja
            "knownBeacons": {
                "F0:DD:31:0E:CA:81": { "x": 2.0, "y": 2.0 }, #3 Hab 1
                "CC:06:A8:C7:B1:65": { "x": 9.0, "y": 1.5 }, #2 Cocina
                "CA:C2:BA:EA:CD:C5": { "x": 5.0, "y": 2.2 }, #1 Pasillo
                "C6:88:FA:09:0B:5A": { "x": 4.5, "y": 10.0 }, #4 Terraza
                "C1:AA:14:D5:B8:73": { "x": 8.5, "y": 7.0 }, #5 Salon
                #"EC:08:40:2E:6F:62": { "x": 8.75, "y": 5.0 }, #6 Entrada
                "D1:68:63:DB:B6:8A": { "x": 1.20, "y": 5.8 }, #7 Hab1 fondo
                #"EC:08:40:2E:6F:62": { "x": 9.25, "y": 5.0 }, #8 Entrada
            },
            "nodos": [

                # MODULO 3
                { "id": "N1", "name": "Final Modulo 3", "plantaId": "planta_0", "position": { "x": 30.0, "y": 99.0 }, "neighbors": ["N2", "N90"] },
                { "id": "N2", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 37.5, "y": 99.0 }, "neighbors": ["N1", "N3"] },
                { "id": "N3", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 44.0, "y": 99.0 }, "neighbors": ["N2", "N4"] },
                { "id": "N4", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 51.5, "y": 99.0 }, "neighbors": ["N3", "N5"] },
                { "id": "N5", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 59.0, "y": 99.0 }, "neighbors": ["N4", "N6"] },
                { "id": "N6", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 66.5, "y": 99.0 }, "neighbors": ["N5", "N7"] },
                { "id": "N7", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 74.0, "y": 99.0 }, "neighbors": ["N6", "N8", "N121"] },
                { "id": "N8", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 81.5, "y": 99.0 }, "neighbors": ["N7", "N9"] },
                { "id": "N9", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 89.0, "y": 99.0 }, "neighbors": ["N8", "N10"] },
                { "id": "N10", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 96.5, "y": 99.0 }, "neighbors": ["N9", "N11"] },
                { "id": "N11", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 104.0, "y": 99.0 }, "neighbors": ["N10", "N12"] },
                { "id": "N12", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 111.5, "y": 99.0 }, "neighbors": ["N11", "N13", "N118"] },
                { "id": "N13", "name": "Inicio Modulo 3", "plantaId": "planta_0", "position": { "x": 119.0, "y": 99.0 }, "neighbors": ["N12", "N15", "N54", "N82", "N85"] },
                { "id": "N81", "name": "Inicio escaleras modulo 3 para planta inferior", "plantaId": "planta_0", "position": { "x": 120.0, "y": 87.0 }, "neighbors": ["N17", "N18"] },
                { "id": "N82", "name": "Inicio escaleras modulo 3 para planta superior", "plantaId": "planta_0", "position": { "x": 120.0, "y": 95.0 }, "neighbors": ["N15", "N13"] },

                # PASILLO PRINCIPAL
                #{ "id": "N14", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 100.0 }, "neighbors": ["N13", "N15", "N54", "N55", "N69"] },
                { "id": "N15", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 95.0 }, "neighbors": ["N54", "N16", "N13", "N82", "N85"] },
                { "id": "N16", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 90.0 }, "neighbors": ["N15", "N17", "N55", "N56"] },
                { "id": "N17", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 85.0 }, "neighbors": ["N16", "N18", "N57"] },
                { "id": "N18", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 122.0, "y": 80.0 }, "neighbors": ["N17", "N19", "N57", "N58", "N41", "N81"] },
                { "id": "N19", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 75.0 }, "neighbors": ["N18", "N20", "N58", "N59", "N41"] },
                { "id": "N20", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 70.0 }, "neighbors": ["N19", "N21", "N60", "N79"] },
                { "id": "N21", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 65.0 }, "neighbors": ["N20", "N22", "N60"] },
                { "id": "N22", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 60.0 }, "neighbors": ["N21", "N23", "N61", "N62", "N80"] },
                { "id": "N23", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 55.0 }, "neighbors": ["N22", "N24", "N62", "N63", "N68"] },
                { "id": "N24", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 50.0 }, "neighbors": ["N23", "N25", "N63", "N64", "N68", "N84"] },
                { "id": "N25", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 45.0 }, "neighbors": ["N24", "N26", "N65"] },
                { "id": "N26", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 40.0 }, "neighbors": ["N25", "N27", "N65"] },
                { "id": "N27", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 35.0 }, "neighbors": ["N26", "N28", "N66", "N67", "N83", "N132"] },
                { "id": "N28", "name": "Pasillo principal Inicio", "plantaId": "planta_0", "position": { "x": 124.0, "y": 30.0 }, "neighbors": ["N27", "N67", "N124"] },

                # PASILLO LATERAL (N54-N67)
                { "id": "N54", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 99.0 }, "neighbors": ["N15", "N55", "N69", "N13", "N85"] },
                { "id": "N55", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 94.0 }, "neighbors": ["N14", "N56", "N16", "N114"] },
                { "id": "N56", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 89.0 }, "neighbors": ["N55", "N16", "N57", "N114"] },
                { "id": "N57", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 84.0 }, "neighbors": ["N56", "N58", "N17", "N18"] },
                { "id": "N58", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 77.5 }, "neighbors": ["N57", "N59", "N18", "N19"] },
                { "id": "N59", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 72.5 }, "neighbors": ["N58", "N60", "N19", "N79"] },
                { "id": "N60", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 67.5 }, "neighbors": ["N59", "N61", "N20", "N21", "N115"] },
                { "id": "N61", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 62.5 }, "neighbors": ["N60", "N62", "N22"] },
                { "id": "N62", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 57.5 }, "neighbors": ["N61", "N63", "N22", "N23"] },
                { "id": "N63", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 52.5 }, "neighbors": ["N62", "N64", "N23", "N24", "N68"] },
                { "id": "N64", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 47.5 }, "neighbors": ["N63", "N65", "N24"] },
                { "id": "N65", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 42.5 }, "neighbors": ["N64", "N66", "N25", "N26", "N116"] },
                { "id": "N66", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 37.5 }, "neighbors": ["N65", "N67", "N27", "N132"] },
                { "id": "N67", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 32.5 }, "neighbors": ["N66", "N27", "N28", "N132"] },


                # MODULO 2
                { "id": "N29", "name": "Final Modulo 2", "plantaId": "planta_0", "position": { "x": 30.0, "y": 75.0 }, "neighbors": ["N30", "N106"] },
                { "id": "N30", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 37.5, "y": 75.0 }, "neighbors": ["N29", "N31"] },
                { "id": "N31", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 45.0, "y": 75.0 }, "neighbors": ["N30", "N32", "N122"] },
                { "id": "N32", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 52.5, "y": 75.0 }, "neighbors": ["N31", "N33"] },
                { "id": "N33", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 60.0, "y": 75.0 }, "neighbors": ["N32", "N34"] },
                { "id": "N34", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 67.5, "y": 75.0 }, "neighbors": ["N33", "N35"] },
                { "id": "N35", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 75.0, "y": 75.0 }, "neighbors": ["N34", "N36"] },
                { "id": "N36", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 82.5, "y": 75.0 }, "neighbors": ["N35", "N37"] },
                { "id": "N37", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 90.0, "y": 75.0 }, "neighbors": ["N36", "N38"] },
                { "id": "N38", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 97.5, "y": 75.0 }, "neighbors": ["N37", "N39"] },
                { "id": "N39", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 105.0, "y": 75.0 }, "neighbors": ["N38", "N40"] },
                { "id": "N40", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 112.5, "y": 75.0 }, "neighbors": ["N39", "N41", "N119"] },
                { "id": "N41", "name": "Inicio Modulo 2", "plantaId": "planta_0", "position": { "x": 120.0, "y": 76.5 }, "neighbors": ["N40", "N19", "N79", "N18"] },
                { "id": "N79", "name": "Inicio escaleras modulo 2 para planta superior", "plantaId": "planta_0", "position": { "x": 120.0, "y": 72.5 }, "neighbors": ["N41", "N59", "N20"] },
                { "id": "N80", "name": "Inicio escaleras modulo 2 para planta inferior", "plantaId": "planta_0", "position": { "x": 120.0, "y": 63.0 }, "neighbors": ["N22"] },


                # MODULO 1
                { "id": "N42", "name": "Final Modulo 1", "plantaId": "planta_0", "position": { "x": 30.0, "y": 51.0 }, "neighbors": ["N43", "N107"] },
                { "id": "N43", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 37.5, "y": 51.0 }, "neighbors": ["N42", "N44"] },
                { "id": "N44", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 45.0, "y": 51.0 }, "neighbors": ["N43", "N45"] },
                { "id": "N45", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 52.5, "y": 51.0 }, "neighbors": ["N44", "N46"] },
                { "id": "N46", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 60.0, "y": 51.0 }, "neighbors": ["N45", "N47"] },
                { "id": "N47", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 67.5, "y": 51.0 }, "neighbors": ["N46", "N48", "N127"] },
                { "id": "N48", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 75.0, "y": 51.0 }, "neighbors": ["N47", "N49"] },
                { "id": "N49", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 82.5, "y": 51.0 }, "neighbors": ["N48", "N50"] },
                { "id": "N50", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 90.0, "y": 51.0 }, "neighbors": ["N49", "N51"] },
                { "id": "N51", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 97.5, "y": 51.0 }, "neighbors": ["N50", "N52"] },
                { "id": "N52", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 105.0, "y": 51.0 }, "neighbors": ["N51", "N53"] },
                { "id": "N53", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 112.5, "y": 51.0 }, "neighbors": ["N52", "N68", "N120"] },
                { "id": "N68", "name": "Inicio Modulo 1", "plantaId": "planta_0", "position": { "x": 120.0, "y": 52.5 }, "neighbors": ["N53", "N24", "N23", "N63"] },
                { "id": "N83", "name": "Inicio escaleras modulo 1 para planta inferior", "plantaId": "planta_0", "position": { "x": 120.0, "y": 38.0 }, "neighbors": ["N27"] },
                { "id": "N84", "name": "Inicio escaleras modulo 1 para planta superior", "plantaId": "planta_0", "position": { "x": 120.0, "y": 47.5 }, "neighbors": ["N24"] },

                # MODULO3 - PASILLO - BIBLIOTECA
                { "id": "N69", "name": "Inicio Pasillo", "plantaId": "planta_0", "position": { "x": 135.5, "y": 98.5 }, "neighbors": ["N14", "N70", "N54"] },
                { "id": "N70", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 143.0, "y": 98.5 }, "neighbors": ["N69", "N71"] },
                { "id": "N71", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 150.5, "y": 98.5 }, "neighbors": ["N70", "N72", "N131"] },
                { "id": "N72", "name": "Acceso Biblioteca Planta 1", "plantaId": "planta_0", "position": { "x": 158.0, "y": 98.5 }, "neighbors": ["N71", "N73"] },
                { "id": "N73", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 165.5, "y": 98.5 }, "neighbors": ["N72", "N74"] },
                { "id": "N74", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 173.0, "y": 98.5 }, "neighbors": ["N73", "N75"] },
                { "id": "N75", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 180.5, "y": 98.5 }, "neighbors": ["N74", "N76"] },
                { "id": "N76", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 188.0, "y": 98.5 }, "neighbors": ["N75", "N77"] },
                { "id": "N77", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 195.5, "y": 98.5 }, "neighbors": ["N76", "N78"] },
                { "id": "N78", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 203.0, "y": 98.5 }, "neighbors": ["N77",] },

                # MODULO 4
                { "id": "N85", "name": "Pasillo principal - Modulo 4 Inicio", "plantaId": "planta_0", "position": { "x": 124.0, "y": 102.5 }, "neighbors": ["N86", "N54", "N13", "N15"] },
                { "id": "N86", "name": "Pasillo principal - Modulo 4", "plantaId": "planta_0", "position": { "x": 124.0, "y": 107.5 }, "neighbors": ["N85", "N87", "N130"] },
                { "id": "N87", "name": "Pasillo principal - Modulo 4", "plantaId": "planta_0", "position": { "x": 124.0, "y": 112.5 }, "neighbors": ["N86", "N88"] },
                { "id": "N88", "name": "Pasillo principal - Modulo 4", "plantaId": "planta_0", "position": { "x": 124.0, "y": 117.5 }, "neighbors": ["N87", "N89"] },
                { "id": "N89", "name": "Pasillo principal - Modulo 4", "plantaId": "planta_0", "position": { "x": 124.0, "y": 122.5 }, "neighbors": ["N88"] },

                # PASILLO PARALEO AL PRINCIPAL
                { "id": "N90", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 22.5, "y": 99.0 }, "neighbors": ["N1", "N91", "N92", "N93", "N108"] },
                { "id": "N91", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 22.5, "y": 104.0 }, "neighbors": ["N90", "N92", "N93"] },
                { "id": "N92", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 104.0 }, "neighbors": ["N90", "N91", "N93", "N123"] },
                { "id": "N93", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 99.0 }, "neighbors": ["N90", "N91", "N92","N94", "N108"] },
                { "id": "N94", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 94.0 }, "neighbors": ["N93", "N95", "N125"] },
                { "id": "N95", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 89.0 }, "neighbors": ["N94", "N96", "N125"] },
                { "id": "N96", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 84.0 }, "neighbors": ["N95", "N97", "N109"] },
                { "id": "N97", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 79.0 }, "neighbors": ["N96", "N98", "N106", "N117"] },
                { "id": "N98", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 74.0 }, "neighbors": ["N97", "N99", "N106", "N110"] },
                { "id": "N99", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 69.0 }, "neighbors": ["N98", "N100"] },
                { "id": "N100", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 64.0 }, "neighbors": ["N99", "N101"] },
                { "id": "N101", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 59.0 }, "neighbors": ["N100", "N102", "N111"] },
                { "id": "N102", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 54.0 }, "neighbors": ["N101", "N103", "N107"] },
                { "id": "N103", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 49.0 }, "neighbors": ["N102", "N104", "N107", "N112"] },
                { "id": "N104", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 44.0 }, "neighbors": ["N103", "N105", "N126"] },
                { "id": "N105", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 39.0 }, "neighbors": ["N104", "N113"] },
                { "id": "N106", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 22.5, "y": 75.0 }, "neighbors": ["N97", "N98", "N29", "N110"] },
                { "id": "N107", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 22.5, "y": 51.0 }, "neighbors": ["N102", "N103", "N42", "N112"] },
                { "id": "N108", "name": "Inicio escaleras modulo 3 para planta superior", "plantaId": "planta_0", "position": { "x": 21.0, "y": 95.0 }, "neighbors": ["N90", "N93"] },
                { "id": "N109", "name": "Inicio escaleras modulo 3 para planta inferior", "plantaId": "planta_0", "position": { "x": 21.0, "y": 86.0 }, "neighbors": ["N96"] },
                { "id": "N110", "name": "Inicio escaleras modulo 2 para planta superior", "plantaId": "planta_0", "position": { "x": 21.0, "y": 71.0 }, "neighbors": ["N98", "N106"] },
                { "id": "N111", "name": "Inicio escaleras modulo 2 para planta inferior", "plantaId": "planta_0", "position": { "x": 21.0, "y": 62.0 }, "neighbors": ["N101"] },
                { "id": "N112", "name": "Inicio escaleras modulo 1 para planta superior", "plantaId": "planta_0", "position": { "x": 21.0, "y": 47.0 }, "neighbors": ["N103", "N107"] },
                { "id": "N113", "name": "Inicio escaleras modulo 1 para planta inferior", "plantaId": "planta_0", "position": { "x": 21.0, "y": 38.0 }, "neighbors": ["N105"] },

                # Secretaría - Salón de actos
                { "id": "N132", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 131.5, "y": 35.0 }, "neighbors": ["N27", "N66","N67", "N133"] },
                { "id": "N133", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 139.0, "y": 35.0 }, "neighbors": ["N132", "N134"] },
                { "id": "N134", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 144.25, "y": 33.0 }, "neighbors": ["N133", "N135"] },
                { "id": "N135", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 149.5, "y": 31.0 }, "neighbors": ["N134", "N136", "N139"] },

                { "id": "N136", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 157.0, "y": 31.0 }, "neighbors": ["N135", "N137", "N139", "N140"] },
                { "id": "N137", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 164.5, "y": 31.0 }, "neighbors": ["N136", "N138", "N140", "N141"] },
                { "id": "N138", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 172.0, "y": 31.0 }, "neighbors": ["N137", "N141", "N142", "N149"] },

                { "id": "N139", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 155.0, "y": 36.0 }, "neighbors": ["N140", "N143", "N135", "N136"] },
                { "id": "N140", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 162.5, "y": 36.0 }, "neighbors": ["N139", "N141", "N143", "N144", "N136", "N137"] },
                { "id": "N141", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 170.0, "y": 36.0 }, "neighbors": ["N140", "N142", "N144", "N145", "N137", "N138"] },
                { "id": "N142", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 177.5, "y": 36.0 }, "neighbors": ["N141","N145", "N146", "N138"] },

                { "id": "N143", "name": "Acceso Secretaría", "plantaId": "planta_0", "position": { "x": 158.75, "y": 41.0 }, "neighbors": ["N144", "N139", "N140", "N128"] },
                { "id": "N144", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 166.0, "y": 41.0 }, "neighbors": ["N143", "N145", "N147", "N148", "N140", "N141"] },
                { "id": "N145", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 173.5, "y": 41.0 }, "neighbors": ["N144", "N146", "N147", "N148", "N141", "N142"] },
                { "id": "N146", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 181.0, "y": 41.0 }, "neighbors": ["N145","N148", "N142"] },

                { "id": "N147", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 166.0, "y": 46.0 }, "neighbors": ["N148", "N144", "N145"] },
                { "id": "N148", "name": "Entrada Factultad", "plantaId": "planta_0", "position": { "x": 173.5, "y": 44.5 }, "neighbors": ["N147", "N144", "N145", "N146", "N129"] },




                # NODOS --> Points of interest
                { "id": "N114", "name": "Aseos MOD 3 - Planta 1", "plantaId": "planta_0", "position": { "x": 133.0, "y": 91.5 }, "neighbors": ["N55", "N56"] },
                { "id": "N115", "name": "Aseos MOD 2 - Planta 1", "plantaId": "planta_0", "position": { "x": 134.0, "y": 67.5 }, "neighbors": ["N60"] },
                { "id": "N116", "name": "Consejería", "plantaId": "planta_0", "position": { "x": 134.0, "y": 42.5 }, "neighbors": ["N65"] },
                { "id": "N117", "name": "Reprografía", "plantaId": "planta_0", "position": { "x": 11.0, "y": 79.0 }, "neighbors": ["N97"] },
                { "id": "N118", "name": "Laboratorios Modulo 3", "plantaId": "planta_0", "position": { "x": 111.5, "y": 93.0 }, "neighbors": ["N12"] },
                { "id": "N119", "name": "Laboratorios Modulo 2", "plantaId": "planta_0", "position": { "x": 112.5, "y": 69.0 }, "neighbors": ["N40"] },
                { "id": "N120", "name": "Laboratorios Modulo 1", "plantaId": "planta_0", "position": { "x": 112.5, "y": 45.0 }, "neighbors": ["N53"] },
                { "id": "N121", "name": "Lab 3.1.6 - Informática", "plantaId": "planta_0", "position": { "x": 74.0, "y": 93.0 }, "neighbors": ["N7"] },
                { "id": "N122", "name": "Lab 2.1.7 - Computadores", "plantaId": "planta_0", "position": { "x": 45.0, "y": 69.0 }, "neighbors": ["N31"] },
                { "id": "N123", "name": "Lab 3.1.11 - Electrónica", "plantaId": "planta_0", "position": { "x": 8.0, "y": 104.0 }, "neighbors": ["N92"] },
                { "id": "N124", "name": "Aparcamiento de bicicletas", "plantaId": "planta_0", "position": { "x": 124.0, "y": 25.0 }, "neighbors": ["N28"] },
                { "id": "N125", "name": "Aseos MOD 3 - Planta 1 - Oeste", "plantaId": "planta_0", "position": { "x": 8.0, "y": 91.5 }, "neighbors": ["N94", "N95"] },
                { "id": "N126", "name": "Aseos MOD 1 - Planta 1 - Oeste", "plantaId": "planta_0", "position": { "x": 8.0, "y": 42.5 }, "neighbors": ["N104"] },
                { "id": "N127", "name": "Lab 1.1.5 - Telecomunicaciones", "plantaId": "planta_0", "position": { "x": 67.5, "y": 45.0 }, "neighbors": ["N47"] },
                { "id": "N128", "name": "Acceso a Secretaría", "plantaId": "planta_0", "position": { "x":161.0, "y": 45.0 }, "neighbors": ["N143"] },
                { "id": "N129", "name": "Salón de Actos", "plantaId": "planta_0", "position": { "x":180.0, "y": 50.0 }, "neighbors": ["N148"] },
                { "id": "N130", "name": "Aulas Módulo 4 - Planta 1", "plantaId": "planta_0", "position": { "x":116.5, "y": 107.5 }, "neighbors": ["N86"] },
                { "id": "N131", "name": "Acceso a Biblioteca - Hemeroteca", "plantaId": "planta_0", "position": { "x":150.5, "y": 93.0 }, "neighbors": ["N71"] },
                { "id": "N149", "name": "Acceso/Salida Factultad", "plantaId": "planta_0", "position": { "x": 179.5, "y": 31.0 }, "neighbors": ["N138"] },



            ],
            "pois": [
                { "id": "POI1", "nombre": "Aseos MOD 3 - Planta 1", "nodoId": "N114", "plantaId": "planta_0" },
                { "id": "POI2", "nombre": "Aseos MOD 2 - Planta 1", "nodoId": "N115", "plantaId": "planta_0" },
                { "id": "POI3", "nombre": "Consejería", "nodoId": "N116", "plantaId": "planta_0" },
                { "id": "POI4", "nombre": "Reprografía", "nodoId": "N117", "plantaId": "planta_0" },
                { "id": "POI5", "nombre": "Laboratorios Modulo 3", "nodoId": "N118", "plantaId": "planta_0" },
                { "id": "POI6", "nombre": "Laboratorios Modulo 2", "nodoId": "N119", "plantaId": "planta_0" },
                { "id": "POI7", "nombre": "Laboratorios Modulo 1", "nodoId": "N120", "plantaId": "planta_0" },
                { "id": "POI8", "nombre": "Lab 3.1.6 - Informática", "nodoId": "N121", "plantaId": "planta_0" },
                { "id": "POI9", "nombre": "Lab 2.1.7 - Computadores", "nodoId": "N122", "plantaId": "planta_0" },
                { "id": "POI10", "nombre": "Lab 3.1.11 - Electrónica", "nodoId": "N123", "plantaId": "planta_0" },
                { "id": "POI11", "nombre": "Aseos MOD 3 - Planta 1 - Oeste", "nodoId": "N125", "plantaId": "planta_0" },
                { "id": "POI12", "nombre": "Aseos MOD 1 - Planta 1 - Oeste", "nodoId": "N126", "plantaId": "planta_0" },
                { "id": "POI13", "nombre": "Aparcamiento de bicicletas", "nodoId": "N124", "plantaId": "planta_0" },
                { "id": "POI14", "nombre": "Lab 1.1.5 - Telecomunicaciones", "nodoId": "N127", "plantaId": "planta_0" },
                { "id": "POI15", "nombre": "Acceso a Secretaría", "nodoId": "N128", "plantaId": "planta_0" },
                { "id": "POI16", "nombre": "Salón de Actos", "nodoId": "N129", "plantaId": "planta_0" },
                { "id": "POI17", "nombre": "Aulas Módulo 4 - Planta 1", "nodoId": "N130", "plantaId": "planta_0" },
                { "id": "POI18", "nombre": "Acceso a Biblioteca - Hemeroteca", "nodoId": "N131", "plantaId": "planta_0" },
                { "id": "POI19", "nombre": "Acceso/Salida Factultad", "nodoId": "N149", "plantaId": "planta_0" },
            ]
        },

        # ==========================================
        # PLANTA 1: PLANTA SUPERIOR (Minimalista)
        # ==========================================
        {
            "plantaId": "planta_1",
            "nombre": "Primera Planta",
            "nivel": 1,
            "imagenBase64": encoded_string3, # Imagen del piso de arriba
            "knownBeacons": {

            },
            "nodos": [

            ],
            "pois": [

            ]
        }
    ]
}

# 3. Borrar el mapa si ya existía (para evitar duplicados al probar)
coleccion.delete_one({"mapaId": "1"})
coleccion.delete_one({"mapaId": "2"})

# 4. Insertar los datos en la base de datos
resultado = coleccion.insert_one(datos_mapa)
resultado = coleccion.insert_one(datos_mapa2)

if resultado.inserted_id:
    print(f"🚀 ¡Mapa insertado con éxito! ObjectID: {resultado.inserted_id}")
else:
    print("❌ Hubo un error al insertar el mapa.")