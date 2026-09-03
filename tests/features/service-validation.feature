# language: es
Característica: Validación de resultados de Liverpool contra el servicio PLP

  Escenario: Comparar los cinco resultados visibles con la respuesta consumida por el frontend
    Dado que navego a la página de Liverpool
    Cuando intercepto la respuesta de resultados al buscar "playstation 5", filtrar "White" y ordenar por menor precio
    Y analizo la respuesta interceptada y extraigo los productos devueltos
    Y valido cruzadamente los resultados de la interfaz con los datos de la respuesta
    Entonces afirmo que al menos 3 de los 5 resultados aparecen en la respuesta interceptada
    Y registro las discrepancias de nombre o precio entre la interfaz y la respuesta
