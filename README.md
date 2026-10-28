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

## Ejecución

**Requisitos:** Docker y una instancia de MySQL accesible, con la base de datos `franchise_db` creada. El proyecto no incluye `docker-compose`; el `Dockerfile` construye la aplicación, no MySQL.

### Docker

En PowerShell, configura la conexión a MySQL. Para MySQL instalado en el equipo, Docker Desktop puede acceder mediante `host.docker.internal`; ajusta la URL si la base de datos está en otro host.

```powershell
$env:DB_URL = "jdbc:mysql://host.docker.internal:3306/franchise_db"
$env:DB_USERNAME = "franchise_user"
$env:DB_PASSWORD = Read-Host "DB_PASSWORD"

docker build -t test-api .
docker run --rm -d --name test-api -p 8080:8080 `
  -e "DB_URL=$env:DB_URL" `
  -e "DB_USERNAME=$env:DB_USERNAME" `
  -e "DB_PASSWORD=$env:DB_PASSWORD" `
  test-api
```

Comprueba que el contenedor está activo y que la API responde:

```powershell
docker ps --filter "name=^/test-api$"
curl.exe -i http://localhost:8080/api/v1/products/1
```

La consulta devuelve `200` si existe el producto o `404` si no existe. Detén y elimina el contenedor con:

```powershell
docker stop test-api
```

Las credenciales se proporcionan mediante `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`; no las guardes en el repositorio. La imagen expone el puerto `8080`.

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
