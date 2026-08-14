# Coworking Reservas API

API REST en **Spring Boot 3 / Java 17** para gestión de espacios y reservas de coworking.

## Requisitos

- Java 17+
- Maven 3.9+
- Docker Desktop (Postgres y, opcionalmente, la app)

## Cómo ejecutar

### Opción recomendada (Docker Compose completo)

```bash
docker compose up -d --build
```

- API: http://localhost:8080  
- Swagger UI: http://localhost:8080/swagger-ui.html  
- Health: http://localhost:8080/actuator/health  

### Opción local (app en Maven + Postgres en Docker)

```bash
docker compose up -d postgres
mvn spring-boot:run
```

Perfil activo por defecto: `dev`.

### Credenciales de prueba (perfil `dev`)

| Usuario | Password | Rol |
|---------|----------|-----|
| `admin` | `admin123` | ADMIN |

Registro público crea usuarios con rol `USER` en `POST /api/auth/register`.

## Colección Postman

Importa en Postman el archivo de la raíz:

`Coworking-Reservas.postman_collection.json`

Orden sugerido:

1. **Login Admin** (guarda `adminToken`)
2. **Crear Espacio**
3. **Register User** / **Login User** (guarda `userToken`)
4. **Crear Reserva** → **Confirmar Reserva**
5. **Ocupación por rango**
6. Endpoints de Actuator / Circuit Breakers

## Endpoints principales

| Área | Rutas |
|------|-------|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Espacios | `GET/POST/PUT/DELETE /api/espacios` |
| Reservas | `POST/GET /api/reservas`, `POST /api/reservas/{id}/confirm\|cancel\|complete` |
| Reportes | `GET /api/reportes/ocupacion?desde=&hasta=` |
| Mock pago | `POST /internal/mock/payment/validate` |
| Docs | `/swagger-ui.html`, `/v3/api-docs` |
| Actuator | `/actuator/health`, `/info`, `/metrics`, `/circuitbreakers` |

## Arquitectura

Capas: `controller` → `service` → `repository`, con `dto` + `mapper`.  
Las entidades JPA **no** se exponen directamente en la API.

Perfiles:

- `application-dev.yml` — Postgres local, DDL `update`, SQL visible
- `application-prod.yml` — variables de entorno (`DB_URL`, `JWT_SECRET`, etc.)
- Config tipada con `@ConfigurationProperties` (`DatabaseProperties`, `JwtProperties`, `PaymentProperties`)

## Decisiones de diseño

### Patrón State (GoF)

Se aplicó **State** al ciclo de vida de la reserva (`PENDING`, `PENDING_PAYMENT`, `CONFIRMED`, `CANCELLED`, `COMPLETED`).

Cada estado encapsula transiciones válidas (`confirm`, `cancel`, `complete`, `markPendingPayment`).  
Frente a un `switch/if-else` centralizado:

- evita que reglas de transición se dispersen en el servicio
- hace explícito qué operaciones son ilegales (`InvalidReservationStateException`)
- facilita extender estados sin romper el flujo de negocio

### Reservas y concurrencia

- Validación de solapes con `@Query`
- Lock pesimista del espacio al crear (`findByIdForUpdate`)
- `@Transactional` en create/confirm/cancel/complete
- `OverlappingReservationException` → HTTP 409
- `JOIN FETCH` en consultas de listado/detalle para evitar N+1

### Circuit Breaker (Resilience4j)

La confirmación invoca un gateway de pago simulado (potencialmente inestable) envuelto con `spring-cloud-starter-circuitbreaker-resilience4j`.

- Umbrales en `application.yml` (`paymentService`)
- Fallback: deja la reserva en `PENDING_PAYMENT` sin fallar la petición HTTP
- Estado visible en `/actuator/health` y `/actuator/circuitbreakers`
- Para forzar fallos en `dev`: `app.payment.force-failure=true`

### Notificación asíncrona

Al confirmar, se publica `ReservationConfirmedEvent`.  
`NotificationService` lo consume con `@TransactionalEventListener(AFTER_COMMIT)` + `@Async` y simula el correo por log (sin bloquear la respuesta).

### Caché de reportes

`GET /api/reportes/ocupacion` usa `@Cacheable` (Caffeine).  
Create/confirm/cancel/complete hacen `@CacheEvict` del cache `ocupacion`.

### Seguridad

- JWT stateless (`ADMIN` / `USER`)
- USER: CRUD de sus reservas (crear/consultar/cancelar/confirmar)
- ADMIN: espacios + gestión global de reservas + completar

## Tests

```bash
mvn test
```

Incluye:

- Unitarios (Mockito): `ReservaServiceTest`, `PaymentValidationClientTest`, `JwtServiceTest`, `ReservationLifecycleTest`
- Integración (`@SpringBootTest` + MockMvc): `AuthAndEspacioIntegrationTest`, `ReservaFlowIntegrationTest`

## Trade-offs / fuera de alcance

Aclaración respecto al PDF:

- Pago externo: pide “mock endpoint **o** WireMock”. Se usó mock/gateway simulado + Circuit Breaker; WireMock es alternativa, no obligatorio.
- Patrón GoF: pide **al menos uno** (ej. Strategy, State, Observer, Chain). Se implementó **State**; Strategy no es un faltante.
- Flyway/Liquibase: **no lo exige el PDF**; el DDL se maneja con Hibernate.

Por límite de tiempo (el propio PDF pide priorizar) se dejó fuera o simplificado:

- Soft-delete y auditoría avanzada
- Notificación SMTP real (se simula por log async)
- Migraciones versionadas (mejora opcional)
- Paginación en listados

Con más tiempo: Flyway, Testcontainers Postgres en CI, Strategy de tarifas y métricas de negocio.

## Checklist vs enunciado

| Requisito | Estado |
|-----------|--------|
| Spring Boot 3.x + Java 17 | OK |
| JPA + relaciones + `@Query` (anti N+1) | OK |
| Security JWT + roles | OK |
| Bean Validation + `@ControllerAdvice` | OK |
| Actuator health/info/metrics (+ circuitbreakers) | OK |
| Perfiles + `@ConfigurationProperties` | OK |
| Caché reporte | OK |
| `@Async` / eventos | OK |
| `@Transactional` reservas | OK |
| OpenAPI/Swagger | OK |
| Tests unitarios + ≥2 integración | OK |
| Dockerfile + docker-compose (Postgres) | OK |
| Circuit Breaker + fallback + Actuator | OK |
| Patrón de comportamiento (State) | OK |
| Capas + DTOs + excepciones de negocio | OK |
| README + Postman | OK |
