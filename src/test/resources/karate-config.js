function fn() {
  var Config = Java.type('config.ConfigManager');
  var Mask = Java.type('hooks.SensitiveLogModifier');
  karate.configure('connectTimeout', Config.positiveInt('http.timeout.ms'));
  karate.configure('readTimeout', Config.positiveInt('http.timeout.ms'));
  karate.configure('followRedirects', false);
  karate.configure('lowerCaseResponseHeaders', true);
  karate.configure('logPrettyRequest', true);
  karate.configure('logPrettyResponse', true);
  karate.configure('logModifier', new Mask());
  return {
    baseUrl: Config.get('base.url'),
    sla: Config.positiveInt('sla.ms'),
    endpoints: karate.read('classpath:endpoints/objects.json'),
    testData: karate.read('classpath:data/objects.json')
  };
}
