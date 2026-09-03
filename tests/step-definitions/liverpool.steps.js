const assert = require('node:assert/strict');
const { Given, When, Then } = require('@cucumber/cucumber');

const normalize = (text) => text.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().replace(/[^a-z0-9]/g, '');
const numericPrice = (price) => Number(price.replace(/[^\d.]/g, ''));

Given('que navego a la página de Liverpool', async function () {
  await this.homePage.navigate();
});

When('busco el producto {string}', async function (searchTerm) {
  await this.homePage.searchFor(searchTerm);
  assert.match(await this.searchResultsPage.getHeading(), new RegExp(searchTerm, 'i'));
});

When('filtro los resultados por el color {string}', async function (color) {
  await this.searchResultsPage.filterByColor(color);
  assert.equal(await this.searchResultsPage.isColorSelected(color), true);
});

When('ordeno los productos del precio más bajo al más alto', async function () {
  await this.searchResultsPage.sortByLowestPrice();
  assert.match(this.searchResultsPage.getCurrentUrl(), /sort=sortPrice(?:%7C|\|)0/);
});

Then('obtengo e imprimo los primeros {int} productos con nombre y precio', async function (quantity) {
  this.uiProducts = await this.searchResultsPage.getFirstProducts(quantity);
  assert.equal(this.uiProducts.length, quantity);
  assert.ok(this.uiProducts.every(({ name, price }) => name && price.includes('$')));
  console.log(`\nPrimeros ${quantity} resultados de UI:`);
  console.table(this.uiProducts);
});

When(
  'intercepto la respuesta de resultados al buscar {string}, filtrar {string} y ordenar por menor precio',
  async function (searchTerm, color) {
    await this.homePage.searchFor(searchTerm);
    await this.searchResultsPage.filterByColor(color);
    await this.searchResultsPage.sortByLowestPrice();
    this.uiProducts = await this.searchResultsPage.getFirstProducts(5);
    this.interceptedResponse = await this.plpCollector.getLatestResponse();

    assert.ok(this.interceptedResponse, 'No se interceptó la respuesta de /api/plp/search');
    assert.match(this.interceptedResponse.url, /\/api\/plp\/search/);
  }
);

When('analizo la respuesta interceptada y extraigo los productos devueltos', function () {
  this.apiProducts = this.plpCollector.extractProducts(this.interceptedResponse.body);
  assert.ok(this.apiProducts.length > 0, 'La respuesta interceptada no contiene productos reconocibles');
});

When('valido cruzadamente los resultados de la interfaz con los datos de la respuesta', function () {
  this.comparison = this.uiProducts.map((uiProduct) => {
    const apiProduct = this.apiProducts.find((item) => normalize(item.name) === normalize(uiProduct.name));
    if (!apiProduct) return { ...uiProduct, status: 'No aparece en la respuesta' };

    const uiPrice = numericPrice(uiProduct.price);
    const priceMatches = apiProduct.prices.some((price) => Math.abs(price - uiPrice) < 0.01);
    return {
      ...uiProduct,
      apiName: apiProduct.name,
      apiPrices: apiProduct.prices.join(', '),
      status: priceMatches ? 'Coincide' : 'Nombre coincide; precio diferente'
    };
  });
  assert.equal(this.comparison.length, this.uiProducts.length);
});

Then(
  'afirmo que al menos {int} de los {int} resultados aparecen en la respuesta interceptada',
  function (minimumMatches, totalResults) {
    assert.equal(this.uiProducts.length, totalResults);
    this.matchCount = this.comparison.filter(({ apiName }) => apiName).length;
    assert.ok(
      this.matchCount >= minimumMatches,
      `Se esperaban al menos ${minimumMatches} coincidencias y se obtuvieron ${this.matchCount}`
    );
  }
);

Then('registro las discrepancias de nombre o precio entre la interfaz y la respuesta', function () {
  const discrepancies = this.comparison.filter(({ status }) => status !== 'Coincide');
  console.log(`\nServicio interceptado: ${this.interceptedResponse.url}`);
  console.log(`Coincidencias por nombre: ${this.matchCount}/${this.uiProducts.length}`);
  if (discrepancies.length > 0) {
    console.log('Discrepancias entre UI y servicio:');
    console.table(discrepancies);
  } else {
    console.log('Sin discrepancias de nombre o precio.');
  }
});
