# Automatizacion web - SauceDemo

Guia sencilla para ejecutar las cinco pruebas del modulo 1.

## Requisitos

- Git Bash.
- Java JDK 21 y Maven 3.9 o superior para ejecucion local.
- Firefox instalado para ejecucion local (en Docker ya viene incluido).
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

El perfil paralelo abre hasta dos navegadores independientes al mismo tiempo.
Todos los casos usan un solo runner (`TestRunner`) y un solo feature.
Sin `-Pparallel`, los casos se ejecutan uno después del otro.

Para ejecutar solo login y checkout en paralelo:

```bash
mvn test -Pparallel "-Dcucumber.filter.tags=@login or @checkout"
```

Para usar tres navegadores (consume más memoria):

```bash
mvn test -Pparallel -Dparallel.threads=3
```

## Ejecutar todo con un comando

Desde la carpeta del proyecto, en Git Bash:

```bash
sh scripts/run-and-notify.sh -Pparallel
```

Ejecuta las pruebas, guarda los reportes, crea el correo con el ZIP adjunto y
simula la actualización de Jira/Xray. No envía correo ni modifica un Jira real.
También acepta tags:

```bash
sh scripts/run-and-notify.sh -Pparallel "-Dcucumber.filter.tags=@CP01 or @CP05"
```

Aunque falle un caso, intenta guardar y adjuntar sus evidencias. Al terminar,
el comando conserva el resultado fallido para que GitHub Actions lo marque en rojo.
No ejecutes dos comandos Maven simultáneos en la misma carpeta del proyecto:
comparten `target`. Usa `-Pparallel` para el paralelismo de escenarios.

## Resultados

Los usuarios, contraseñas de demostración, producto, comprador y valores esperados
se editan en `src/test/resources/features/saucedemo.feature`. Los pasos reciben
esos datos como parámetros o tablas; cada escenario declara sus propios datos.
`qa.properties` conserva solo la URL, navegador, modo headless, tiempos de espera
y configuración de correo y Xray.

- HTML y JSON: `target/cucumber-report.html` y `target/cucumber-report.json`.
- PDFs: `output/pdf/ddmmaa_hhmmss/`.
- Cada escenario tiene un archivo `CP001_...pdf` hasta `CP005_...pdf`.
- Todos los PDF de una ejecución, incluso en paralelo, quedan en una sola carpeta.
- El comando completo copia HTML, JSON, XML y tiempos a `output/reports/fecha_hora/`.
- `execution-timeline.csv` muestra inicio, fin y el hilo de cada escenario.
- Los resultados esperados de cada paso del PDF vienen de `src/test/resources/config/expected-results.properties`.

## Simular un correo con evidencias adjuntas

En `src/test/resources/config/qa.properties` puedes cambiar el destinatario:

```properties
report.email.to=kikec44@gmail.com
```

Después de ejecutar las pruebas, genera el correo local desde Git Bash:

```bash
mvn test-compile exec:java "-Dexec.mainClass=integrations.email.ReportNotifier"
```

El programa selecciona la carpeta con fecha más reciente en `output/pdf`,
comprime sus PDFs y genera un correo `.eml` en `output/email` con el ZIP
incorporado como adjunto. Abre el EML con un programa compatible, como Outlook
o Thunderbird, para ver el mensaje y guardar el adjunto.

Es una simulación: no llega nada a Gmail, no se conecta a SMTP y no necesitas
contraseña ni variables SMTP. La antigua bandera `report.email.enabled`
ya no se utiliza; incluso si estaba activada, este comando no envía correo.

Para elegir exactamente qué ejecución adjuntar:

```bash
mvn test-compile exec:java "-Dexec.mainClass=integrations.email.ReportNotifier" "-Dexec.args=output/pdf/250926_220219"
```

Cambia `250926_220219` por la carpeta que quieras compartir. Solo se adjuntan
los PDFs de esa carpeta. El comando completo usa exactamente la ejecución que
acaba de terminar, evitando adjuntar resultados antiguos.

## Ejecutar con Docker

```bash
docker compose build
docker compose run --rm qa-automation
```

Docker ejecuta el flujo completo en paralelo con Firefox sin ventana. Las evidencias,
el correo simulado y la respuesta de Xray se guardan en tu carpeta local `output`.
No necesitas abrir una dirección en el navegador ni instalar Java en tu computadora.

