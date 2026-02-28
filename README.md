# Microservicio Productor de Ubicaciones

Microservicio Spring Boot responsable de generar y publicar eventos de ubicación de vehículos hacia el topic de Kafka `ubicaciones_vehiculos`. Actúa como el punto de entrada de datos en el pipeline de seguimiento vehicular.

---

## Tabla de contenidos

- [Descripción](#descripción)
- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
  - [Local](#ejecución-local)
  - [Docker](#ejecución-con-docker)
- [API REST](#api-rest)
- [Topic Kafka](#topic-kafka)

---

## Descripción

Este microservicio cumple dos roles:

1. **Publicación automática (scheduler):** Cada 60 segundos selecciona aleatoriamente uno de los vehículos de la flota (patentes `BBBB12`, `CCDD34`, `FFGG56`), genera una ubicación simulada con coordenadas aleatorias alrededor de Santiago de Chile y la publica en Kafka.
2. **Publicación manual (REST API):** Expone un endpoint `POST /api/ubicaciones` para enviar ubicaciones de forma manual.

---

## Arquitectura

```
┌──────────────────────────────────────────────────────────┐
│                  PRODUCTOR-UBICACIONES                   │
│                                                          │
│  ┌───────────────────────┐   ┌────────────────────────┐  │
│  │  UbicacionController  │   │    UbicacionService     │  │
│  │  POST /api/ubicaciones│──▶│  publicar()             │  │
│  └───────────────────────┘   │  publicarUbicacion      │  │
│                              │  Automatica() @Scheduled│  │
│                              └───────────┬─────────────┘  │
└──────────────────────────────────────────┼───────────────┘
                                           │ publica mensaje JSON
                                           ▼
                        ┌──────────────────────────────┐
                        │  TOPIC: ubicaciones_vehiculos │
                        │  (3 particiones, 2 réplicas)  │
                        └──────────────────────────────┘
                                │               │
                                ▼               ▼
                    consumidor-procesa-   consumidor-
                        senales           monitoreo
```

El productor publica mensajes JSON con la siguiente estructura:

```json
{
  "patente": "BBBB12",
  "latitud": -33.4512,
  "longitud": -70.6423,
  "fechaActualizacion": "2025-10-15T14:30:00"
}
```

---

## Tecnologías

| Tecnología       | Versión  | Uso                                 |
|------------------|----------|-------------------------------------|
| Java             | 21       | Lenguaje principal                  |
| Spring Boot      | 4.0.3    | Framework de aplicación             |
| Spring Kafka     | -        | Integración con Apache Kafka        |
| Spring Web       | -        | REST API                            |
| Lombok           | -        | Reducción de boilerplate            |
| Apache Kafka     | 7.4.4    | Broker de mensajes (imagen Confluent)|
| Maven            | 3.9      | Gestión de dependencias y build     |
| Docker           | -        | Contenerización                     |

---

## Estructura del proyecto

```
productor-ubicaciones/
├── src/
│   └── main/
│       ├── java/com/musabeli/productor_ubicaciones/
│       │   ├── ProductorUbicacionesApplication.java   # Entry point (@EnableScheduling)
│       │   ├── config/
│       │   │   └── KafkaConfig.java                   # Configuración del topic y producer
│       │   ├── controllers/
│       │   │   └── UbicacionController.java            # REST: POST /api/ubicaciones
│       │   ├── dtos/
│       │   │   ├── UbicacionRequestDto.java            # Payload de entrada HTTP
│       │   │   └── UbicacionVehiculoDto.java           # Mensaje Kafka (JSON)
│       │   └── services/
│       │       └── UbicacionService.java               # Lógica de negocio + scheduler
│       └── resources/
│           └── application.properties                  # Configuración de la app
├── docker-compose.yml                                  # Despliegue Docker standalone
├── Dockerfile                                          # Build multi-etapa
└── pom.xml                                            # Dependencias Maven
```

---

## Configuración

### `application.properties`

```properties
spring.application.name=productor-ubicaciones
server.port=8081

# Para pruebas locales (fuera de Docker)
spring.kafka.bootstrap-servers=localhost:29092,localhost:39092,localhost:49092

# Para produccion (dentro de Docker) - descomentar antes de dockerizar
#spring.kafka.bootstrap-servers=kafka-1:9092,kafka-2:9092,kafka-3:9092
```

> Los serializadores de Kafka (`StringSerializer`) se configuran programáticamente en `KafkaConfig.java`.

### Variables de entorno (Docker)

| Variable                          | Valor por defecto                        | Descripción                    |
|-----------------------------------|------------------------------------------|--------------------------------|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS`  | `kafka-1:9092,kafka-2:9092,kafka-3:9092` | Brokers de Kafka               |

---

## Ejecución

### Pre-requisitos

- Java 21+
- Maven 3.9+
- Kafka corriendo (ver [kafka-server/](../kafka-server/))

### Ejecución local

```bash
cd productor-ubicaciones

# Compilar y ejecutar
mvn spring-boot:run
```

El servicio quedará disponible en `http://localhost:8081`.

> Asegúrate de que el cluster Kafka esté activo antes de iniciar el servicio.

### Ejecución con Docker

```bash
# Desde la raíz del proyecto, construir la imagen
cd productor-ubicaciones
docker build -t productor-ubicaciones .

# Ejecutar conectado a la red kafka-net
docker run -d \
  --name productor-ubicaciones \
  --network kafka-net \
  -p 8081:8081 \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka-1:9092,kafka-2:9092,kafka-3:9092 \
  productor-ubicaciones
```

---

## API REST

### `POST /api/ubicaciones`

Publica manualmente una ubicación de vehículo en Kafka.

**URL:** `http://localhost:8081/api/ubicaciones`

**Request Body:**

```json
{
  "patente": "BBBB12",
  "latitud": -33.4569,
  "longitud": -70.6483
}
```

| Campo     | Tipo       | Requerido | Descripción                    |
|-----------|------------|-----------|--------------------------------|
| `patente` | String     | Sí        | Identificador del vehículo     |
| `latitud` | BigDecimal | Sí        | Latitud GPS                    |
| `longitud`| BigDecimal | Sí        | Longitud GPS                   |

**Ejemplo con curl:**

```bash
curl -X POST http://localhost:8081/api/ubicaciones \
  -H "Content-Type: application/json" \
  -d '{"patente":"BBBB12","latitud":-33.4569,"longitud":-70.6483}'
```

---

## Topic Kafka

| Propiedad    | Valor                   |
|--------------|-------------------------|
| Nombre       | `ubicaciones_vehiculos` |
| Particiones  | 3                       |
| Réplicas     | 2                       |
| Serialización| String (JSON embebido)  |

El topic es creado automáticamente por el bean `NewTopic` definido en `KafkaConfig.java` al iniciar la aplicación.

---

## Scheduler de ubicaciones

El servicio publica automáticamente cada **60 segundos** una ubicación simulada para **un vehículo seleccionado aleatoriamente** de la siguiente flota:

| Patente  | Zona de simulación    |
|----------|-----------------------|
| `BBBB12` | Santiago de Chile     |
| `CCDD34` | Santiago de Chile     |
| `FFGG56` | Santiago de Chile     |

Las coordenadas se generan con variación aleatoria de ±0.05 grados alrededor de `-33.45, -70.65`. La `patente` del vehículo seleccionado se usa como clave del mensaje Kafka para garantizar orden por vehículo dentro de la partición.
