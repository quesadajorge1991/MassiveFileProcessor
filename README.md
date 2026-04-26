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

## 🏗️ Arquitectura
