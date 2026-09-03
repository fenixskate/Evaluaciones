class LiverpoolSelectors {
  constructor(page) { this.page = page; }

  get searchInput() {
    return this.page.getByRole('textbox', { name: 'Buscar por producto, categoría y más...' });
  }

  get resultsHeading() { return this.page.getByRole('heading', { level: 1 }); }
  get sortButton() { return this.page.getByRole('button', { name: /^Ordenar por:/ }); }
  get lowestPriceOption() { return this.page.getByText('Menor precio', { exact: true }); }
  get productCards() { return this.page.locator('a[href*="/tienda/pdp/"]:has(section)'); }
  get resultsRegion() { return this.page.getByRole('main'); }
  get dynamicProductContent() { return this.productCards.locator('img, h3, h4, [data-testid$="-price"]'); }

  colorCheckbox(color) {
    return this.page.getByRole('checkbox', { name: new RegExp(`^${color} \\(`, 'i') });
  }

  productName(card) { return card.locator('h3'); }
  productCurrentPrice(card) { return card.locator('[data-testid$="-price"] > span').first(); }
}

module.exports = { LiverpoolSelectors };
