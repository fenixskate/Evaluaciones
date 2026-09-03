const { World, setWorldConstructor } = require('@cucumber/cucumber');
const { HomePage } = require('../../src/pages/HomePage');
const { SearchResultsPage } = require('../../src/pages/SearchResultsPage');
const { PlpResponseCollector } = require('../../src/services/PlpResponseCollector');

class LiverpoolWorld extends World {
  constructor(options) {
    super(options);
  }

  initialize(page) {
    this.page = page;
    this.homePage = new HomePage(page);
    this.searchResultsPage = new SearchResultsPage(page);
    this.plpCollector = new PlpResponseCollector(page);
    this.plpCollector.start();
  }
}

setWorldConstructor(LiverpoolWorld);
