# BochoCredit Backend — Java Spring Boot + PostgreSQL

Backend REST que reemplaza al backend Flask original (`app_v2.py`), manteniendo
exactamente la misma lógica de negocio: cálculo de planes de pago vehicular
(método francés vencido ordinario, alemán, americano, peruano y "Compra
Inteligente"), indicadores VAN/TIR/TCEA, gestión de clientes, vehículos y
bloqueo de vehículos con plan activo.

## Stack

- **Java 21** + **Spring Boot 3.3.4**
- **Spring Data JPA** (Hibernate 6) + **PostgreSQL**
- **Spring Security** + **JWT** (login con usuario/clave, como el original)
- **Flyway** para migraciones versionadas del esquema
- **Lombok** para reducir boilerplate
- **JUnit 5** + **H2** para pruebas (no requieren Postgres)

## Estructura del proyecto

```
src/main/java/com/bochocredit/
├── BochoCreditBackendApplication.java
├── config/SecurityConfig.java          # CORS, JWT filter chain, BCrypt
├── controller/                         # Endpoints REST
│   ├── AuthController.java
│   ├── ClienteController.java
│   ├── VehiculoController.java
│   └── SimulacionController.java
├── dto/                                # Records de entrada/salida (request/response)
├── entity/                              # Entidades JPA (mapeo 1:1 con el esquema SQL)
│   └── converter/CronogramaJsonConverter.java
├── exception/                           # Excepciones de negocio + @RestControllerAdvice
├── repository/                          # Spring Data JPA repositories
├── security/                            # JWT service, filtro, UserDetailsService
└── service/
    ├── finance/FinancialEngine.java     # ★ Motor de cálculo (núcleo del negocio)
    ├── AuthService.java
    ├── ClienteService.java
    ├── VehiculoService.java
    ├── VehiculoBloqueoService.java      # Lógica de bloqueo de vehículo con plan activo
    ├── SimulacionService.java           # Orquesta el motor + persistencia
    └── DataSeederService.java           # Crea usuario admin/admin123 al arrancar

src/main/resources/
├── application.yml
└── db/migration/                        # Scripts Flyway (V1, V2...)
```

## Requisitos previos

- JDK 21
- Maven 3.8+
- PostgreSQL 14+ corriendo localmente (o vía Docker)

## Puesta en marcha

### 1. Levantar PostgreSQL

```bash
docker run --name bochocredit-db -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=bochocredit -p 5432:5432 -d postgres:16
```

O usa una instancia ya existente — solo ajusta las variables de entorno.

### 2. Configurar variables de entorno

Copia `.env.example` y ajusta según tu entorno (host, usuario, clave del JWT, etc.):

```bash
cp .env.example .env
# edítalo, luego:
export $(grep -v '^#' .env | xargs)
```

### 3. Compilar y correr

```bash
mvn clean install
mvn spring-boot:run
```

Flyway crea el esquema automáticamente al arrancar (tablas `clientes`,
`vehiculos`, `simulaciones`, `pagos`, etc.) y `DataSeederService` crea el
usuario administrador:

```
usuario: admin
clave:   admin123
```

La API queda disponible en `http://localhost:8080`.

### 4. Ejecutar pruebas

```bash
mvn test
```

Las pruebas usan el perfil `test` (H2 en memoria), por lo que **no** requieren
una instancia de PostgreSQL corriendo.

## Autenticación

Todas las rutas bajo `/api/**` excepto `/api/auth/**` requieren un JWT válido
en el header `Authorization: Bearer <token>`.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Respuesta:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "username": "admin",
  "nombreCompleto": "Administrador",
  "rol": "ADMIN"
}
```

Usa ese token en las siguientes peticiones:
```bash
curl http://localhost:8080/api/clientes \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

## Endpoints principales

