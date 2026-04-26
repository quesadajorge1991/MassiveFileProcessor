# 🚀 MassiveFileProcessor - Procesador de Archivos Masivo con Paralelismo

![Java](https://img.shields.io/badge/Java-17-007396?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-6DB33F?logo=springboot)
![License](https://img.shields.io/badge/License-MIT-green)

## 📋 Descripción

**MassiveFileProcessor** es una API REST diseñada para procesar archivos CSV de gran tamaño utilizando **procesamiento paralelo**. El sistema divide archivos enormes en chunks más pequeños y los procesa simultáneamente usando múltiples hilos, logrando mejoras significativas de rendimiento.

### 🎯 ¿Qué problema resuelve?

Las empresas reciben diariamente archivos masivos (logs, ventas, datos de usuarios) que necesitan procesar. Hacerlo de forma secuencial es lento e ineficiente. Este sistema:

- **Divide** archivos grandes en partes manejables
- **Procesa** cada parte en paralelo usando todos los núcleos del CPU
- **Consolida** los resultados en un reporte único
- **Mide** el rendimiento para demostrar la mejora

---

## ✨ Características

| Característica | Descripción |
|----------------|-------------|
| ⚡ **Procesamiento Paralelo** | Divide archivos en chunks y los procesa simultáneamente |
| 📊 **Benchmarks Integrados** | Mide speedup, throughput y eficiencia |
| 🔄 **Asincrónico** | Procesa archivos sin bloquear al usuario |
| 📈 **Seguimiento en Tiempo Real** | Consulta el progreso del procesamiento |
| 🛡️ **Tolerancia a Fallos** | Continúa procesando chunks válidos aunque uno falle |
| 🧪 **Validación de Datos** | Identifica y reporta registros inválidos |
| 💾 **Sin Base de Datos** | Procesamiento puro, sin dependencias externas |

---

## 🛠️ Tecnologías Utilizadas

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Spring Boot | 3.1.5 | Framework principal |
| Java | 17 | Lenguaje |
| Maven | 3.8+ | Gestión de dependencias |
| OpenCSV | 5.8 | Procesamiento de CSV |
| Lombok | 1.18.30 | Reducción de boilerplate |
| Micrometer | - | Métricas de rendimiento |

---

## 📊 Resultados de Rendimiento

Benchmarks realizados con archivo de **10,000 registros**:

| Hilos | Tiempo (ms) | Throughput (rec/s) | Speedup |
|-------|-------------|--------------------|---------|
| 1 (secuencial) | 1250 | 8,000 | 1.00x |
| 2 | 650 | 15,384 | 1.92x |
| 4 | 350 | 28,571 | 3.57x |
| 8 | 185 | 54,054 | 6.76x |

> **Conclusión:** Procesar con 8 hilos es **~6.7 veces más rápido** que hacerlo en un solo hilo.

---

Proceso:
Archivo → Splitter → Chunks → Threads → Procesar → Consolidar → Resultado

Proceso:
Archivo → Splitter → Chunks → Threads → Procesar → Consolidar → Resultado


---

## 🚀 Instalación y Ejecución

### Requisitos Previos

- Java 17 o superior
- Maven 3.8+
- Git

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/MassiveFileProcessor.git
cd MassiveFileProcessor

# 2. Compilar
mvn clean package

# 3. Ejecutar
java -jar target/MassiveFileProcessor-1.0.0.jar

La aplicación estará disponible en: http://localhost:8080

📡 Endpoints de la API
Subir Archivo
POST /api/upload
Parámetros:

Parámetro	 Tipo	     Descripción
file	     File	     Archivo CSV a procesar
chunks	   Integer	 Número de chunks (hilos) para procesar
curl -X POST -F "file=@datos.csv" -F "chunks=8" http://localhost:8080/api/upload

Respuesta:
{
  "processId": "abc123-def456",
  "message": "Archivo recibido. Procesamiento iniciado.",
  "chunks": "8"
}

2. Consultar Estado
GET /api/status/{processId}
Ejemplo:
curl http://localhost:8080/api/status/abc123-def456

Respuesta:
{
  "processId": "abc123-def456",
  "fileName": "datos.csv",
  "status": "RUNNING",
  "totalChunks": 8,
  "completedChunks": 4,
  "progress": 50
}

3. Obtener Resultado
GET /api/result/{processId}

Ejemplo:
curl http://localhost:8080/api/result/abc123-def456

Respuesta:
{
  "processId": "abc123-def456",
  "fileName": "datos.csv",
  "totalRows": 100000,
  "validRows": 98500,
  "invalidRows": 1500,
  "totalTimeMs": 1250,
  "throughputPerSec": 80000,
  "threadsUsed": 8,
  "sampleErrors": [
    "Error en línea: usuario,email",
    "Error en línea: ,,"
  ]
}

4. Ejecutar Benchmark
POST /api/benchmark
Ejemplo:
curl -X POST http://localhost:8080/api/benchmark
Respuesta:
[
  {"threads": 1, "timeMs": 1250, "rowsProcessed": 10000, "throughputPerSec": 8000, "speedup": 1.0},
  {"threads": 2, "timeMs": 650, "rowsProcessed": 10000, "throughputPerSec": 15384, "speedup": 1.92},
  {"threads": 4, "timeMs": 350, "rowsProcessed": 10000, "throughputPerSec": 28571, "speedup": 3.57},
  {"threads": 8, "timeMs": 185, "rowsProcessed": 10000, "throughputPerSec": 54054, "speedup": 6.76}
]

🧪 Generar Archivo de Prueba
Windows (PowerShell)
(1..100000 | ForEach-Object { "$_,Usuario$_,user$_@test.com,$(20 + $_ % 50)" }) | Out-File test.csv -Encoding UTF8

Linux / Mac / Git Bash
seq 1 100000 | while read i; do echo "$i,Usuario$i,user$i@test.com,$((20 + i % 50))"; done > test.csv

📁 Estructura del Proyecto
MassiveFileProcessor/
├── src/main/java/com/massivefileprocessor/
│   ├── MassiveFileProcessorApplication.java
│   ├── config/
│   │   └── AsyncConfig.java              # Configuración de hilos
│   ├── controller/
│   │   └── FileProcessController.java    # Endpoints REST
│   ├── model/
│   │   ├── ProcessStatus.java
│   │   ├── ProcessResult.java
│   │   ├── ChunkResult.java
│   │   └── BenchmarkResult.java
│   ├── service/
│   │   ├── FileProcessorService.java     # Lógica principal
│   │   ├── ProgressTracker.java          # Seguimiento
│   │   └── BenchmarkService.java         # Pruebas rendimiento
│   └── util/
│       └── FileSplitter.java             # División de archivos
├── src/main/resources/
│   └── application.properties
└── pom.xml

🔧 Configuración
### application.properties
server.port=8080
spring.servlet.multipart.max-file-size=10GB
spring.servlet.multipart.max-request-size=10GB
logging.level.com.massivefileprocessor=DEBUG

Configuración del Thread Pool
// Número de hilos = número de núcleos del CPU
int cores = Runtime.getRuntime().availableProcessors();
executor.setCorePoolSize(cores);
executor.setMaxPoolSize(cores * 2);

📊 Explicación de Métricas
Métrica	        Definición	              Fórmula
Throughput	    Registros por segundo   	TotalRows / (TimeMs / 1000)
Speedup	         Ganancia vs secuencial	  TimeSecuencial / TimeParalelo
Eficiencia	     Uso de recursos	        Speedup / Núcleos

🐛 Posibles Errores y Soluciones
Error                    	Causa	                                        Solución
Archivo vacío            	Archivo no generado correctamente	            Verificar la creación del archivo CSV
NullPointerException	    startTime no inicializado	                    Reiniciar la aplicación después de corregir
curl: command not found	  Usando PowerShell	                              Usar curl.exe en lugar de curl


🚀 Próximas Mejoras
Soporte para formatos JSON y XML

Procesamiento distribuido con múltiples servidores

Interfaz web con progreso en tiempo real

Exportación de reportes en PDF/Excel

Streaming en tiempo real sin guardar archivo

🤝 Contribuciones
Las contribuciones son bienvenidas. Por favor:

Fork el proyecto

Crea una rama (git checkout -b feature/nueva-funcionalidad)

Commit tus cambios (git commit -m 'Agrega funcionalidad')

Push a la rama (git push origin feature/nueva-funcionalidad)

Abre un Pull Request


📞 Contacto
Autor: Jorge Adrián Quesada Perdomo

GitHub: github.com/quesadajorge1991


