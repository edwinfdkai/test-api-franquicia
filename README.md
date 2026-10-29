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

La colección de Postman está en [`src/main/resources/doc/EPAM.postman_collection.json`](src/main/resources/doc/EPAM.postman_collection.json). Impórtala en Postman para encontrar solicitudes de ejemplo dirigidas a `https://test-api-franquicia.onrender.com`. Algunos requests usan IDs fijos; ajústalos si los recursos de tu despliegue tienen otros identificadores.

También está disponible Swagger UI en `/swagger-ui/index.html` y la especificación OpenAPI en `/v3/api-docs`. Sustituye el host por el de tu despliegue; en local, por ejemplo, usa `https://test-api-franquicia.onrender.com/swagger-ui/index.html#/`.

## Ejecución

**Requisitos:** Docker y Docker Compose para el arranque en contenedores, o Java 21 y una instancia de MySQL accesible para ejecución local. El archivo `compose.yml` define MySQL y la API; el `Dockerfile` construye la aplicación.

### Docker

Desde la carpeta del proyecto, arranca MySQL y la API con:

```powershell
docker compose up --build -d
```

Compose configura la conexión de la API a MySQL mediante `DB_USERNAME` y `DB_PASSWORD`; puede recibirlas del entorno o del archivo `.env` local. El script `database/init/001_schema.sql` inicializa el esquema al crear MySQL con un volumen vacío. No se vuelve a ejecutar automáticamente sobre un volumen ya inicializado. No elimines el volumen `mysql_data` si quieres conservar sus datos.

Comprueba el estado de los servicios y que la API responde:

```powershell
docker compose ps
curl.exe -i http://localhost:8080/api/v1/products/1
```

La consulta devuelve `200` si existe el producto o `404` si no existe. Detén los servicios sin borrar el volumen con:

```powershell
docker compose down
```

No guardes credenciales reales en el repositorio. La API expone el puerto `8080`.

### Ejecución local

Con **Java 21** y una instancia MySQL accesible, configura las mismas variables. Para MySQL local, la URL predeterminada es `jdbc:mysql://localhost:3306/franchise_db`.

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/franchise_db"
$env:DB_USERNAME = "franchise_user"
$env:DB_PASSWORD = Read-Host "DB_PASSWORD"

.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

La aplicación inicia en `http://localhost:8080`; detenla con `Ctrl+C`. El perfil predeterminado es `dev` y actualiza el esquema mediante Hibernate.

## Pruebas

Hay pruebas unitarias, de integración de la API y de persistencia/repositorios. Usan **JUnit 5**; **Mockito** aísla dependencias en pruebas unitarias y **Testcontainers** proporciona MySQL 8.4 para pruebas de integración y persistencia. Docker debe estar disponible para ejecutar estas últimas.

```powershell
.\mvnw.cmd test
```