| Método | Ruta                                  | Descripción                                          |
|--------|----------------------------------------|-------------------------------------------------------|
| POST   | `/api/auth/login`                      | Login, devuelve JWT                                   |
| GET    | `/api/clientes`                        | Listar clientes                                       |
| POST   | `/api/clientes`                        | Crear cliente                                          |
| PUT    | `/api/clientes/{id}`                   | Editar cliente                                         |
| DELETE | `/api/clientes/{id}`                   | Eliminar cliente                                       |
| GET    | `/api/vehiculos`                       | Listar vehículos (incluye estado de bloqueo)           |
| POST   | `/api/vehiculos`                       | Registrar vehículo                                     |
| PUT    | `/api/vehiculos/{id}`                  | Editar vehículo                                        |
| POST   | `/api/calcular`                        | Vista previa de cálculo (sin persistir)                |
| POST   | `/api/creditos`                        | Crear simulación de crédito (persiste cronograma)       |
| GET    | `/api/creditos`                        | Listar todas las simulaciones                          |
| GET    | `/api/creditos/{id}`                   | Detalle de una simulación + cronograma completo        |
| PUT    | `/api/creditos/{id}`                   | Editar/recalcular una simulación                        |
| PATCH  | `/api/creditos/{id}/elegir`            | Marcar un plan como elegido (bloquea el vehículo)        |
| GET    | `/api/clientes/{id}/creditos`          | Simulaciones de un cliente específico                    |

### Ejemplo: crear una simulación (método francés)

```json
POST /api/creditos
{
  "clienteId": 1,
  "vehiculoId": 1,
  "moneda": "soles",
  "precioVehiculo": 70000,
  "cuotaInicialPct": 20,
  "plazoMeses": 48,
  "tipoTasa": "efectiva_anual",
  "tasaValor": 15,
  "graciaTipo": "ninguno",
  "graciaMeses": 0,
  "tsd": 0.05,
  "tsv": 0.15,
  "portes": 5,
  "metodoPago": "regular"
}
```

### Ejemplo: simulación "Compra Inteligente"

```json
POST /api/creditos
{
  "clienteId": 1,
  "vehiculoId": 1,
  "moneda": "soles",
  "precioVehiculo": 70000,
  "cuotaInicialPct": 20,
  "plazoMeses": 36,
  "tipoTasa": "efectiva_anual",
  "tasaValor": 15,
  "graciaTipo": "ninguno",
  "graciaMeses": 0,
  "tsd": 0.05,
  "tsv": 0.15,
  "portes": 5,
  "metodoPago": "compra_inteligente",
  "pctCuotaFinal": 40,
  "cok": 50
}
```

## Notas de migración desde el backend Flask

- El modelo `nombre_completo` con separador `;` del Python se normalizó a
  columnas `nombres` / `apellidos` separadas — más correcto en un modelo
  relacional. El getter `getNombreCompleto()` reconstruye el string combinado
  cuando se necesita.
- El cronograma se persiste como **JSONB** en PostgreSQL (antes era un
  `TEXT` con `json.dumps()` en SQLite). Se usa un `AttributeConverter`
  (`CronogramaJsonConverter`) + `@JdbcTypeCode(SqlTypes.JSON)`.
- Las contraseñas se hashean con **BCrypt** (igual que `werkzeug.security`
  en el original) en vez de quedar planas.
- La sesión basada en cookies de Flask se reemplazó por **JWT stateless**,
  más apropiado para un backend que servirá un frontend desacoplado.
- Todas las funciones de cálculo (`calcular_tem`, `calcular_tir`,
  `generar_cronograma_*`) se portaron método por método a
  `FinancialEngine.java`, validadas numéricamente contra el comportamiento
  original (ver `FinancialEngineTest.java`).

## Limitaciones conocidas / próximos pasos sugeridos

- No se implementó aún un endpoint de registro de nuevos usuarios (el
  Flask original tampoco lo expone; solo siembra el admin). Se puede agregar
  un `POST /api/usuarios` protegido por rol `ADMIN` si se requiere.
- El endpoint de "marcar plan como pagado" (actualizar `pagos.esta_pagado`)
  no estaba en el alcance original revisado; se puede agregar fácilmente
  sobre `PagoRepository` si se necesita.
