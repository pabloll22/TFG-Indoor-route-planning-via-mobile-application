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
                "F0:DD:31:0E:CA:81": { "x": 17.0, "y": 105.5 }, #3 Electronica
                "CC:06:A8:C7:B1:65": { "x": 30.0, "y": 101.0 }, #2 Final modulo 3
                "CA:C2:BA:EA:CD:C5": { "x": 14.0, "y": 91.5 }, #1 Aseos Modulo 3 oeste
                "C6:88:FA:09:0B:5A": { "x": 45.0, "y": 97.0 }, #4 Modulo 3 intermedio
                "C1:AA:14:D5:B8:73": { "x": 60.0, "y": 101.0 }, #5 Modulo 3 intermedio
                "EC:08:40:2E:6F:62": { "x": 75.0, "y": 97.0 }, #6 Modulo 3 intermedio
                "D1:68:63:DB:B6:8A": { "x": 22.0, "y": 79.0 }, #7 Reprografia
                "CB:25:40:66:AA:F2": { "x": 90.0, "y": 101.0 }, #8 Modulo 3
            },
            "nodos": [
                # MÓDULO 3
                { "id": "N200", "name": "Final Modulo 3", "plantaId": "planta_0", "position": { "x": 30.0, "y": 99.0 }, "neighbors": ["N201", "N289", "N290", "N312", "N313"] },
                { "id": "N201", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 37.5, "y": 99.0 }, "neighbors": ["N200", "N202", "N312", "N313", "N314", "POI27"] },
                { "id": "N202", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 44.0, "y": 99.0 }, "neighbors": ["N201", "N203", "N313", "N314", "N315"] },
                { "id": "N203", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 51.5, "y": 99.0 }, "neighbors": ["N202", "N204", "N314", "N315", "N316"] },
                { "id": "N204", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 59.0, "y": 99.0 }, "neighbors": ["N203", "N205", "N315", "N316", "N317"] },
                { "id": "N205", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 66.5, "y": 99.0 }, "neighbors": ["N204", "N206", "N316", "N317", "N318", "POI26"] },
                { "id": "N206", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 74.0, "y": 99.0 }, "neighbors": ["N205", "N207", "N317", "N318"] },
                { "id": "N207", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 81.5, "y": 99.0 }, "neighbors": ["N206", "N208", "N319", "N320", "N422"] },
                { "id": "N208", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 89.0, "y": 99.0 }, "neighbors": ["N207", "N209", "N319", "N320", "N321"] },
                { "id": "N209", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 96.5, "y": 99.0 }, "neighbors": ["N208", "N210", "N213", "N320", "N321", "N322"] },
                { "id": "N210", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 104.0, "y": 99.0 }, "neighbors": ["N209", "N211", "N321", "N322", "N323"] },
                { "id": "N211", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 111.5, "y": 99.0 }, "neighbors": ["N210", "N212", "N322", "N323"] },
                { "id": "N212", "name": "Inicio Modulo 3", "plantaId": "planta_0", "position": { "x": 119.0, "y": 99.0 }, "neighbors": ["N211", "N215", "N323", "N284", "N216", "N230"] },
                { "id": "N215", "name": "Inicio escaleras modulo 3 para planta superior", "plantaId": "planta_0", "position": { "x": 120.0, "y": 95.0 }, "neighbors": ["N212", "N81", "N216"] },

                { "id": "N422", "name": "Escaleras intermedias modulo 3", "plantaId": "planta_0", "position": { "x": 79.0, "y": 101.0 }, "neighbors": ["N207","N319","N153"] },

                { "id": "N312", "name": "Final Modulo 3", "plantaId": "planta_0", "position": { "x": 30.0, "y": 103.0 }, "neighbors": ["N200", "N201", "N313", "N289", "N290"] },
                { "id": "N313", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 37.5, "y": 103.0 }, "neighbors": ["N312", "N314", "N200", "N201", "N202"] },
                { "id": "N314", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 45.0, "y": 103.0 }, "neighbors": ["N313", "N315", "N201", "N202", "N203"] },
                { "id": "N315", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 52.5, "y": 103.0 }, "neighbors": ["N314", "N316", "N202", "N203", "N204"] },
                { "id": "N316", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 60.0, "y": 103.0 }, "neighbors": ["N315", "N317", "N203", "N204", "N205"] },
                { "id": "N317", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 67.5, "y": 103.0 }, "neighbors": ["N316", "N318", "N204", "N205", "N206"] },
                { "id": "N318", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 75.0, "y": 103.0 }, "neighbors": ["N317", "N319", "N205", "N206"] },
                { "id": "N319", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 82.5, "y": 103.0 }, "neighbors": ["N318", "N320", "N207", "N208", "N422"] },
                { "id": "N320", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 90.0, "y": 103.0 }, "neighbors": ["N319", "N321", "N207", "N208", "N209"] },
                { "id": "N321", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 97.5, "y": 103.0 }, "neighbors": ["N320", "N322", "N208", "N209", "N210"] },
                { "id": "N322", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 105.0, "y": 103.0 }, "neighbors": ["N321", "N323", "N209", "N210", "N211"] },
                { "id": "N323", "name": "Pasillo Modulo 3", "plantaId": "planta_0", "position": { "x": 112.5, "y": 103.0 }, "neighbors": ["N322", "N210", "N211", "N212", "N324", "N284"] },
                { "id": "N324", "name": "Inicio Modulo 3", "plantaId": "planta_0", "position": { "x": 119.0, "y": 104.5 }, "neighbors": ["N323", "N284", "N285",] },


                # PASILLO PRINCIPAL
                { "id": "N216", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 95.0 }, "neighbors": ["N230", "N217", "N215", "N284", "N212", "N231"] },
                { "id": "N217", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 90.0 }, "neighbors": ["N216", "N218", "N231", "N232"] },
                { "id": "N218", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 85.0 }, "neighbors": ["N217", "N219", "N233"] },
                { "id": "N219", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 80.0 }, "neighbors": ["N218", "N220", "N233", "N234", "N256", "N336", "N418"] },
                { "id": "N220", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 75.0 }, "neighbors": ["N219", "N221", "N234", "N235", "N256"] },
                { "id": "N221", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 70.0 }, "neighbors": ["N220", "N222", "N236", "N257"] },
                { "id": "N222", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 65.0 }, "neighbors": ["N221", "N223", "N236", "POI31"] },
                { "id": "N223", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 60.0 }, "neighbors": ["N222", "N224", "N237", "N238", "N348", "POI31"] },
                { "id": "N224", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 55.0 }, "neighbors": ["N223", "N225", "N238", "N239", "N271", "N348"] },
                { "id": "N225", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 50.0 }, "neighbors": ["N224", "N226", "N239", "N240", "N271", "N273"] },
                { "id": "N226", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 45.0 }, "neighbors": ["N225", "N227", "N241"] },
                { "id": "N227", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 40.0 }, "neighbors": ["N226", "N228", "N241"] },
                { "id": "N228", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 124.0, "y": 35.0 }, "neighbors": ["N227", "N229", "N242", "N243"] },
                { "id": "N229", "name": "Pasillo principal Inicio", "plantaId": "planta_0", "position": { "x": 124.0, "y": 30.0 }, "neighbors": ["N228", "N243"] },

                # PASILLO LATERAL (N54-N67)
                { "id": "N230", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 99.0 }, "neighbors": ["N216", "N231", "N274", "N284", "N212", "N386"] },
                { "id": "N231", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 94.0 }, "neighbors": ["N230","N232", "N217", "POI21", "N216"] },
                { "id": "N232", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 89.0 }, "neighbors": ["N231", "N217", "N233", "POI21"] },
                { "id": "N233", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 84.0 }, "neighbors": ["N232", "N234", "N218", "N219", "N418"] },
                { "id": "N234", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 77.5 }, "neighbors": ["N233", "N235", "N219", "N220", "N418"] },
                { "id": "N235", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 72.5 }, "neighbors": ["N234", "N236", "N220", "N257", "POI22"] },
                { "id": "N236", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 67.5 }, "neighbors": ["N235", "N237", "N221", "N222"] },
                { "id": "N237", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 62.5 }, "neighbors": ["N236", "N238", "N223"] },
                { "id": "N238", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 57.5 }, "neighbors": ["N237", "N239", "N223", "N224", "N385"] },
                { "id": "N239", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 52.5 }, "neighbors": ["N238", "N240", "N224", "N225", "N271", "N385"] },
                { "id": "N240", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 47.5 }, "neighbors": ["N239", "N241", "N225", "POI23"] },
                { "id": "N241", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 42.5 }, "neighbors": ["N240", "N242", "N226", "N227"] },
                { "id": "N242", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 37.5 }, "neighbors": ["N241", "N243", "N228"] },
                { "id": "N243", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 128.0, "y": 32.5 }, "neighbors": ["N242", "N228", "N229"] },


                # MODULO 2
                { "id": "N244", "name": "Final Modulo 2", "plantaId": "planta_0", "position": { "x": 30.0, "y": 75.0 }, "neighbors": ["N245", "N305", "N325", "N326", "N350"] },
                { "id": "N245", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 37.5, "y": 75.0 }, "neighbors": ["N244", "N246", "N325", "N326", "N327"] },
                { "id": "N246", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 45.0, "y": 75.0 }, "neighbors": ["N245", "N247", "N326", "N327", "N328", "POI28"] },
                { "id": "N247", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 52.5, "y": 75.0 }, "neighbors": ["N246", "N248", "N327", "N328", "N329"] },
                { "id": "N248", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 60.0, "y": 75.0 }, "neighbors": ["N247", "N249", "N328", "N329", "N330"] },
                { "id": "N249", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 67.5, "y": 75.0 }, "neighbors": ["N248", "N250", "N329", "N330", "N331"] },
                { "id": "N250", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 75.0, "y": 75.0 }, "neighbors": ["N249", "N251", "N330", "N331", "POI32"] },
                { "id": "N251", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 82.5, "y": 75.0 }, "neighbors": ["N250", "N252", "N332", "N333", "N421"] },
                { "id": "N252", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 90.0, "y": 75.0 }, "neighbors": ["N251", "N253", "N332", "N333", "N334"] },
                { "id": "N253", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 97.5, "y": 75.0 }, "neighbors": ["N252", "N254", "N333", "N334", "N335"] },
                { "id": "N254", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 105.0, "y": 75.0 }, "neighbors": ["N253", "N255", "N334", "N335", "N336"] },
                { "id": "N255", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 112.5, "y": 75.0 }, "neighbors": ["N254", "N256", "N335", "N336"] },
                { "id": "N256", "name": "Inicio Modulo 2", "plantaId": "planta_0", "position": { "x": 120.0, "y": 76.5 }, "neighbors": ["N255", "N220", "N257", "N219", "N336"] },
                { "id": "N257", "name": "Inicio escaleras modulo 2 para planta superior", "plantaId": "planta_0", "position": { "x": 120.0, "y": 72.5 }, "neighbors": ["N256", "N235", "N221", "N80"] },

                { "id": "N421", "name": "Escaleras intermedias modulo 2", "plantaId": "planta_0", "position": { "x": 79.0, "y": 77.0 }, "neighbors": ["N152", "N251", "N332"] },

                { "id": "N350", "name": "Final Modulo 2", "plantaId": "planta_0", "position": { "x": 22.5, "y": 79.0 }, "neighbors": ["N325", "N296", "N305", "N295", "N244"] },
                { "id": "N325", "name": "Final Modulo 2", "plantaId": "planta_0", "position": { "x": 30.0, "y": 79.0 }, "neighbors": ["N244", "N245", "N326", "N350"] },
                { "id": "N326", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 37.5, "y": 79.0 }, "neighbors": ["N325", "N327", "N244", "N245", "N246"] },
                { "id": "N327", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 45.0, "y": 79.0 }, "neighbors": ["N326", "N328", "N245", "N246", "N247"] },
                { "id": "N328", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 52.5, "y": 79.0 }, "neighbors": ["N327", "N329", "N246", "N247", "N248"] },
                { "id": "N329", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 60.0, "y": 79.0 }, "neighbors": ["N328", "N330", "N247", "N248", "N249"] },
                { "id": "N330", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 67.5, "y": 79.0 }, "neighbors": ["N329", "N331","N248", "N249", "N250"] },
                { "id": "N331", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 75.0, "y": 79.0 }, "neighbors": ["N330", "N332", "N249", "N250"] },
                { "id": "N332", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 82.5, "y": 79.0 }, "neighbors": ["N331", "N333", "N251", "N252", "N421"] },
                { "id": "N333", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 90.0, "y": 79.0 }, "neighbors": ["N332", "N334", "N251", "N252", "N253"] },
                { "id": "N334", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 97.5, "y": 79.0 }, "neighbors": ["N333", "N335", "N252", "N253", "N254"] },
                { "id": "N335", "name": "Pasillo Modulo 2", "plantaId": "planta_0", "position": { "x": 105.0, "y": 79.0 }, "neighbors": ["N334", "N336", "N253", "N254", "N255"] },
                { "id": "N336", "name": "Inicio Modulo 2", "plantaId": "planta_0", "position": { "x": 112.5, "y": 79.0 }, "neighbors": ["N335", "N254", "N255", "N256", "N219"] },


                # MODULO 1
                { "id": "N259", "name": "Final Modulo 1", "plantaId": "planta_0", "position": { "x": 30.0, "y": 51.0 }, "neighbors": ["N260", "N306", "N337", "N338", "N349"] },
                { "id": "N260", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 37.5, "y": 51.0 }, "neighbors": ["N259", "N261", "N337", "N338", "N339", "POI29"] },
                { "id": "N261", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 45.0, "y": 51.0 }, "neighbors": ["N260", "N262", "N338", "N339", "N340"] },
                { "id": "N262", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 52.5, "y": 51.0 }, "neighbors": ["N261", "N263", "N339", "N340", "N341"] },
                { "id": "N263", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 60.0, "y": 51.0 }, "neighbors": ["N262", "N264", "N340", "N341", "N342"] },
                { "id": "N264", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 67.5, "y": 51.0 }, "neighbors": ["N263", "N265", "N341", "N342", "N343"] },
                { "id": "N265", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 75.0, "y": 51.0 }, "neighbors": ["N264", "N266", "N342", "N343"] },
                { "id": "N266", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 82.5, "y": 51.0 }, "neighbors": ["N265", "N267", "N344", "N345", "N420"] },
                { "id": "N267", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 90.0, "y": 51.0 }, "neighbors": ["N266", "N268", "N344", "N345", "N346"] },
                { "id": "N268", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 97.5, "y": 51.0 }, "neighbors": ["N267", "N269", "N345", "N346", "N347"] },
                { "id": "N269", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 105.0, "y": 51.0 }, "neighbors": ["N268", "N270", "N346", "N347", "N348"] },
                { "id": "N270", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 112.5, "y": 51.0 }, "neighbors": ["N269", "N271", "N347", "N348"] },
                { "id": "N271", "name": "Inicio Modulo 1", "plantaId": "planta_0", "position": { "x": 120.0, "y": 52.5 }, "neighbors": ["N270", "N225", "N224", "N239", "N273", "N348"] },
                { "id": "N273", "name": "Inicio escaleras modulo 1 para planta superior", "plantaId": "planta_0", "position": { "x": 120.0, "y": 47.5 }, "neighbors": ["N225", "N83", "N271"] },

                { "id": "N420", "name": "Escaleras intermedias modulo 1", "plantaId": "planta_0", "position": { "x": 79.0, "y": 53.0 }, "neighbors": ["N266","N344","N151"] },

                { "id": "N349", "name": "Final Modulo 1", "plantaId": "planta_0", "position": { "x": 22.5, "y": 55.0 }, "neighbors": ["N337", "N259", "N306", "N300", "N301"] },
                { "id": "N337", "name": "Final Modulo 1", "plantaId": "planta_0", "position": { "x": 30.0, "y": 55.0 }, "neighbors": ["N338", "N259", "N260", "N306", "N349"] },
                { "id": "N338", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 37.5, "y": 55.0 }, "neighbors": ["N337", "N339", "N259", "N260", "N261"] },
                { "id": "N339", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 45.0, "y": 55.0 }, "neighbors": ["N338", "N340", "N260", "N261", "N262"] },
                { "id": "N340", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 52.5, "y": 55.0 }, "neighbors": ["N339", "N341", "N261", "N262", "N263"] },
                { "id": "N341", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 60.0, "y": 55.0 }, "neighbors": ["N340", "N342", "N262", "N263", "N264"] },
                { "id": "N342", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 67.5, "y": 55.0 }, "neighbors": ["N341", "N343", "N263", "N264", "N265"] },
                { "id": "N343", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 75.0, "y": 55.0 }, "neighbors": ["N342", "N344", "N264", "N265"] },
                { "id": "N344", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 82.5, "y": 55.0 }, "neighbors": ["N343", "N345", "N266", "N267", "N420"] },
                { "id": "N345", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 90.0, "y": 55.0 }, "neighbors": ["N344", "N346", "N266", "N267", "N268"] },
                { "id": "N346", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 97.5, "y": 55.0 }, "neighbors": ["N345", "N347", "N267", "N268", "N269"] },
                { "id": "N347", "name": "Pasillo Modulo 1", "plantaId": "planta_0", "position": { "x": 105.0, "y": 55.0 }, "neighbors": ["N346", "N348", "N268", "N269", "N270"] },
                { "id": "N348", "name": "Inicio Modulo 1", "plantaId": "planta_0", "position": { "x": 112.5, "y": 55.0 }, "neighbors": ["N347", "N269", "N270", "N271", "N223", "N224"] },

                # MODULO3 - PASILLO Principal- Maquinas EXPENDEDORAS - SALON DE ACTOS -PARKING
                { "id": "N274", "name": "Inicio Pasillo", "plantaId": "planta_0", "position": { "x": 135.5, "y": 98.5 }, "neighbors": ["N275", "N230", "N351", "N386", "N387"] },
                { "id": "N275", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 143.0, "y": 98.5 }, "neighbors": ["N274", "N276", "N351", "N352", "N386", "N387", "N388"] },
                { "id": "N276", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 150.5, "y": 98.5 }, "neighbors": ["N275", "N277", "N351", "N352", "N353", "N387", "N388", "N389"] },
                { "id": "N277", "name": "Acceso Biblioteca Planta 0", "plantaId": "planta_0", "position": { "x": 158.0, "y": 98.5 }, "neighbors": ["N276", "N278", "N352", "N353", "N354", "N388", "N389", "N390"] },
                { "id": "N278", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 165.5, "y": 98.5 }, "neighbors": ["N277", "N279", "N353", "N354", "N355", "N389", "N390", "N391"] },
                { "id": "N279", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 173.0, "y": 98.5 }, "neighbors": ["N278", "N280", "N354", "N355", "N356", "N390", "N391", "N392"] },
                { "id": "N280", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 180.5, "y": 98.5 }, "neighbors": ["N279", "N281", "N355", "N356", "N357", "N391", "N392", "N393"] },
                { "id": "N281", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 188.0, "y": 98.5 }, "neighbors": ["N280", "N282", "N356", "N357", "N392", "N393", "N394"] },
                { "id": "N282", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 195.5, "y": 98.5 }, "neighbors": ["N281", "N283", "N393", "N394", "N395"] },
                { "id": "N283", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 203.0, "y": 98.5 }, "neighbors": ["N282", "N394", "N395", "POI25"] },

                { "id": "N351", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 143.0, "y": 93.5 }, "neighbors": ["N352", "N274", "N275", "N276", "POI35"] },
                { "id": "N352", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 150.5, "y": 93.5 }, "neighbors": ["N351", "N353", "N275", "N276", "N277", "N358", "POI35"] },
                { "id": "N353", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 158.0, "y": 93.5 }, "neighbors": ["N352", "N354", "N276", "N277", "N278", "N358", "N359"] },
                { "id": "N354", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 165.5, "y": 93.5 }, "neighbors": ["N353", "N355", "N277", "N278", "N279", "N359", "N360"] },
                { "id": "N355", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 173.0, "y": 93.5 }, "neighbors": ["N354", "N356", "N278", "N279", "N280", "N360", "N361"] },
                { "id": "N356", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 180.5, "y": 93.5 }, "neighbors": ["N355", "N357", "N279", "N280", "N281", "N361", "N362"] },
                { "id": "N357", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 188.0, "y": 93.5 }, "neighbors": ["N356", "N280", "N281", "N362", "POI33", "POI34"] },

                { "id": "N358", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 154.5, "y": 88.5 }, "neighbors": ["N359", "N352", "N353", "N364", "N365"] },
                { "id": "N359", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 162.0, "y": 88.5 }, "neighbors": ["N358", "N360", "N353", "N354", "N365", "N366"] },
                { "id": "N360", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 169.5, "y": 88.5 }, "neighbors": ["N359", "N361", "N354", "N355", "N366", "N367"] },
                { "id": "N361", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 177.0, "y": 88.5 }, "neighbors": ["N360", "N362", "N355", "N356", "N367", "N368"] },
                { "id": "N362", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 184.5, "y": 88.5 }, "neighbors": ["N361", "N356", "N357", "N368", "N369"] },

                { "id": "N363", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 143.5, "y": 82.5 }, "neighbors": ["N364", "N370", "N418"] },
                { "id": "N364", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 151.0, "y": 82.5 }, "neighbors": ["N363", "N365", "N358", "N370", "N371"] },
                { "id": "N365", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 158.5, "y": 82.5 }, "neighbors": ["N364", "N366", "N358", "N359", "N371", "N372"] },
                { "id": "N366", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 166.0, "y": 82.5 }, "neighbors": ["N365", "N367", "N359", "N360", "N372", "N373"] },
                { "id": "N367", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 173.5, "y": 82.5 }, "neighbors": ["N366", "N368", "N360", "N361", "N373"] },
                { "id": "N368", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 181.0, "y": 82.5 }, "neighbors": ["N367", "N369", "N361", "N362"] },
                { "id": "N369", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 188.5, "y": 82.5 }, "neighbors": ["N368", "N362"] },

                { "id": "N370", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 147.5, "y": 78.5 }, "neighbors": ["N363", "N364", "N371", "N374", "N418"] },
                { "id": "N371", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 155.0, "y": 78.5 }, "neighbors": ["N364", "N365", "N370", "N372", "N374", "N375"] },
                { "id": "N372", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 162.5, "y": 78.5 }, "neighbors": ["N365", "N366", "N371", "N373"] },
                { "id": "N373", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 170.0, "y": 78.5 }, "neighbors": ["N366", "N367", "N372"] },

                { "id": "N374", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 151.5, "y": 74.5 }, "neighbors": ["N375", "N370", "N371", "N376", "N377"] },
                { "id": "N375", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 159.0, "y": 74.5 }, "neighbors": ["N374", "N371", "N376", "N377"] },

                { "id": "N376", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 149.0, "y": 70.5 }, "neighbors": ["N377", "N374", "N375", "N378", "N379"] },
                { "id": "N377", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 156.5, "y": 70.5 }, "neighbors": ["N376", "N374", "N375", "N378", "N379"] },

                { "id": "N378", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 147.0, "y": 66.5 }, "neighbors": ["N379", "N377", "N376", "N380", "N381"] },
                { "id": "N379", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 154.5, "y": 66.5 }, "neighbors": ["N378", "N377", "N376", "N380", "N381", "POI30"] },

                { "id": "N380", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 145.0, "y": 62.5 }, "neighbors": ["N381", "N378", "N379", "N382", "N383"] },
                { "id": "N381", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 152.5, "y": 62.5 }, "neighbors": ["N380", "N378", "N379", "N383", "POI30"] },

                { "id": "N382", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 141.0, "y": 58.5 }, "neighbors": ["N380", "N383", "N385"] },
                { "id": "N383", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 148.5, "y": 58.5 }, "neighbors": ["N382", "N384", "N380", "N381", "N419"] },
                { "id": "N384", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 155.0, "y": 56.5 }, "neighbors": ["N383", "N419"] },

                { "id": "N385", "name": "Pasillo principal", "plantaId": "planta_0", "position": { "x": 135.5, "y": 57.5 }, "neighbors": ["N382", "N238", "N239"] },

                { "id": "N418", "name": "Pasillo", "plantaId": "planta_0", "position": { "x": 137.5, "y": 80.5 }, "neighbors": ["N370", "N363", "N233", "N234", "N219"] },
                { "id": "N419", "name": "Escaleras", "plantaId": "planta_0", "position": { "x": 151.0, "y": 54.0 }, "neighbors": ["N383", "N384", "N150"] },

                # CAFETERÍA
                { "id": "N386", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 135.5, "y": 103.0 }, "neighbors": ["N387", "N274", "N275", "N396", "N397", "N230", "N284"] },
                { "id": "N387", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 143.0, "y": 103.0 }, "neighbors": ["N386", "N388", "N274", "N275", "N276", "N396", "N397"] },
                { "id": "N388", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 150.5, "y": 103.0 }, "neighbors": ["N387", "N389", "N275", "N276", "N277", "N398"] },
                { "id": "N389", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 158.0, "y": 103.0 }, "neighbors": ["N388", "N390", "N276", "N277", "N278", "N398", "N399"] },
                { "id": "N390", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 165.5, "y": 103.0 }, "neighbors": ["N389", "N391", "N277", "N278", "N279", "N399", "N400"] },
                { "id": "N391", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 173.0, "y": 103.0 }, "neighbors": ["N390", "N392", "N278", "N279", "N280", "N400"] },
                { "id": "N392", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 180.5, "y": 103.0 }, "neighbors": ["N391", "N393", "N279", "N280", "N281"] },
                { "id": "N393", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 188.0, "y": 103.0 }, "neighbors": ["N392", "N394", "N280", "N281", "N282"] },
                { "id": "N394", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 195.5, "y": 103.0 }, "neighbors": ["N393", "N395", "N281", "N282", "N283"] },
                { "id": "N395", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 203.0, "y": 103.0 }, "neighbors": ["N394", "N282", "N283", "POI25"] },

                { "id": "N396", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 132.0, "y": 106.0 }, "neighbors": ["N397", "N386", "N387", "N284", "N285"] },
                { "id": "N397", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 139.5, "y": 106.0 }, "neighbors": ["N386", "N396", "N387", "N414"] },
                { "id": "N398", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 154.5, "y": 106.0 }, "neighbors": ["N399", "N388", "N389", "N401"] },
                { "id": "N399", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 162.0, "y": 106.0 }, "neighbors": ["N398", "N400", "N389", "N390", "N401", "N402"] },
                { "id": "N400", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 169.5, "y": 106.0 }, "neighbors": ["N399", "N390", "N391", "N402"] },

                { "id": "N401", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 158.5, "y": 111.0 }, "neighbors": ["N398", "N399", "N402", "N403", "N404"] },
                { "id": "N402", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 166.0, "y": 111.0 }, "neighbors": ["N399", "N400", "N401", "N404"] },

                { "id": "N403", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 154.5, "y": 116.0 }, "neighbors": ["N401", "N404", "N406", "N407"] },
                { "id": "N404", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 162.0, "y": 116.0 }, "neighbors": ["N403", "N401", "N402", "N407", "N408"] },

                { "id": "N405", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 143.5, "y": 121.0 }, "neighbors": ["N406", "N409", "N410", "N416"] },
                { "id": "N406", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 151.0, "y": 121.0 }, "neighbors": ["N405", "N407", "N403", "N410", "N411"] },
                { "id": "N407", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 158.5, "y": 121.0 }, "neighbors": ["N406", "N408", "N403", "N404", "N411", "N412"] },
                { "id": "N408", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 166.0, "y": 121.0 }, "neighbors": ["N407", "N404", "N412", "N413"] },

                { "id": "N409", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 139.5, "y": 126.0 }, "neighbors": ["N410", "N405", "N417"] },
                { "id": "N410", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 147.0, "y": 126.0 }, "neighbors": ["N409", "N405", "N406", "N411"] },
                { "id": "N411", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 154.5, "y": 126.0 }, "neighbors": ["N410", "N412", "N406", "N407", "POI24"] },
                { "id": "N412", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 162.0, "y": 126.0 }, "neighbors": ["N411", "N413", "N407", "N408"] },
                { "id": "N413", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 169.5, "y": 126.0 }, "neighbors": ["N412", "N408"] },

                { "id": "N414", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 139.0, "y": 110.0 }, "neighbors": ["N397", "N415"] },
                { "id": "N415", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 139.0, "y": 114.0 }, "neighbors": ["N414", "N416"] },
                { "id": "N416", "name": "Zona Cafetería", "plantaId": "planta_0", "position": { "x": 139.0, "y": 118.0 }, "neighbors": ["N415", "N405"] },

                # MODULO 4
                { "id": "N284", "name": "Pasillo principal - Modulo 4 Inicio", "plantaId": "planta_0", "position": { "x": 124.0, "y": 102.5 }, "neighbors": ["N285", "N230", "N216", "N324", "N212", "N386", "N396", "N323"] },
                { "id": "N285", "name": "Pasillo principal - Modulo 4", "plantaId": "planta_0", "position": { "x": 124.0, "y": 107.5 }, "neighbors": ["N284", "N286", "N324", "N396"] },
                { "id": "N286", "name": "Pasillo principal - Modulo 4", "plantaId": "planta_0", "position": { "x": 124.0, "y": 112.5 }, "neighbors": ["N285", "N287"] },
                { "id": "N287", "name": "Pasillo principal - Modulo 4", "plantaId": "planta_0", "position": { "x": 124.0, "y": 117.5 }, "neighbors": ["N286", "N288"] },
                { "id": "N288", "name": "Pasillo principal - Modulo 4", "plantaId": "planta_0", "position": { "x": 124.0, "y": 122.5 }, "neighbors": ["N287", "N417"] },
                { "id": "N417", "name": "Modulo 4 - Cafetería", "plantaId": "planta_0", "position": { "x": 131.5, "y": 126.0 }, "neighbors": ["N288", "N409"] },

                # PASILLO PARALEO AL PRINCIPAL
                { "id": "N289", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 22.5, "y": 99.0 }, "neighbors": ["N290", "N291", "N292", "N307", "N200", "N312"] },
                { "id": "N290", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 22.5, "y": 104.0 }, "neighbors": ["N289", "N291", "N292", "N200", "N312"] },
                { "id": "N291", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 104.0 }, "neighbors": ["N289", "N290", "N292"] },
                { "id": "N292", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 99.0 }, "neighbors": ["N289", "N290", "N291", "N293", "N307"] },
                { "id": "N293", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 94.0 }, "neighbors": ["N292", "N294"] },
                { "id": "N294", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 89.0 }, "neighbors": ["N293", "N295"] },
                { "id": "N295", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 84.0 }, "neighbors": ["N294", "N296", "N350"] },
                { "id": "N296", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 79.0 }, "neighbors": ["N295", "N297", "N305", "N350"] },
                { "id": "N297", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 74.0 }, "neighbors": ["N296", "N298", "N305", "N309"] },
                { "id": "N298", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 69.0 }, "neighbors": ["N297", "N299"] },
                { "id": "N299", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 64.0 }, "neighbors": ["N298", "N300"] },
                { "id": "N300", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 59.0 }, "neighbors": ["N299", "N301", "N349"] },
                { "id": "N301", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 54.0 }, "neighbors": ["N300", "N302", "N306", "N349"] },
                { "id": "N302", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 49.0 }, "neighbors": ["N301", "N303", "N306", "N311"] },
                { "id": "N303", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 44.0 }, "neighbors": ["N302", "N304"] },
                { "id": "N304", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 17.0, "y": 39.0 }, "neighbors": ["N303"] },
                { "id": "N305", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 22.5, "y": 75.0 }, "neighbors": ["N296", "N297", "N244", "N309", "N350"] },
                { "id": "N306", "name": "Pasillo Oeste", "plantaId": "planta_0", "position": { "x": 22.5, "y": 51.0 }, "neighbors": ["N301", "N302", "N259", "N311", "N349", "N337"] },
                { "id": "N307", "name": "Inicio escaleras modulo 3 para planta superior", "plantaId": "planta_0", "position": { "x": 21.0, "y": 95.0 }, "neighbors": ["N289", "N292", "N109"] },
                { "id": "N309", "name": "Inicio escaleras modulo 2 para planta superior", "plantaId": "planta_0", "position": { "x": 21.0, "y": 71.0 }, "neighbors": ["N297", "N305", "N111"] },
                { "id": "N311", "name": "Inicio escaleras modulo 1 para planta superior", "plantaId": "planta_0", "position": { "x": 21.0, "y": 47.0 }, "neighbors": ["N302", "N306", "N113"] },

                # NODOS --> Points of interest
                { "id": "N213", "name": "Aula 3.0.2", "plantaId": "planta_0", "position": { "x": 96.5, "y": 94.0 }, "neighbors": ["N209"] },
                { "id": "POI21", "name": "Aseos MOD 3 - Planta 0", "plantaId": "planta_0", "position": { "x": 133.0, "y": 91.5 }, "neighbors": ["N231", "N232"] },
                { "id": "POI22", "name": "Aseos MOD 2 - Planta 0", "plantaId": "planta_0", "position": { "x": 134.0, "y": 67.5 }, "neighbors": ["N235"] },
                { "id": "POI23", "name": "Aseos MOD 1 - Planta 0", "plantaId": "planta_0", "position": { "x": 134.0, "y": 42.5 }, "neighbors": ["N240"] },
                { "id": "POI24", "name": "Cafetería", "plantaId": "planta_0", "position": { "x": 154.5, "y": 131.0 }, "neighbors": ["N411"] },
                { "id": "POI25", "name": "Aparcamiento Coches acceso", "plantaId": "planta_0", "position": { "x": 207.5, "y": 100.5 }, "neighbors": ["N395", "N283"] },
                { "id": "POI26", "name": "Aula 3.0.5", "plantaId": "planta_0", "position": { "x": 66.5, "y": 94.0 }, "neighbors": ["N205"] },
                { "id": "POI27", "name": "Aula 3.0.8", "plantaId": "planta_0", "position": { "x": 37.5, "y": 94.0 }, "neighbors": ["N201"] },
                { "id": "POI28", "name": "Aula 2.0.7", "plantaId": "planta_0", "position": { "x": 45.0, "y": 69.0 }, "neighbors": ["N246"] },
                { "id": "POI29", "name": "Aula 1.0.8", "plantaId": "planta_0", "position": { "x": 37.5, "y": 45.0 }, "neighbors": ["N260"] },
                { "id": "POI30", "name": "Salón de Actos - 0", "plantaId": "planta_0", "position": { "x": 161, "y": 64.5 }, "neighbors": ["N379", "N381"] },
                { "id": "POI31", "name": "Aula 2.0.1.A", "plantaId": "planta_0", "position": { "x": 118.0, "y": 62.5 }, "neighbors": ["N222", "N223"] },
                { "id": "POI32", "name": "Aula 2.0.4", "plantaId": "planta_0", "position": { "x": 75.0, "y": 70.0 }, "neighbors": ["N250"] },
                { "id": "POI33", "name": "Zona de Descanso", "plantaId": "planta_0", "position": { "x": 193.0, "y": 88.5 }, "neighbors": ["N357"] },
                { "id": "POI34", "name": "Acceso Zona de Estudio", "plantaId": "planta_0", "position": { "x": 197.0, "y": 93.5 }, "neighbors": ["N357"] },
                { "id": "POI35", "name": "Acceso Biblioteca - 0", "plantaId": "planta_0", "position": { "x": 147.0, "y": 88.0 }, "neighbors": ["N352", "N351"] },


            ],
            "pois": [
                { "id": "POI20", "nombre": "Aula 3.0.2", "nodoId": "N213", "plantaId": "planta_0" },
                { "id": "POI21", "nombre": "Aseos MOD 3 - Planta 0", "nodoId": "POI21", "plantaId": "planta_0" },
                { "id": "POI22", "nombre": "Aseos MOD 2 - Planta 0", "nodoId": "POI22", "plantaId": "planta_0" },
                { "id": "POI23", "nombre": "Aseos MOD 1 - Planta 0", "nodoId": "POI23", "plantaId": "planta_0" },
                { "id": "POI24", "nombre": "Cafetería", "nodoId": "POI24", "plantaId": "planta_0" },
                { "id": "POI25", "nombre": "Aparcamiento Coches acceso", "nodoId": "POI25", "plantaId": "planta_0" },
                { "id": "POI26", "nombre": "Aula 3.0.5", "nodoId": "POI26", "plantaId": "planta_0" },
                { "id": "POI27", "nombre": "Aula 3.0.8", "nodoId": "POI27", "plantaId": "planta_0" },
                { "id": "POI28", "nombre": "Aula 2.0.7", "nodoId": "POI28", "plantaId": "planta_0" },
                { "id": "POI29", "nombre": "Aula 1.0.8", "nodoId": "POI29", "plantaId": "planta_0" },
                { "id": "POI30", "nombre": "Salón de Actos - 0", "nodoId": "POI30", "plantaId": "planta_0" },
                { "id": "POI31", "nombre": "Aula 2.0.1.A", "nodoId": "POI31", "plantaId": "planta_0" },
                { "id": "POI32", "nombre": "Aula 2.0.4", "nodoId": "POI32", "plantaId": "planta_0" },
                { "id": "POI33", "nombre": "Zona de Descanso", "nodoId": "POI33", "plantaId": "planta_0" },
                { "id": "POI34", "nombre": "Acceso Zona de Estudio", "nodoId": "POI34", "plantaId": "planta_0" },
                { "id": "POI35", "nombre": "Acceso Biblioteca - 0", "nodoId": "POI35", "plantaId": "planta_0" },
            ]
        },

        # ==========================================
        # PLANTA 1: PLANTA SUPERIOR
        # ==========================================
        {
            "plantaId": "planta_1",
            "nombre": "Primera Planta",
            "nivel": 1,
            "imagenBase64": encoded_string3, # Imagen del piso de arriba
            "knownBeacons": {
                "F0:DD:31:0E:CA:81": { "x": 17.0, "y": 105.5 }, #3 Electronica
                "CC:06:A8:C7:B1:65": { "x": 30.0, "y": 101.0 }, #2 Final modulo 3
                "CA:C2:BA:EA:CD:C5": { "x": 14.0, "y": 91.5 }, #1 Aseos Modulo 3 oeste
                "C6:88:FA:09:0B:5A": { "x": 45.0, "y": 97.0 }, #4 Modulo 3 intermedio
                "C1:AA:14:D5:B8:73": { "x": 60.0, "y": 101.0 }, #5 Modulo 3 intermedio
                "EC:08:40:2E:6F:62": { "x": 75.0, "y": 97.0 }, #6 Modulo 3 intermedio
                "D1:68:63:DB:B6:8A": { "x": 22.0, "y": 79.0 }, #7 Reprografia
                "CB:25:40:66:AA:F2": { "x": 90.0, "y": 101.0 }, #8 Modulo 3
            },
            "nodos": [

                # MODULO 3
                { "id": "N1", "name": "Final Modulo 3", "plantaId": "planta_1", "position": { "x": 30.0, "y": 99.0 }, "neighbors": ["N2", "N90"] },
                { "id": "N2", "name": "Pasillo Modulo 3", "plantaId": "planta_1", "position": { "x": 37.5, "y": 99.0 }, "neighbors": ["N1", "N3"] },
                { "id": "N3", "name": "Pasillo Modulo 3", "plantaId": "planta_1", "position": { "x": 44.0, "y": 99.0 }, "neighbors": ["N2", "N4"] },
                { "id": "N4", "name": "Pasillo Modulo 3", "plantaId": "planta_1", "position": { "x": 51.5, "y": 99.0 }, "neighbors": ["N3", "N5"] },
                { "id": "N5", "name": "Pasillo Modulo 3", "plantaId": "planta_1", "position": { "x": 59.0, "y": 99.0 }, "neighbors": ["N4", "N6"] },
                { "id": "N6", "name": "Pasillo Modulo 3", "plantaId": "planta_1", "position": { "x": 66.5, "y": 99.0 }, "neighbors": ["N5", "N7"] },
                { "id": "N7", "name": "Pasillo Modulo 3", "plantaId": "planta_1", "position": { "x": 74.0, "y": 99.0 }, "neighbors": ["N6", "N8", "N121", "N153"] },
                { "id": "N8", "name": "Pasillo Modulo 3", "plantaId": "planta_1", "position": { "x": 81.5, "y": 99.0 }, "neighbors": ["N7", "N9"] },
                { "id": "N9", "name": "Pasillo Modulo 3", "plantaId": "planta_1", "position": { "x": 89.0, "y": 99.0 }, "neighbors": ["N8", "N10"] },
                { "id": "N10", "name": "Pasillo Modulo 3", "plantaId": "planta_1", "position": { "x": 96.5, "y": 99.0 }, "neighbors": ["N9", "N11"] },
                { "id": "N11", "name": "Pasillo Modulo 3", "plantaId": "planta_1", "position": { "x": 104.0, "y": 99.0 }, "neighbors": ["N10", "N12"] },
                { "id": "N12", "name": "Pasillo Modulo 3", "plantaId": "planta_1", "position": { "x": 111.5, "y": 99.0 }, "neighbors": ["N11", "N13", "N118"] },
                { "id": "N13", "name": "Inicio Modulo 3", "plantaId": "planta_1", "position": { "x": 119.0, "y": 99.0 }, "neighbors": ["N12", "N15", "N54", "N82", "N85"] },
                { "id": "N81", "name": "Inicio escaleras modulo 3 para planta inferior", "plantaId": "planta_1", "position": { "x": 120.0, "y": 87.0 }, "neighbors": ["N17", "N18", "N215"] },
                { "id": "N82", "name": "Inicio escaleras modulo 3 para planta superior", "plantaId": "planta_1", "position": { "x": 120.0, "y": 95.0 }, "neighbors": ["N15", "N13"] },
                { "id": "N153", "name": "Escaleras intermedias modulo 3", "plantaId": "planta_1", "position": { "x": 79.0, "y": 101.0 }, "neighbors": ["N7", "N422"] },

                # PASILLO PRINCIPAL
                { "id": "N15", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 124.0, "y": 95.0 }, "neighbors": ["N54", "N16", "N13", "N82", "N85"] },
                { "id": "N16", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 124.0, "y": 90.0 }, "neighbors": ["N15", "N17", "N55", "N56"] },
                { "id": "N17", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 124.0, "y": 85.0 }, "neighbors": ["N16", "N18", "N57", "N81"] },
                { "id": "N18", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 124.0, "y": 80.0 }, "neighbors": ["N17", "N19", "N57", "N58", "N41", "N81"] },
                { "id": "N19", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 124.0, "y": 75.0 }, "neighbors": ["N18", "N20", "N58", "N59", "N41"] },
                { "id": "N20", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 124.0, "y": 70.0 }, "neighbors": ["N19", "N21", "N60", "N79"] },
                { "id": "N21", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 124.0, "y": 65.0 }, "neighbors": ["N20", "N22", "N60"] },
                { "id": "N22", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 124.0, "y": 60.0 }, "neighbors": ["N21", "N23", "N61", "N62", "N80"] },
                { "id": "N23", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 124.0, "y": 55.0 }, "neighbors": ["N22", "N24", "N62", "N63", "N68"] },
                { "id": "N24", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 124.0, "y": 50.0 }, "neighbors": ["N23", "N25", "N63", "N64", "N68", "N84"] },
                { "id": "N25", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 124.0, "y": 45.0 }, "neighbors": ["N24", "N26", "N65"] },
                { "id": "N26", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 124.0, "y": 40.0 }, "neighbors": ["N25", "N27", "N65"] },
                { "id": "N27", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 124.0, "y": 35.0 }, "neighbors": ["N26", "N28", "N66", "N67", "N83", "N132"] },
                { "id": "N28", "name": "Pasillo principal Inicio", "plantaId": "planta_1", "position": { "x": 124.0, "y": 30.0 }, "neighbors": ["N27", "N67", "N124"] },

                # PASILLO LATERAL (N54-N67)
                { "id": "N54", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 128.0, "y": 99.0 }, "neighbors": ["N15", "N55", "N69", "N13", "N85"] },
                { "id": "N55", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 128.0, "y": 94.0 }, "neighbors": ["N54","N56", "N16", "N114"] },
                { "id": "N56", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 128.0, "y": 89.0 }, "neighbors": ["N55", "N16", "N57", "N114"] },
                { "id": "N57", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 128.0, "y": 84.0 }, "neighbors": ["N56", "N58", "N17", "N18"] },
                { "id": "N58", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 128.0, "y": 77.5 }, "neighbors": ["N57", "N59", "N18", "N19"] },
                { "id": "N59", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 128.0, "y": 72.5 }, "neighbors": ["N58", "N60", "N19", "N79"] },
                { "id": "N60", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 128.0, "y": 67.5 }, "neighbors": ["N59", "N61", "N20", "N21", "N115"] },
                { "id": "N61", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 128.0, "y": 62.5 }, "neighbors": ["N60", "N62", "N22"] },
                { "id": "N62", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 128.0, "y": 57.5 }, "neighbors": ["N61", "N63", "N22", "N23"] },
                { "id": "N63", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 128.0, "y": 52.5 }, "neighbors": ["N62", "N64", "N23", "N24", "N68"] },
                { "id": "N64", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 128.0, "y": 47.5 }, "neighbors": ["N63", "N65", "N24"] },
                { "id": "N65", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 128.0, "y": 42.5 }, "neighbors": ["N64", "N66", "N25", "N26", "N116"] },
                { "id": "N66", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 128.0, "y": 37.5 }, "neighbors": ["N65", "N67", "N27", "N132"] },
                { "id": "N67", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 128.0, "y": 32.5 }, "neighbors": ["N66", "N27", "N28", "N132"] },


                # MODULO 2
                { "id": "N29", "name": "Final Modulo 2", "plantaId": "planta_1", "position": { "x": 30.0, "y": 75.0 }, "neighbors": ["N30", "N106"] },
                { "id": "N30", "name": "Pasillo Modulo 2", "plantaId": "planta_1", "position": { "x": 37.5, "y": 75.0 }, "neighbors": ["N29", "N31"] },
                { "id": "N31", "name": "Pasillo Modulo 2", "plantaId": "planta_1", "position": { "x": 45.0, "y": 75.0 }, "neighbors": ["N30", "N32", "N122"] },
                { "id": "N32", "name": "Pasillo Modulo 2", "plantaId": "planta_1", "position": { "x": 52.5, "y": 75.0 }, "neighbors": ["N31", "N33"] },
                { "id": "N33", "name": "Pasillo Modulo 2", "plantaId": "planta_1", "position": { "x": 60.0, "y": 75.0 }, "neighbors": ["N32", "N34"] },
                { "id": "N34", "name": "Pasillo Modulo 2", "plantaId": "planta_1", "position": { "x": 67.5, "y": 75.0 }, "neighbors": ["N33", "N35"] },
                { "id": "N35", "name": "Pasillo Modulo 2", "plantaId": "planta_1", "position": { "x": 75.0, "y": 75.0 }, "neighbors": ["N34", "N36", "N152"] },
                { "id": "N36", "name": "Pasillo Modulo 2", "plantaId": "planta_1", "position": { "x": 82.5, "y": 75.0 }, "neighbors": ["N35", "N37"] },
                { "id": "N37", "name": "Pasillo Modulo 2", "plantaId": "planta_1", "position": { "x": 90.0, "y": 75.0 }, "neighbors": ["N36", "N38"] },
                { "id": "N38", "name": "Pasillo Modulo 2", "plantaId": "planta_1", "position": { "x": 97.5, "y": 75.0 }, "neighbors": ["N37", "N39"] },
                { "id": "N39", "name": "Pasillo Modulo 2", "plantaId": "planta_1", "position": { "x": 105.0, "y": 75.0 }, "neighbors": ["N38", "N40"] },
                { "id": "N40", "name": "Pasillo Modulo 2", "plantaId": "planta_1", "position": { "x": 112.5, "y": 75.0 }, "neighbors": ["N39", "N41", "N119"] },
                { "id": "N41", "name": "Inicio Modulo 2", "plantaId": "planta_1", "position": { "x": 120.0, "y": 76.5 }, "neighbors": ["N40", "N19", "N79", "N18"] },
                { "id": "N79", "name": "Inicio escaleras modulo 2 para planta superior", "plantaId": "planta_1", "position": { "x": 120.0, "y": 72.5 }, "neighbors": ["N41", "N59", "N20"] },
                { "id": "N80", "name": "Inicio escaleras modulo 2 para planta inferior", "plantaId": "planta_1", "position": { "x": 120.0, "y": 63.0 }, "neighbors": ["N22", "N257"] },
                { "id": "N152", "name": "Escaleras intermedias modulo 2", "plantaId": "planta_1", "position": { "x": 79.0, "y": 77.0 }, "neighbors": ["N35", "N421"] },


                # MODULO 1
                { "id": "N42", "name": "Final Modulo 1", "plantaId": "planta_1", "position": { "x": 30.0, "y": 51.0 }, "neighbors": ["N43", "N107"] },
                { "id": "N43", "name": "Pasillo Modulo 1", "plantaId": "planta_1", "position": { "x": 37.5, "y": 51.0 }, "neighbors": ["N42", "N44"] },
                { "id": "N44", "name": "Pasillo Modulo 1", "plantaId": "planta_1", "position": { "x": 45.0, "y": 51.0 }, "neighbors": ["N43", "N45"] },
                { "id": "N45", "name": "Pasillo Modulo 1", "plantaId": "planta_1", "position": { "x": 52.5, "y": 51.0 }, "neighbors": ["N44", "N46"] },
                { "id": "N46", "name": "Pasillo Modulo 1", "plantaId": "planta_1", "position": { "x": 60.0, "y": 51.0 }, "neighbors": ["N45", "N47"] },
                { "id": "N47", "name": "Pasillo Modulo 1", "plantaId": "planta_1", "position": { "x": 67.5, "y": 51.0 }, "neighbors": ["N46", "N48", "N127"] },
                { "id": "N48", "name": "Pasillo Modulo 1", "plantaId": "planta_1", "position": { "x": 75.0, "y": 51.0 }, "neighbors": ["N47", "N49", "N151"] },
                { "id": "N49", "name": "Pasillo Modulo 1", "plantaId": "planta_1", "position": { "x": 82.5, "y": 51.0 }, "neighbors": ["N48", "N50"] },
                { "id": "N50", "name": "Pasillo Modulo 1", "plantaId": "planta_1", "position": { "x": 90.0, "y": 51.0 }, "neighbors": ["N49", "N51"] },
                { "id": "N51", "name": "Pasillo Modulo 1", "plantaId": "planta_1", "position": { "x": 97.5, "y": 51.0 }, "neighbors": ["N50", "N52"] },
                { "id": "N52", "name": "Pasillo Modulo 1", "plantaId": "planta_1", "position": { "x": 105.0, "y": 51.0 }, "neighbors": ["N51", "N53"] },
                { "id": "N53", "name": "Pasillo Modulo 1", "plantaId": "planta_1", "position": { "x": 112.5, "y": 51.0 }, "neighbors": ["N52", "N68", "N120"] },
                { "id": "N68", "name": "Inicio Modulo 1", "plantaId": "planta_1", "position": { "x": 120.0, "y": 52.5 }, "neighbors": ["N53", "N24", "N23", "N63"] },
                { "id": "N83", "name": "Inicio escaleras modulo 1 para planta inferior", "plantaId": "planta_1", "position": { "x": 120.0, "y": 38.0 }, "neighbors": ["N27", "N273"] },
                { "id": "N84", "name": "Inicio escaleras modulo 1 para planta superior", "plantaId": "planta_1", "position": { "x": 120.0, "y": 47.5 }, "neighbors": ["N24"] },
                { "id": "N151", "name": "Escaleras intermedias modulo 1", "plantaId": "planta_1", "position": { "x": 79.0, "y": 53.0 }, "neighbors": ["N48", "N420"] },

                # MODULO3 - PASILLO - BIBLIOTECA
                { "id": "N69", "name": "Inicio Pasillo", "plantaId": "planta_1", "position": { "x": 135.5, "y": 98.5 }, "neighbors": ["N70", "N54"] },
                { "id": "N70", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 143.0, "y": 98.5 }, "neighbors": ["N69", "N71"] },
                { "id": "N71", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 150.5, "y": 98.5 }, "neighbors": ["N70", "N72", "N131"] },
                { "id": "N72", "name": "Acceso Biblioteca Planta 1", "plantaId": "planta_1", "position": { "x": 158.0, "y": 98.5 }, "neighbors": ["N71", "N73"] },
                { "id": "N73", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 165.5, "y": 98.5 }, "neighbors": ["N72", "N74"] },
                { "id": "N74", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 173.0, "y": 98.5 }, "neighbors": ["N73", "N75"] },
                { "id": "N75", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 180.5, "y": 98.5 }, "neighbors": ["N74", "N76"] },
                { "id": "N76", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 188.0, "y": 98.5 }, "neighbors": ["N75", "N77"] },
                { "id": "N77", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 195.5, "y": 98.5 }, "neighbors": ["N76", "N78"] },
                { "id": "N78", "name": "Pasillo principal", "plantaId": "planta_1", "position": { "x": 203.0, "y": 98.5 }, "neighbors": ["N77",] },

                # MODULO 4
                { "id": "N85", "name": "Pasillo principal - Modulo 4 Inicio", "plantaId": "planta_1", "position": { "x": 124.0, "y": 102.5 }, "neighbors": ["N86", "N54", "N13", "N15"] },
                { "id": "N86", "name": "Pasillo principal - Modulo 4", "plantaId": "planta_1", "position": { "x": 124.0, "y": 107.5 }, "neighbors": ["N85", "N87", "N130"] },
                { "id": "N87", "name": "Pasillo principal - Modulo 4", "plantaId": "planta_1", "position": { "x": 124.0, "y": 112.5 }, "neighbors": ["N86", "N88"] },
                { "id": "N88", "name": "Pasillo principal - Modulo 4", "plantaId": "planta_1", "position": { "x": 124.0, "y": 117.5 }, "neighbors": ["N87", "N89"] },
                { "id": "N89", "name": "Pasillo principal - Modulo 4", "plantaId": "planta_1", "position": { "x": 124.0, "y": 122.5 }, "neighbors": ["N88"] },

                # PASILLO PARALEO AL PRINCIPAL
                { "id": "N90", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 22.5, "y": 99.0 }, "neighbors": ["N1", "N91", "N92", "N93", "N108"] },
                { "id": "N91", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 22.5, "y": 104.0 }, "neighbors": ["N90", "N92", "N93"] },
                { "id": "N92", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 17.0, "y": 104.0 }, "neighbors": ["N90", "N91", "N93", "N123"] },
                { "id": "N93", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 17.0, "y": 99.0 }, "neighbors": ["N90", "N91", "N92","N94", "N108"] },
                { "id": "N94", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 17.0, "y": 94.0 }, "neighbors": ["N93", "N95", "N125"] },
                { "id": "N95", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 17.0, "y": 89.0 }, "neighbors": ["N94", "N96", "N125"] },
                { "id": "N96", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 17.0, "y": 84.0 }, "neighbors": ["N95", "N97", "N109"] },
                { "id": "N97", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 17.0, "y": 79.0 }, "neighbors": ["N96", "N98", "N106", "N117"] },
                { "id": "N98", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 17.0, "y": 74.0 }, "neighbors": ["N97", "N99", "N106", "N110"] },
                { "id": "N99", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 17.0, "y": 69.0 }, "neighbors": ["N98", "N100"] },
                { "id": "N100", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 17.0, "y": 64.0 }, "neighbors": ["N99", "N101"] },
                { "id": "N101", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 17.0, "y": 59.0 }, "neighbors": ["N100", "N102", "N111"] },
                { "id": "N102", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 17.0, "y": 54.0 }, "neighbors": ["N101", "N103", "N107"] },
                { "id": "N103", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 17.0, "y": 49.0 }, "neighbors": ["N102", "N104", "N107", "N112"] },
                { "id": "N104", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 17.0, "y": 44.0 }, "neighbors": ["N103", "N105", "N126"] },
                { "id": "N105", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 17.0, "y": 39.0 }, "neighbors": ["N104", "N113"] },
                { "id": "N106", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 22.5, "y": 75.0 }, "neighbors": ["N97", "N98", "N29", "N110"] },
                { "id": "N107", "name": "Pasillo Oeste", "plantaId": "planta_1", "position": { "x": 22.5, "y": 51.0 }, "neighbors": ["N102", "N103", "N42", "N112"] },
                { "id": "N108", "name": "Inicio escaleras modulo 3 para planta superior", "plantaId": "planta_1", "position": { "x": 21.0, "y": 95.0 }, "neighbors": ["N90", "N93"] },
                { "id": "N109", "name": "Inicio escaleras modulo 3 para planta inferior", "plantaId": "planta_1", "position": { "x": 21.0, "y": 86.0 }, "neighbors": ["N96", "N307"] },
                { "id": "N110", "name": "Inicio escaleras modulo 2 para planta superior", "plantaId": "planta_1", "position": { "x": 21.0, "y": 71.0 }, "neighbors": ["N98", "N106"] },
                { "id": "N111", "name": "Inicio escaleras modulo 2 para planta inferior", "plantaId": "planta_1", "position": { "x": 21.0, "y": 62.0 }, "neighbors": ["N101", "N309"] },
                { "id": "N112", "name": "Inicio escaleras modulo 1 para planta superior", "plantaId": "planta_1", "position": { "x": 21.0, "y": 47.0 }, "neighbors": ["N103", "N107"] },
                { "id": "N113", "name": "Inicio escaleras modulo 1 para planta inferior", "plantaId": "planta_1", "position": { "x": 21.0, "y": 38.0 }, "neighbors": ["N105", "N311"] },

                # Secretaría - Salón de actos
                { "id": "N132", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 131.5, "y": 35.0 }, "neighbors": ["N27", "N66","N67", "N133"] },
                { "id": "N133", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 139.0, "y": 35.0 }, "neighbors": ["N132", "N134", "N150"] },
                { "id": "N134", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 144.25, "y": 33.0 }, "neighbors": ["N133", "N135", "N150"] },
                { "id": "N135", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 149.5, "y": 31.0 }, "neighbors": ["N134", "N136", "N139"] },

                { "id": "N136", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 157.0, "y": 31.0 }, "neighbors": ["N135", "N137", "N139", "N140"] },
                { "id": "N137", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 164.5, "y": 31.0 }, "neighbors": ["N136", "N138", "N140", "N141"] },
                { "id": "N138", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 172.0, "y": 31.0 }, "neighbors": ["N137", "N141", "N142", "N149"] },

                { "id": "N139", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 155.0, "y": 36.0 }, "neighbors": ["N140", "N143", "N135", "N136"] },
                { "id": "N140", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 162.5, "y": 36.0 }, "neighbors": ["N139", "N141", "N143", "N144", "N136", "N137"] },
                { "id": "N141", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 170.0, "y": 36.0 }, "neighbors": ["N140", "N142", "N144", "N145", "N137", "N138"] },
                { "id": "N142", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 177.5, "y": 36.0 }, "neighbors": ["N141","N145", "N146", "N138"] },

                { "id": "N143", "name": "Acceso Secretaría", "plantaId": "planta_1", "position": { "x": 158.75, "y": 41.0 }, "neighbors": ["N144", "N139", "N140", "N128"] },
                { "id": "N144", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 166.0, "y": 41.0 }, "neighbors": ["N143", "N145", "N147", "N148", "N140", "N141"] },
                { "id": "N145", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 173.5, "y": 41.0 }, "neighbors": ["N144", "N146", "N147", "N148", "N141", "N142"] },
                { "id": "N146", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 181.0, "y": 41.0 }, "neighbors": ["N145","N148", "N142"] },

                { "id": "N147", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 166.0, "y": 46.0 }, "neighbors": ["N148", "N144", "N145"] },
                { "id": "N148", "name": "Entrada Factultad", "plantaId": "planta_1", "position": { "x": 173.5, "y": 44.5 }, "neighbors": ["N147", "N144", "N145", "N146", "N129"] },

                { "id": "N150", "name": "Escaleras", "plantaId": "planta_1", "position": { "x": 144.0, "y": 39.0 }, "neighbors": ["N133", "N134","N419"] },


                # NODOS --> Points of interest
                { "id": "N114", "name": "Aseos MOD 3 - Planta 1", "plantaId": "planta_1", "position": { "x": 133.0, "y": 91.5 }, "neighbors": ["N55", "N56"] },
                { "id": "N115", "name": "Aseos MOD 2 - Planta 1", "plantaId": "planta_1", "position": { "x": 134.0, "y": 67.5 }, "neighbors": ["N60"] },
                { "id": "N116", "name": "Consejería", "plantaId": "planta_1", "position": { "x": 134.0, "y": 42.5 }, "neighbors": ["N65"] },
                { "id": "N117", "name": "Reprografía", "plantaId": "planta_1", "position": { "x": 11.0, "y": 79.0 }, "neighbors": ["N97"] },
                { "id": "N118", "name": "Laboratorios Modulo 3", "plantaId": "planta_1", "position": { "x": 111.5, "y": 93.0 }, "neighbors": ["N12"] },
                { "id": "N119", "name": "Laboratorios Modulo 2", "plantaId": "planta_1", "position": { "x": 112.5, "y": 69.0 }, "neighbors": ["N40"] },
                { "id": "N120", "name": "Laboratorios Modulo 1", "plantaId": "planta_1", "position": { "x": 112.5, "y": 45.0 }, "neighbors": ["N53"] },
                { "id": "N121", "name": "Lab 3.1.6 - Informática", "plantaId": "planta_1", "position": { "x": 74.0, "y": 93.0 }, "neighbors": ["N7"] },
                { "id": "N122", "name": "Lab 2.1.7 - Computadores", "plantaId": "planta_1", "position": { "x": 45.0, "y": 69.0 }, "neighbors": ["N31"] },
                { "id": "N123", "name": "Lab 3.1.13 - Electrónica", "plantaId": "planta_1", "position": { "x": 8.0, "y": 104.0 }, "neighbors": ["N92"] },
                { "id": "N124", "name": "Aparcamiento de bicicletas", "plantaId": "planta_1", "position": { "x": 124.0, "y": 25.0 }, "neighbors": ["N28"] },
                { "id": "N125", "name": "Aseos MOD 3 - Planta 1 - Oeste", "plantaId": "planta_1", "position": { "x": 8.0, "y": 91.5 }, "neighbors": ["N94", "N95"] },
                { "id": "N126", "name": "Aseos MOD 1 - Planta 1 - Oeste", "plantaId": "planta_1", "position": { "x": 8.0, "y": 42.5 }, "neighbors": ["N104"] },
                { "id": "N127", "name": "Lab 1.1.5 - Telecomunicaciones", "plantaId": "planta_1", "position": { "x": 67.5, "y": 45.0 }, "neighbors": ["N47"] },
                { "id": "N128", "name": "Acceso a Secretaría", "plantaId": "planta_1", "position": { "x":161.0, "y": 45.0 }, "neighbors": ["N143"] },
                { "id": "N129", "name": "Salón de Actos - 1", "plantaId": "planta_1", "position": { "x":180.0, "y": 50.0 }, "neighbors": ["N148"] },
                { "id": "N130", "name": "Aulas Módulo 4 - Planta 1", "plantaId": "planta_1", "position": { "x":116.5, "y": 107.5 }, "neighbors": ["N86"] },
                { "id": "N131", "name": "Acceso a Biblioteca - Hemeroteca", "plantaId": "planta_1", "position": { "x":150.5, "y": 93.0 }, "neighbors": ["N71"] },
                { "id": "N149", "name": "Acceso/Salida Factultad", "plantaId": "planta_1", "position": { "x": 179.5, "y": 31.0 }, "neighbors": ["N138"] },



            ],
            "pois": [
                { "id": "POI1", "nombre": "Aseos MOD 3 - Planta 1", "nodoId": "N114", "plantaId": "planta_1" },
                { "id": "POI2", "nombre": "Aseos MOD 2 - Planta 1", "nodoId": "N115", "plantaId": "planta_1" },
                { "id": "POI3", "nombre": "Consejería", "nodoId": "N116", "plantaId": "planta_1" },
                { "id": "POI4", "nombre": "Reprografía", "nodoId": "N117", "plantaId": "planta_1" },
                { "id": "POI5", "nombre": "Laboratorios Modulo 3", "nodoId": "N118", "plantaId": "planta_1" },
                { "id": "POI6", "nombre": "Laboratorios Modulo 2", "nodoId": "N119", "plantaId": "planta_1" },
                { "id": "POI7", "nombre": "Laboratorios Modulo 1", "nodoId": "N120", "plantaId": "planta_1" },
                { "id": "POI8", "nombre": "Lab 3.1.6 - Informática", "nodoId": "N121", "plantaId": "planta_1" },
                { "id": "POI9", "nombre": "Lab 2.1.7 - Computadores", "nodoId": "N122", "plantaId": "planta_1" },
                { "id": "POI10", "nombre": "Lab 3.1.13 - Electrónica", "nodoId": "N123", "plantaId": "planta_1" },
                { "id": "POI11", "nombre": "Aseos MOD 3 - Planta 1 - Oeste", "nodoId": "N125", "plantaId": "planta_1" },
                { "id": "POI12", "nombre": "Aseos MOD 1 - Planta 1 - Oeste", "nodoId": "N126", "plantaId": "planta_1" },
                { "id": "POI13", "nombre": "Aparcamiento de bicicletas", "nodoId": "N124", "plantaId": "planta_1" },
                { "id": "POI14", "nombre": "Lab 1.1.5 - Telecomunicaciones", "nodoId": "N127", "plantaId": "planta_1" },
                { "id": "POI15", "nombre": "Acceso a Secretaría", "nodoId": "N128", "plantaId": "planta_1" },
                { "id": "POI16", "nombre": "Salón de Actos - 1", "nodoId": "N129", "plantaId": "planta_1" },
                { "id": "POI17", "nombre": "Aulas Módulo 4 - Planta 1", "nodoId": "N130", "plantaId": "planta_1" },
                { "id": "POI18", "nombre": "Acceso a Biblioteca - Hemeroteca", "nodoId": "N131", "plantaId": "planta_1" },
                { "id": "POI19", "nombre": "Acceso/Salida Factultad", "nodoId": "N149", "plantaId": "planta_1" },
            ]
        },

        # ==========================================
        # PLANTA 2: PLANTA SUPERIOR
        # ==========================================
        {
            "plantaId": "planta_2",
            "nombre": "Primera Planta",
            "nivel": 2,
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