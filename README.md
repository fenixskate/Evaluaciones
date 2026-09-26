# Automatizacion web - SauceDemo

Guia sencilla para ejecutar las cinco pruebas del modulo 1.

## Requisitos

- Git Bash.
- Java JDK 21 y Maven 3.9 o superior para ejecucion local.
- Docker Desktop para ejecucion en contenedor.
- Internet durante la primera ejecucion.

## Descargar

```bash
git clone URL_DEL_REPOSITORIO
cd PruebaTecnica
```

## Ejecutar en Git Bash

```bash
mvn clean test -Dbrowser=firefox -Dheadless=true
```

Casos individuales:

```bash
mvn test -Dcucumber.filter.tags=@CP01
mvn test -Dcucumber.filter.tags=@CP05
```

Paralelo:

```bash
mvn test -Pparallel -Dbrowser=firefox -Dheadless=true
```

El perfil paralelo ejecuta dos grupos: login/carrito y checkout.

## Resultados

- HTML y JSON: `target/cucumber-report.html` y `target/cucumber-report.json`.
- PDFs: `output/pdf/ddmmaa_hhmmss/`.
- Cada escenario tiene un archivo `CP001_...pdf` hasta `CP005_...pdf`.

## Enviar evidencias por correo

En `src/test/resources/config/qa.properties` activa:

```properties
report.email.enabled=true
report.email.to=kikec44@gmail.com
```

En Git Bash configura una contraseña de aplicación de Gmail:

```bash
export SMTP_HOST="smtp.gmail.com"
export SMTP_PORT="587"
export SMTP_USER="tu-correo@gmail.com"
export SMTP_PASSWORD="tu-contrasena-de-aplicacion"
```

Envía la carpeta PDF más reciente comprimida como ZIP:

```bash
mvn test-compile exec:java "-Dexec.mainClass=notifications.ReportNotifier"
```

Con la bandera en `false` solo se registra una notificación simulada. Nunca guardes la contraseña en Git.

## Ejecutar con Docker

```bash
docker compose build
docker compose up --abort-on-container-exit
```

Docker usa Firefox headless, Maven y Java 21. Las evidencias se guardan en tu carpeta local `output`; no se publica ningún puerto.

Si Maven muestra errores de permisos:

```bash
docker compose down
docker compose build --no-cache
docker compose up --abort-on-container-exit
```

## Arquitectura

- `pages`: operaciones de cada pantalla.
- `selectors`: todos los selectores separados por pantalla.
- `actions`: flujos de negocio.
- `driver`: ciclo de vida del navegador.
- `utils`: esperas y capturas.
- `steps` y `features`: escenarios Cucumber.
- `reports`: PDF por escenario.
- `Dockerfile` y `docker-compose.yml`: ejecución reproducible.

El alcance implementado es el módulo 1 web: login, carrito, checkout, evidencias PDF, ejecución paralela, notificación por correo y Docker.
