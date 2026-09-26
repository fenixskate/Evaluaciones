# language: es
@modulo1 @web
Característica: Flujos de compra en SauceDemo
  Validar el acceso, la navegación y el proceso de compra.
  Los datos de usuarios, productos y comprador provienen de archivos externos.

  @login @CP01
  Escenario: Iniciar sesión con un usuario válido
    Dado que el usuario está en la página de login
    Cuando inicia sesión con el usuario válido configurado
    Entonces se muestra el catálogo de productos

  @login @CP02
  Escenario: Rechazar el acceso de un usuario bloqueado
    Dado que el usuario está en la página de login
    Cuando inicia sesión con el usuario bloqueado configurado
    Entonces se muestra el mensaje de usuario bloqueado
    Y permanece en la página de login

  @carrito @CP03
  Escenario: Agregar un producto al carrito
    Dado que el usuario válido ha iniciado sesión
    Cuando agrega el producto configurado desde el catálogo
    Y navega al carrito
    Entonces el carrito contiene el producto seleccionado con su precio y cantidad

  @carrito @CP04
  Escenario: Eliminar un producto del carrito
    Dado que el usuario válido ha iniciado sesión
    Y tiene el producto configurado en el carrito
    Cuando elimina el producto desde el carrito
    Entonces el carrito queda vacío

  @checkout @CP05
  Escenario: Completar una compra
    Dado que el usuario válido ha iniciado sesión
    Y tiene el producto configurado en el carrito
    Cuando inicia el checkout
    Y completa los datos del comprador configurado
    Entonces el resumen muestra el producto y los importes correctos
    Cuando confirma la compra
    Entonces se muestra la confirmación de compra exitosa
