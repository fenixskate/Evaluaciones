# Estrategia de pruebas — búsqueda Liverpool

## 1. ¿Qué no automatizaría y por qué?

No automatizaría el juicio estético de fotografías, colores percibidos o campañas. La regresión visual opcional comprueba estructura con contenido comercial enmascarado y baselines revisadas, no aprueba cambios automáticamente. Tampoco fijaría inventario, precios o posiciones esperadas: son dinámicos. Las reglas comerciales profundas requieren servicios y datos controlados; la coincidencia UI/API no demuestra que el precio comercial sea correcto.

## 2. ¿Cómo manejaría un CAPTCHA?

Nunca intentaría resolverlo ni deshabilitarlo desde el test. Solicitaría al equipo un mecanismo exclusivo para ambientes de prueba: clave CAPTCHA de prueba, allowlist para los runners de CI o feature flag autorizado. Mantendría una prueba separada que confirme que el CAPTCHA aparece cuando corresponde, mientras el recorrido funcional usaría el bypass oficial. Si producción fuera el único ambiente disponible, marcaría el escenario como no apto para CI automatizado.

## 3. Riesgos de flakiness y mitigaciones

Los principales riesgos son latencia, renderizado asíncrono, recarga al filtrar, cambios de inventario/precio, protección anti-bot y cambios de DOM. Se mitigan con locators accesibles o semánticos centralizados, esperas por estado/URL en `BasePage`, ausencia de sleeps fijos, datos comparados dinámicamente con la misma respuesta `/api/plp/search`, tolerancia mínima de 3/5 coincidencias, Chrome estable con configuración regional consistente, timeout acotado y screenshot automático desde un hook global al fallar.

## 4. Integración con 50+ suites

Etiquetaría escenarios (`@smoke`, `@network`), ejecutaría este flujo como smoke en PR y la matriz completa en horario programado. Usaría ambiente y datos controlados, sharding por suite, límites de concurrencia para no saturar Liverpool, retries únicamente para fallos de infraestructura y conservación de trace/video solo al fallar. Publicaría reportes unificados, mediría duración y tasa de flakiness, asignaría ownership y bloquearía merges solo con pruebas estables y críticas.

La matriz opcional ejecuta tres motores en paralelo. Axe reporta WCAG y permite un gate estricto; no sustituye revisión manual de teclado/lector. El presupuesto configurable mide Enter→cinco tarjetas visibles, no Core Web Vitals. CI público puede ser bloqueado por anti-bot: no prometo disponibilidad de un tercero ni convertiría un bloqueo en un falso aprobado.
