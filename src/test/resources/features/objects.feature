Feature: Modulo 2 - API publica de objetos

# Configuración común: URL base, contratos reutilizables y variable del recurso creado.
Background:
  * url baseUrl
  * def objectSchema = read('classpath:schemas/object.json')
  * def createdId = null

@CP01 @read
Scenario: Consultar la lista de objetos
  Given path endpoints.objects
  When method get
  Then assert responseStatus == testData.statuses.list
  And match response == '#[]'
  And assert response.length > 0
  And match each response == objectSchema
  And assert responseTime < sla
  And match responseHeaders['content-type'][0] contains 'application/json'

@CP02 @create
Scenario: Crear un objeto y consultar todos los campos enviados
  * def Builder = Java.type('automation.builders.ObjectPayloadBuilder')
  * def payload = karate.fromString(new Builder('create').uniqueName().build().toString())
  Given url baseUrl
  Given path endpoints.objects
  And request payload
  When method post
  Then assert responseStatus == testData.statuses.create
  And match response contains payload
  And match response contains read('classpath:schemas/created.json')
  * def createdId = response.id
  Given url baseUrl
  And path endpoints.object.replace('{id}', createdId)
  When method get
  Then assert responseStatus == testData.statuses.get
  And match response == objectSchema
  And match response contains payload
  And match response.id == createdId
  And assert responseTime < sla
  And match responseHeaders['content-type'][0] contains 'application/json'

@CP03 @update
Scenario: Reemplazar un objeto con PUT y verificar persistencia

  * def Builder = Java.type('automation.builders.ObjectPayloadBuilder')
  * def payload = karate.fromString(new Builder('create').uniqueName().build().toString())
  Given url baseUrl
  Given path endpoints.objects
  And request payload
  When method post
  Then assert responseStatus == testData.statuses.create
  And match response contains payload
  And match response contains read('classpath:schemas/created.json')
  * def createdId = response.id
  * def update = karate.fromString(new Builder('update').build().toString())
  * match update == read('classpath:schemas/request.json')
  Given url baseUrl
  And path endpoints.object.replace('{id}', createdId)
  And request update
  When method put
  Then assert responseStatus == testData.statuses.update
  And match response contains update
  And match response contains { id: '#(createdId)', updatedAt: '#number' }
  And assert responseTime < sla
  And match responseHeaders['content-type'][0] contains 'application/json'
  Given path endpoints.object.replace('{id}', createdId)
  When method get
  Then assert responseStatus == testData.statuses.get
  And match response == objectSchema
  And match response contains update
  And assert responseTime < sla
  And match responseHeaders['content-type'][0] contains 'application/json'

@CP04 @patch
Scenario: Editar el nombre con PATCH conservando los demas campos

  * def Builder = Java.type('automation.builders.ObjectPayloadBuilder')
  * def payload = karate.fromString(new Builder('create').uniqueName().build().toString())
  Given url baseUrl
  Given path endpoints.objects
  And request payload
  When method post
  Then assert responseStatus == testData.statuses.create
  And match response contains payload
  And match response contains read('classpath:schemas/created.json')
  * def createdId = response.id
  * def patch = karate.fromString(new Builder('patch').build().toString())
  * match patch == { name: '#string' }
  Given url baseUrl
  And path endpoints.object.replace('{id}', createdId)
  And request patch
  When method patch
  Then assert responseStatus == testData.statuses.patch
  And match response contains { id: '#(createdId)', name: '#(patch.name)', data: '#(payload.data)', updatedAt: '#number' }
  And assert responseTime < sla
  And match responseHeaders['content-type'][0] contains 'application/json'
  Given path endpoints.object.replace('{id}', createdId)
  When method get
  Then assert responseStatus == testData.statuses.get
  And match response == { id: '#(createdId)', name: '#(patch.name)', data: '#(payload.data)' }
  And assert responseTime < sla
  And match responseHeaders['content-type'][0] contains 'application/json'

@CP05 @delete
Scenario: Eliminar un objeto y comprobar que no existe

  * def Builder = Java.type('automation.builders.ObjectPayloadBuilder')
  * def payload = karate.fromString(new Builder('create').uniqueName().build().toString())
  Given url baseUrl
  Given path endpoints.objects
  And request payload
  When method post
  Then assert responseStatus == testData.statuses.create
  And match response contains payload
  And match response contains read('classpath:schemas/created.json')
  * def createdId = response.id
  Given url baseUrl
  And path endpoints.object.replace('{id}', createdId)
  When method delete
  Then assert responseStatus == testData.statuses.delete
  And match response == { message: '#string' }
  And match response.message contains createdId
  And assert responseTime < sla
  And match responseHeaders['content-type'][0] contains 'application/json'
  Given path endpoints.object.replace('{id}', createdId)
  When method get
  Then assert responseStatus == testData.statuses.missing
  And match response == read('classpath:schemas/error.json')
  And assert responseTime < sla
  And match responseHeaders['content-type'][0] contains 'application/json'
  * def createdId = null

@CP06 @negative @read
Scenario: Consultar un identificador inexistente

  Given path endpoints.object.replace('{id}', testData.missingId)
  When method get
  Then assert responseStatus == testData.statuses.missing
  And match response == read('classpath:schemas/error.json')
  And match response.error contains testData.missingId
  And assert responseTime < sla
  And match responseHeaders['content-type'][0] contains 'application/json'

@CP07 @negative
Scenario: Rechazar un JSON mal formado

  Given path endpoints.objects
  And header Content-Type = 'application/json'
  And request testData.malformed
  When method post
  Then assert responseStatus == testData.statuses.malformed
  And match response contains read('classpath:schemas/error.json')
  And assert responseTime < sla
  And match responseHeaders['content-type'][0] contains 'application/json'

