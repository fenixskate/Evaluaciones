const { BasePage } = require('./BasePage');
const { LiverpoolSelectors } = require('./LiverpoolSelectors');

class HomePage extends BasePage {
  constructor(page) {
    super(page);
    this.selectors = new LiverpoolSelectors(page);
  }

  async navigate() {
    await this.open('/tienda/home');
    await this.waitUntilVisible(this.selectors.searchInput);
  }

  async searchFor(term) {
    await this.selectors.searchInput.fill(term);
    await Promise.all([
      this.waitForUrl((url) => url.searchParams.has('s')),
      this.selectors.searchInput.press('Enter')
    ]);
  }
}

module.exports = { HomePage };
