# language: es
Característica: Búsqueda de productos en Liverpool

  Escenario: Consultar los cinco productos blancos más económicos para PlayStation 5
    Dado que navego a la página de Liverpool
    Cuando busco el producto "playstation 5"
    Y filtro los resultados por el color "White"
    Y ordeno los productos del precio más bajo al más alto
    Entonces obtengo e imprimo los primeros 5 productos con nombre y precio
