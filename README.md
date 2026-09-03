# Liverpool E2E con Playwright y Cucumber

Automatización E2E en JavaScript con Playwright, Cucumber/Gherkin y Page Object Model.

## Requisitos

- Node.js 20 o superior
- Google Chrome
- npm
- Git for Windows (incluye Git Bash)

En Visual Studio Code abre una terminal nueva: el proyecto configura **Git Bash**
como perfil predeterminado en Windows. Si tenías una terminal abierta, ciérrala
y crea otra. Todos los ejemplos siguientes se ejecutan desde Git Bash.

## Instalación

```bash
npm install
```

## Ejecución

Headless (predeterminado):

```bash
npm test
```

Con navegador visible:

```bash
npm run test:headed
```

Con grabación de video:

```bash
npm run test:video
```

## Flujo automatizado

1. Abre Liverpool.
2. Busca `playstation 5`.
3. Filtra el color `White`, mostrado como `Blanco` en el sitio.
4. Ordena por `Menor precio`.
5. Extrae el nombre y el precio de los primeros cinco productos.
6. Imprime los datos mediante `console.table`.

El reporte HTML queda en `reports/cucumber-report.html`. Si falla un escenario,
se adjunta una captura de pantalla al reporte.

## Arquitectura

```text
src/
  pages/                           # Locators y acciones; nunca contiene asserts
    BasePage.js                    # Navegador y esperas comunes
    LiverpoolSelectors.js         # Clase centralizada de selectores
    HomePage.js
    SearchResultsPage.js
  services/
    PlpResponseCollector.js        # Intercepta y transforma /api/plp/search
tests/                             # Flujo y aserciones; nunca contiene selectores web
  features/
    liverpool-search.feature
  step-definitions/
    liverpool.steps.js
  support/
    hooks.js
    world.js
cucumber.js
```

La validación de servicio captura la respuesta consumida por la interfaz en
`/api/plp/search`, cruza nombre y precio con los cinco resultados visibles,
registra las discrepancias y exige al menos tres coincidencias por nombre.

La Parte 1 y la Parte 2 están separadas en dos archivos feature. El workflow
`.github/workflows/test.yml` ejecuta ambas en headless y publica el reporte HTML.

## Bonus: calidad y múltiples navegadores

```bash
npx playwright install --with-deps chrome firefox webkit
npm run test:quality
npm run test:quality:headed
npm run report:quality
```

### Navegadores visibles en paralelo (Git Bash)

```bash
npx playwright install firefox webkit
npm run test:parallel:headed
```

Ejecuta la suite de calidad con ventanas visibles y hasta tres workers en paralelo,
usando los proyectos Chrome, Firefox y WebKit. Requiere Google Chrome instalado.
Las ventanas se abren y cierran automáticamente; el sistema operativo puede
superponerlas, no se distribuyen en mosaico. La planificación de workers no
garantiza un navegador de cada tipo abierto exactamente al mismo tiempo.

Para limitar la demostración a una búsqueda:

```bash
SEARCH_TERMS='["playstation 5"]' npm run test:parallel:headed
```

Para ejecutar únicamente Chrome en paralelo:

```bash
npm run test:parallel:headed -- --project=chrome
```

La suite adicional usa el runner de Playwright, los mismos Page Objects y tres
proyectos paralelos (Chrome, Firefox, WebKit). Incluye búsquedas parametrizadas,
presupuesto de carga y escaneo axe-core WCAG A/AA. Los resultados completos de
axe se adjuntan al HTML; `AXE_STRICT=true` convierte las violaciones en fallos.
El tiempo se mide desde Enter hasta cinco tarjetas visibles, no como una métrica
de laboratorio ni como tiempo de carga de todos los recursos publicitarios.

Configuración sin editar código (Git Bash):

```bash
export SEARCH_TERMS='["xbox series x","nintendo switch"]'
export MAX_SEARCH_SECONDS=25
npm run test:quality
```

Para aplicar variables únicamente a una ejecución:

```bash
SEARCH_TERMS='["playstation 5"]' MAX_SEARCH_SECONDS=25 npm run test:quality -- --project=chrome
```

Para quitar las variables exportadas:

```bash
unset SEARCH_TERMS MAX_SEARCH_SECONDS
```

`SEARCH_TERMS` es JSON; `MAX_SEARCH_SECONDS` vale 20 por defecto. `BASE_URL`
permite apuntar a un ambiente autorizado. Los cinco bonus están separados del
smoke obligatorio para no confundir fallos de accesibilidad del sitio público
con regresiones del framework. La matriz opcional está en `quality.yml`.

### Baselines visuales

```bash
npm run test:visual:update -- --project=chrome
npm run test:visual -- --project=chrome
```

La primera orden genera candidatos de referencia; revisarlos visualmente y
versionarlos antes de aceptar una baseline. Nunca ejecutar `--update-snapshots`
en el job de validación. Se compara la región principal de resultados y se
enmascaran fotos, nombres y precios dinámicos; filtros/conteos aún pueden variar.
Las referencias son específicas de SO/navegador. Para CI generar y aprobar en
Linux con datos controlados. Sin baseline aprobada el test visual falla, no se
declara aprobado automáticamente.

## Publicación

Pendiente: URL del repositorio público y enlace a una ejecución verde real.
No se ha publicado ni enviado correo desde este entorno. Tras publicar, ejecutar
el workflow `test.yml`, verificar el artifact HTML y agregar aquí su enlace.
