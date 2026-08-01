# 🍽️ Food Court

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Relational_DB-4169E1?logo=postgresql&logoColor=white)
![Coverage](https://img.shields.io/badge/Coverage-%E2%89%A580%25-brightgreen)

Microservicio central de **Food Court**. Administra restaurantes y menús, valida la propiedad de los recursos y controla el ciclo completo de los pedidos desde su creación hasta la entrega o cancelación. Coordina la información de usuarios, los eventos de trazabilidad y las notificaciones al cliente.

## ✨ Funcionalidades

- Creación y consulta paginada de restaurantes.
- Creación, modificación y activación de platos.
- Menús paginados y filtrados por categoría.
- Pedidos compuestos por platos de un único restaurante.
- Asignación de empleados y transición controlada de estados.
- Generación y validación del PIN de entrega.
- Cancelación de pedidos pendientes.

## 🔄 Ciclo del pedido

```text
PENDING → IN_PREPARATION → READY → DELIVERED
    └────────────────────────────→ CANCELED
```

## 🧩 Arquitectura e integraciones

```text
Users :8081 ───────┐
Traceability :8083 ├──→ Food Court :8082 ───→ PostgreSQL
Messaging :8084 ───┘
```

Arquitectura hexagonal con puertos para persistencia, autenticación y comunicación HTTP entre microservicios.

## 🚀 Ejecución local

Requiere Java 21, PostgreSQL y la base de datos `foodcourt`.

```bash
./gradlew bootRun
```

| Recurso | URL |
|---|---|
| API | `http://localhost:8082/api/v1` |
| Swagger | `http://localhost:8082/api/v1/swagger-ui.html` |

## ⚙️ Configuración

```text
DB_URL                       jdbc:postgresql://localhost:5432/foodcourt
DB_USERNAME                  Usuario de PostgreSQL
DB_PASSWORD                  Contraseña de PostgreSQL
JWT_SECRET                   Clave compartida para validar JWT
USERS_SERVICE_URL            http://localhost:8081/api/v1
TRACEABILITY_SERVICE_URL     http://localhost:8083/api/v1
MESSAGING_SERVICE_URL        http://localhost:8084/api/v1
```

## 🔌 API principal

| Recurso | Operaciones |
|---|---|
| Restaurantes | `GET /restaurants` · `POST /restaurants` |
| Platos | `GET` · `POST` · `PATCH /restaurants/{restaurantId}/dishes` |
| Pedidos | `GET /orders` · `POST /orders` |
| Preparación | `PATCH /orders/{orderId}/assignment` · `/ready` |
| Cierre | `PATCH /orders/{orderId}/delivery` · `/cancellation` |

## 🧪 Calidad

```bash
./gradlew clean test jacocoTestCoverageVerification
```

Pruebas unitarias con JUnit y Mockito, reporte JaCoCo y cobertura mínima del **80 %**.
