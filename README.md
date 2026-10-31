# API de gestión de franquicias

API REST para administrar franquicias, sus sucursales y los productos con su stock. Permite consultar, por franquicia, los productos con mayor inventario de cada sucursal.

## Arquitectura y operaciones

La API separa la entrada HTTP en **Controllers**, la lógica de negocio en **Services** y el acceso a datos en **Repositories** de Spring Data JPA. Las entidades `Franchise`, `Branch` y `Product` modelan las relaciones persistidas en MySQL; los DTO validan las solicitudes y definen las respuestas.

Se pueden crear y renombrar franquicias y sucursales; crear, consultar, renombrar y eliminar productos; y actualizar su stock. La consulta de mayor stock devuelve **todos los productos empatados** en cada sucursal, ordenados por sucursal y producto. Las sucursales sin productos no aparecen en el resultado.

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/v1/franchises` | Crear franquicia |
| `PATCH` | `/api/v1/franchises/{id}` | Renombrar franquicia |
| `POST` | `/api/v1/franchises/{franchiseId}/branches` | Crear sucursal |
| `PATCH` | `/api/v1/branches/{id}` | Renombrar sucursal |
| `POST` | `/api/v1/branches/{branchId}/products` | Crear producto en una sucursal |
| `DELETE` | `/api/v1/branches/{branchId}/products/{productId}` | Eliminar producto de una sucursal |
| `PATCH` | `/api/v1/products/{id}/stock` | Actualizar stock |
| `PATCH` | `/api/v1/products/{id}` | Renombrar producto |
| `GET` | `/api/v1/products/{id}` | Consultar producto por ID |
| `GET` | `/api/v1/franchises/{franchiseId}/products/highest-stock` | Consultar productos con mayor stock por sucursal |

## Probar la API

La colección de Postman está en [`src/main/resources/doc/EPAM.postman_collection.json`](src/main/resources/doc/EPAM.postman_collection.json). Impórtala en Postman para probar los endpoints; sus solicitudes de ejemplo apuntan a la demo online. Algunos requests usan IDs fijos, así que ajústalos si los recursos de tu despliegue tienen otros identificadores.

## Formas de ejecución

### A) Demo online

Abre [Swagger UI](https://test-api-franquicia.onrender.com/swagger-ui/index.html) para explorar y probar la API. La demo utiliza el plan gratuito de Render, por lo que, si estuvo inactiva, la primera respuesta puede tardar mientras el servicio vuelve a estar disponible.

La especificación OpenAPI está disponible en `/v3/api-docs`. También puedes importar en Postman la colección ubicada en [`src/main/resources/doc/EPAM.postman_collection.json`](src/main/resources/doc/EPAM.postman_collection.json).

### B) Ejecución local

**Requisitos:** Docker Desktop instalado y en ejecución, con Docker Compose disponible.

Clona el repositorio y levanta la API junto con MySQL:

```text
git clone https://github.com/edwinfdkai/test-api-franquicia.git
cd test-api-franquicia
docker compose up --build
```

Docker Compose levanta la API y una base MySQL independiente en tu equipo. No necesitas una cuenta de Aiven ni configurar credenciales externas para ejecutar el proyecto localmente. La conexión utiliza valores predeterminados exclusivos para desarrollo, que también pueden sobrescribirse mediante las variables de entorno `DB_USERNAME` y `DB_PASSWORD`.

Swagger queda disponible en [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html). La especificación OpenAPI está en `http://localhost:8080/v3/api-docs`.

Para detener los contenedores, ejecuta desde la carpeta del proyecto:

```powershell
docker compose down
```

La base de datos se conserva en el volumen `mysql_data` aunque detengas los contenedores. No uses `docker compose down -v` salvo que quieras eliminar también la base de datos local y sus datos. El script `database/init/001_schema.sql` inicializa el esquema al crear un volumen vacío; no se ejecuta de nuevo automáticamente sobre un volumen ya inicializado.

## Pruebas

Hay pruebas unitarias, de integración de la API y de persistencia/repositorios. Usan **JUnit 5**; **Mockito** aísla dependencias en pruebas unitarias y **Testcontainers** proporciona MySQL 8.4 para pruebas de integración y persistencia. Docker debe estar disponible para ejecutar estas últimas.

```powershell
.\mvnw.cmd test
```
