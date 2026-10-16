# ms-rutaexpress-rabbitmq-admin

Microservicio administrador de RabbitMQ de la plataforma **RutaExpress** (envíos de última milla).
Permite crear, consultar y eliminar colas, exchanges y bindings mediante una API REST, y declara al iniciar la topología de mensajería del sistema (3 flujos de trabajo + sus DLQ).

- **Stack:** Java 21, Spring Boot 4.1.1, Spring AMQP, Spring Security (OAuth2 Resource Server), RabbitMQ 3.13
- **Puerto:** 8087

---

## 1. Topología RabbitMQ

Exchanges:

| Exchange | Tipo | Uso |
|---|---|---|
| `cmd.direct` | direct | Enrutamiento exacto por routing key |
| `cmd.topic` | topic | Enrutamiento por patrón |
| `cmd.dead.dlx` | direct | Dead letter exchange (mensajes no entregados) |

Colas y bindings (3 flujos, cada uno con su DLQ):

| Cola principal | Propósito | DLQ | Binding direct | Binding topic |
|---|---|---|---|---|
| `q.cmd.email` | Email/push al destinatario | `q.cmd.email.dlq` | `email.send` | `email.*` |
| `q.cmd.warehouse` | Ticket de picking / bodega | `q.cmd.warehouse.dlq` | `warehouse.ticket` | `warehouse.#` |
| `q.cmd.label` | Generación de PDF (guía/etiqueta) | `q.cmd.label.dlq` | `label.gen` | `label.*` |

Cada cola principal tiene configurado `x-dead-letter-exchange = cmd.dead.dlx` y un `x-dead-letter-routing-key` propio; la DLQ se enlaza a `cmd.dead.dlx` con esa clave (`email.dlq`, `warehouse.dlq`, `label.dlq`).

Todos los nombres se definen en `src/main/resources/application.yaml` (clave `rutaexpress.rabbit`) y se leen mediante `RabbitNamesProperties`. No hay nombres escritos en el código de negocio.

## 2. Estructura del proyecto

```
src/main/java/cl/rutaexpress/ms_rutaexpress_rabbitmq_admin
├── config
│   ├── RabbitNamesProperties.java   # nombres centralizados (lee application.yaml)
│   ├── RabbitTopologyConfig.java    # beans Queue / Exchange / Binding
│   ├── SecurityConfig.java          # JWT de Azure AD (perfil por defecto)
│   └── LocalSecurityConfig.java     # sin autenticación, solo perfil "local"
├── controller
│   └── RabbitAdminController.java   # endpoints REST
├── service
│   └── RabbitAdminService.java      # única clase que usa AmqpAdmin
├── dto                              # QueueRequest, ExchangeRequest, BindingRequest, QueueInfoResponse
└── exception                        # ResourceNotFound, Conflict, GlobalExceptionHandler
```

## 3. Cómo ejecutarlo

Requisitos: Java 21, Maven y Docker.

