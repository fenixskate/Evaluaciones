# language: es
@modulo1 @web
Característica: Flujos de compra en SauceDemo
  Validar el acceso, la navegación y el proceso de compra.
  Los datos de prueba se declaran en cada escenario.
  Las credenciales son públicas y pertenecen al sitio de demostración.

  @login @CP01
  Escenario: Iniciar sesión con un usuario válido
    Dado que el usuario está en la página de login
    Cuando inicia sesión con usuario "standard_user" y contraseña "secret_sauce"
    Entonces se muestra el catálogo "Products" en "inventory.html"

  @login @CP02
  Escenario: Rechazar el acceso de un usuario bloqueado
    Dado que el usuario está en la página de login
    Cuando inicia sesión con usuario "locked_out_user" y contraseña "secret_sauce"
    Entonces se muestra el mensaje de bloqueo "Epic sadface: Sorry, this user has been locked out."
    Y permanece en la página de login

  @carrito @CP03
  Escenario: Agregar un producto al carrito
    Dado que el usuario ha iniciado sesión con los siguientes datos
      | usuario    | standard_user  |
      | contraseña | secret_sauce   |
      | titulo     | Products       |
      | ruta       | inventory.html |
    Cuando agrega el producto "sauce-labs-backpack" desde el catálogo
    Y navega al carrito
    Entonces el carrito contiene el siguiente producto
      | titulo   | Your Cart           |
      | id       | sauce-labs-backpack |
      | nombre   | Sauce Labs Backpack |
      | precio   | $29.99              |
      | cantidad | 1                   |

  @carrito @CP04
  Escenario: Eliminar un producto del carrito
    Dado que el usuario ha iniciado sesión con los siguientes datos
      | usuario    | standard_user  |
      | contraseña | secret_sauce   |
      | titulo     | Products       |
      | ruta       | inventory.html |
    Y tiene el siguiente producto en el carrito
      | titulo   | Your Cart           |
      | id       | sauce-labs-backpack |
      | nombre   | Sauce Labs Backpack |
      | precio   | $29.99              |
      | cantidad | 1                   |
    Cuando elimina el producto "sauce-labs-backpack" desde el carrito
    Entonces el carrito queda vacío

  @checkout @CP05
  Escenario: Completar una compra
    Dado que el usuario ha iniciado sesión con los siguientes datos
      | usuario    | standard_user  |
      | contraseña | secret_sauce   |
      | titulo     | Products       |
      | ruta       | inventory.html |
    Y tiene el siguiente producto en el carrito
      | titulo   | Your Cart           |
      | id       | sauce-labs-backpack |
      | nombre   | Sauce Labs Backpack |
      | precio   | $29.99              |
      | cantidad | 1                   |
    Cuando inicia el checkout
    Y completa los siguientes datos del comprador
      | nombre       | Ana     |
      | apellido     | Pruebas |
      | codigoPostal | 01000   |
    Entonces el resumen muestra el siguiente producto e importes
      | titulo       | Checkout: Overview  |
      | nombre       | Sauce Labs Backpack |
      | precio       | $29.99              |
      | cantidad     | 1                   |
      | tasaImpuesto | 0.08                |
    Cuando confirma la compra
    Entonces se muestra la siguiente confirmación de compra
      | titulo      | Thank you for your order!                                                             |
      | descripcion | Your order has been dispatched, and will arrive just as fast as the pony can get there! |
      | ruta        | checkout-complete.html                                                                |
