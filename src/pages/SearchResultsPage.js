const { BasePage } = require('./BasePage');
const { LiverpoolSelectors } = require('./LiverpoolSelectors');

class SearchResultsPage extends BasePage {
  constructor(page) {
    super(page);
    this.selectors = new LiverpoolSelectors(page);
  }

  async getHeading() {
    await this.waitUntilVisible(this.selectors.resultsHeading);
    return (await this.selectors.resultsHeading.innerText()).trim();
  }

  async filterByColor(color) {
    const displayedColor = ({ White: 'Blanco' })[color] ?? color;
    const checkbox = this.selectors.colorCheckbox(displayedColor);
    await checkbox.scrollIntoViewIfNeeded();
    await checkbox.click({ force: true });
    await this.waitForCondition(() => checkbox.isChecked());
    await this.waitUntilVisible(this.selectors.productCards.first());
  }

  async isColorSelected(color) {
    const displayedColor = ({ White: 'Blanco' })[color] ?? color;
    return this.selectors.colorCheckbox(displayedColor).isChecked();
  }

  async sortByLowestPrice() {
    await this.selectors.sortButton.click();
    await this.selectors.lowestPriceOption.click();
    await this.waitForUrl(/sort=sortPrice(?:%7C|\|)0/);
    await this.waitUntilVisible(this.selectors.productCards.first());
  }

  getCurrentUrl() {
    return this.page.url();
  }

  async getFirstProducts(limit = 5) {
    await this.waitForResults(limit);
    const products = [];

    for (let index = 0; index < limit; index += 1) {
      const card = this.selectors.productCards.nth(index);
      const name = (await this.selectors.productName(card).innerText()).trim();
      const priceText = (await this.selectors.productCurrentPrice(card).textContent()).replace(/\s+/g, '');
      const price = priceText.match(/\$[\d,]+\.\d{2}/)?.[0] ?? priceText;
      products.push({ name, price });
    }

    return products;
  }

  async waitForResults(minimum = 5) {
    await this.waitUntilVisible(this.selectors.productCards.nth(minimum - 1));
  }

  visualRegion() {
    return { region: this.selectors.resultsRegion, dynamicContent: [this.selectors.dynamicProductContent] };
  }
}

module.exports = { SearchResultsPage };