```bash
# 1. Levantar RabbitMQ (puertos 5672 y 15672)
docker compose up -d

# 2. Ejecutar el microservicio con el perfil local (sin JWT, solo para pruebas)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Panel de administración: http://localhost:15672 (usuario `guest`, contraseña `guest`).

Variables de entorno opcionales: `RABBIT_HOST`, `RABBIT_PORT`, `RABBIT_USER`, `RABBIT_PASS`, `TENANT_ID`.

## 4. API REST

Base: `/api/rabbit` (requiere rol `ADMIN` cuando no se usa el perfil `local`).

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| POST | `/api/rabbit/queues` | Crea una cola | 201, 400, 409 |
| GET | `/api/rabbit/queues/{name}` | Consulta una cola (mensajes y consumidores) | 200, 404 |
| DELETE | `/api/rabbit/queues/{name}` | Elimina una cola | 204, 404 |
| POST | `/api/rabbit/exchanges` | Crea un exchange | 201, 400, 409 |
| DELETE | `/api/rabbit/exchanges/{name}` | Elimina un exchange | 204 |
| POST | `/api/rabbit/bindings` | Crea un binding cola ← exchange | 201, 400, 404, 409 |
| DELETE | `/api/rabbit/bindings` | Elimina un binding | 204, 400 |

## 5. Matriz de funcionalidades

Alcance de este repositorio: microservicio administrador de RabbitMQ.
Estados: **Implementada**, **Parcial**, **No implementada**. Solo las funcionalidades implementadas y operativas, con evidencia verificable, cuentan para el logro.

Commit de referencia: [`0d4e39e`](https://github.com/Abraham2143/ms-rutaexpress-rabbitmq-admin/commit/0d4e39e)

| N° | Funcionalidad | Estado | Componente responsable | Evidencia |
|---|---|---|---|---|
| 1 | Nombres de colas, exchanges y routing keys centralizados | Implementada | `application.yaml` + `RabbitNamesProperties` | Commit `0d4e39e` |
| 2 | Beans de Queue, Exchange y Binding por caso de uso (email, warehouse, label) | Implementada | `RabbitTopologyConfig` | Commit `0d4e39e`; captura RabbitMQ: `docs/evidencias/01-colas.png`, `02-exchanges.png` |
| 3 | DLQ por cada flujo (cola principal con dead letter hacia `cmd.dead.dlx`) | Implementada | `RabbitTopologyConfig` | `docs/evidencias/01-colas.png` |
| 4 | Crear cola | Implementada | `RabbitAdminController` / `RabbitAdminService` | `POST /api/rabbit/queues` → 201: `docs/evidencias/03-crear-cola.png` |
| 5 | Consultar cola | Implementada | `RabbitAdminController` / `RabbitAdminService` | `GET /api/rabbit/queues/q.cmd.email` → 200: `docs/evidencias/04-consultar-cola.png` |
| 6 | Eliminar cola | Implementada | `RabbitAdminController` / `RabbitAdminService` | `DELETE /api/rabbit/queues/{name}` → 204: `docs/evidencias/05-eliminar-cola.png` |
| 7 | Crear exchange | Implementada | `RabbitAdminController` / `RabbitAdminService` | `POST /api/rabbit/exchanges` → 201: `docs/evidencias/06-crear-exchange.png` |
| 8 | Eliminar exchange | Implementada | `RabbitAdminController` / `RabbitAdminService` | `DELETE /api/rabbit/exchanges/{name}` → 204: `docs/evidencias/07-eliminar-exchange.png` |
| 9 | Crear binding | Implementada | `RabbitAdminController` / `RabbitAdminService` | `POST /api/rabbit/bindings` → 201: `docs/evidencias/08-crear-binding.png` |
| 10 | Eliminar binding | Implementada | `RabbitAdminController` / `RabbitAdminService` | `DELETE /api/rabbit/bindings` → 204: `docs/evidencias/09-eliminar-binding.png` |
| 11 | Validación de entrada (nombres vacíos o inválidos, tipos de exchange) | Implementada | DTOs (`QueueRequest`, `ExchangeRequest`, `BindingRequest`) | Respuesta 400: `docs/evidencias/10-validacion-400.png` |
| 12 | Manejo global de errores (400, 404, 409) | Implementada | `GlobalExceptionHandler` | 409 al duplicar cola: `docs/evidencias/11-conflicto-409.png` |
| 13 | Lógica de administración encapsulada en un servicio | Implementada | `RabbitAdminService` | Commit `0d4e39e` (el controlador no usa la API de RabbitMQ) |
| 14 | Pruebas unitarias del servicio (4 casos con Mockito) | Implementada | `RabbitAdminServiceTest` | `docs/evidencias/12-tests.png` |
| 15 | RabbitMQ con Management UI mediante Docker Compose | Implementada | `docker-compose.yml` | `docs/evidencias/13-docker.png` |
| 16 | Autorización con JWT de Azure AD y rol ADMIN | Parcial | `SecurityConfig` | Código implementado; falta probar con un token real de Azure AD |

