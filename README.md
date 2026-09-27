# Módulo 2 · Automatización de Web APIs

Framework de pruebas para [restful-api.dev](https://restful-api.dev/) usando Java 21, Maven y Karate 1.5.2.

## Entregables y cumplimiento

| Entregable | Estado | Evidencia |
|---|---|---|
| Código fuente | Listo localmente | `src/main`, `src/test`, `pom.xml` |
| README técnico | Listo | Este documento |
| Reporte de ejecución | Listo | `output/reports/ddMMyyyy_HHmmss/` |
| Justificación de herramientas | Listo | Sección técnica de este documento |
| Repositorio GitHub e historial | Pendiente de publicación | Requiere commit/push del propietario |

## Justificación técnica

Java 21 ofrece compatibilidad multiplataforma, tipado fuerte y soporte LTS. Maven centraliza dependencias, perfiles y ejecución reproducible en Windows, Linux, Docker y GitHub Actions. Karate se selecciona porque permite escribir pruebas API en Gherkin, validar JSON con `match`, registrar request/response automáticamente y ejecutar escenarios en paralelo sin una capa HTTP adicional. JUnit 5 integra el runner con el ciclo de vida estándar de Maven. El reporte HTML/JSON/JUnit de Karate facilita la trazabilidad en local y como artefacto de CI.

La arquitectura separa configuración, autenticación, builders, datos, endpoints, esquemas y casos. Las URLs, tiempos, credenciales y datos se cargan desde archivos externos o variables de entorno. Esta separación reduce duplicación y permite cambiar de ambiente sin modificar los escenarios.

## Estructura

```text
src/main/java
├── automation/auth       # Bearer, API Key y OAuth2 con renovación de token
├── automation/builders   # Construcción de payloads
├── automation/utils      # Lectura segura de JSON
└── config                # Configuración por entorno

src/test
├── java/runners           # Punto de entrada JUnit 5 + Karate
└── resources
    ├── features           # Casos de negocio y soporte reutilizable
    ├── data               # Datos de prueba
    ├── schemas             # Contratos JSON
    ├── endpoints           # Rutas centralizadas
    └── config              # Valores por entorno
```

## Ejecución

```powershell
mvn -s .mvn/settings.xml test
mvn -s .mvn/settings.xml test -Pparallel
mvn -s .mvn/settings.xml test -Dtags=@CP01
```

Cada ejecución crea una carpeta propia en `output/reports/ddMMyyyy_HHmmss/`. Allí quedan `karate-summary.html`, JSON, JUnit y los recursos HTTP de cada escenario.

PowerShell y Git Bash son compatibles. En Git Bash:

```bash
cd /c/Users/Cuevita/Documents/GIT/PruebaTecnicaRequest
mvn -s .mvn/settings.xml test
```

En PowerShell:

```powershell
Set-Location C:\Users\Cuevita\Documents\GIT\PruebaTecnicaRequest
mvn -s .mvn/settings.xml test
```

Para ejecutar con Docker:

```bash
docker compose build
docker compose run --rm --name pruebaTecnicaRequest api-tests
```

El contenedor se llama `pruebaTecnicaRequest` y el volumen conserva `output/` en el equipo host.

Los casos cubren listado, creación, consulta, PUT, PATCH, DELETE y respuestas de excepción. Todos están concentrados en `features/objects.feature`.

## Docker y CI

```bash
docker compose build
docker compose run --rm api-tests
```

GitHub Actions ejecuta `mvn test` en Java 21 y publica `output/` como artefacto aunque una prueba falle. Esto conserva el reporte de éxito o de error para diagnóstico.

Workflow: `.github/workflows/api-tests.yml`.

## Reporte de fallas

Una respuesta inesperada queda registrada en el reporte HTML con el request, headers, body, response, status y tiempo. Para conservar una evidencia de error se puede ejecutar `mvn test -Dtags=@CP06` contra un ambiente no disponible o modificar temporalmente el status esperado; la ejecución fallida permanece en `output/reports` y el workflow la publica como artefacto.

## Autenticación

`karate-config.js` centraliza headers y `AuthProvider` soporta `none`, `bearer`, `api-key` y `oauth2`. Las credenciales se proporcionan mediante variables como `AUTH_BEARER_TOKEN`, `AUTH_API_KEY`, `AUTH_OAUTH_CLIENT_ID` y nunca se guardan en el repositorio.

## Evaluación técnica

| Criterio | Cumplimiento |
|---|---|
| Justificación técnica | Java 21, Maven, Karate y JUnit 5 están justificados arriba. |
| Arquitectura | Separación entre configuración, autenticación, builders, datos, endpoints, schemas y feature. |
| Calidad de código | Nombres semánticos, JavaDoc y un único feature organizado por casos. |
| Gestión de datos | URL, status, payloads, schemas y credenciales están externalizados. |
| Robustez | Timeouts, validación de errores, SLA, headers y manejo de OAuth2 con expiración. |
| Evidencia | Karate genera HTML, JSON, JUnit y logs request/response por ejecución. |
| Puntos extra | Docker, GitHub Actions, ejecución paralela, correo simulado y Xray simulado incluidos. |

## Limitación del servicio público

`restful-api.dev` aplica un límite diario a su API pública. Cuando se alcanza, el framework conserva el reporte de la falla y GitHub Actions lo publica como artefacto. Para una ejecución estable se puede configurar una cuenta/API key mediante variables de entorno o utilizar un mock local.