Para elegir casos dentro de Docker:

```bash
docker compose run --rm qa-automation sh scripts/run-and-notify.sh -Pparallel "-Dcucumber.filter.tags=@CP05"
```

Si Maven muestra errores de permisos:

```bash
docker compose down
docker compose build --no-cache
docker compose run --rm qa-automation
```

## Ejecutar desde GitHub Actions

1. Sube este proyecto a tu repositorio de GitHub, incluyendo `.github/workflows/qa.yml`.
2. Abre la pestaña **Actions** y selecciona **Pruebas SauceDemo**.
3. Pulsa **Run workflow**. Deja `not @pendiente` para los cinco casos o escribe `@CP05` para checkout.
4. Espera el resultado y abre la ejecución. En **Artifacts**, descarga `evidencias-...`.
5. Descomprime el archivo: contiene PDF, HTML, correo EML con ZIP adjunto y la simulación de Xray.

También se ejecuta automáticamente cuando subes cambios o abres un pull request.
Para ver el botón manual, el workflow debe estar en la rama predeterminada.
No requiere contraseñas de correo ni tokens de Jira. Los archivos se conservan 14 días.
El flujo está preparado en el proyecto; su primera ejecución en GitHub requiere subirlo al repositorio.

## Simulación de Jira y Xray

El comando completo realiza una petición HTTP POST a un servidor temporal en tu
propia computadora, guarda los estados de los casos y luego cierra ese servidor.
Los estados provienen del resultado real: `PASS`, `FAIL` o `TODO` cuando no se ejecutó.
No necesitas licencia ni una cuenta de Jira.

En `output/integrations/fecha_hora/` encontrarás:

- `request.json`: los casos y estados enviados.
- `received.json`: lo recibido por el servidor simulado.
- `response.json` y `http.txt`: respuesta y código HTTP.
- `jira-state-simulated.json`: estado de los casos después de la actualización simulada.

Las claves ficticias `DEMO-1` a `DEMO-5` se configuran en `qa.properties`.
Para repetir únicamente esta simulación después de una prueba:

```bash
mvn test-compile exec:java "-Dexec.mainClass=integrations.xray.XraySimulator"
```

La simulación demuestra el intercambio por API con el formato de Xray Server/DC;
no valida autenticación, permisos o compatibilidad de una instalación real.
Una integración real requeriría su URL, credenciales seguras y claves existentes.

## Arquitectura

```text
src/main/java/
├── automation/
│   ├── pages/
│   ├── selectors/
│   ├── actions/
│   ├── driver/
│   └── utils/
├── integrations/
│   ├── email/ReportNotifier.java
│   └── xray/XraySimulator.java
└── config/ConfigManager.java
```

- `automation.pages`: operaciones de cada pantalla.
- `automation.selectors`: todos los selectores separados por pantalla.
- `automation.actions`: flujos de negocio.
- `automation.driver`: ciclo de vida del navegador.
- `automation.utils`: esperas y capturas.
- `integrations.email` y `integrations.xray`: correo y API simulados.
- `config`: configuración compartida.
- `steps` y `features`: escenarios Cucumber.
- `reports`: PDF por escenario.
- `Dockerfile` y `docker-compose.yml`: ejecución reproducible.

El alcance es el módulo 1 web. Java y Maven mantienen tipos y dependencias explícitas;
Cucumber permite leer los casos en español; JUnit Platform permite paralelizar
escenarios de un mismo feature; POM separa pantallas, selectores y flujos.
OpenPDF genera evidencias portables por paso, junto al HTML de Cucumber.
Docker comparte el entorno de ejecución y GitHub Actions automatiza el mismo comando.
El correo y Jira/Xray son simulados y están identificados como tales.

Referencias: [paralelismo de Cucumber](https://github.com/cucumber/cucumber-jvm/tree/v7.34.8/cucumber-junit-platform-engine),
[artefactos de GitHub Actions](https://docs.github.com/en/actions/concepts/workflows-and-actions/workflow-artifacts),
[formato de Xray Server/DC](https://getxraydocs.atlassian.net/wiki/spaces/XRAY/pages/1813184538/Using+Xray+JSON+Format+to+Import+Execution+Results).
Surefire se fija en 3.5.2 para evitar el problema de conteo con Cucumber documentado en
[Surefire #834](https://github.com/apache/maven-surefire/issues/834).
