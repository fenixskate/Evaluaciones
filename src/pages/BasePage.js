class BasePage {
  constructor(page) {
    this.page = page;
  }

  async open(path) {
    await this.page.goto(path);
  }

  async waitUntilVisible(locator) {
    await locator.waitFor({ state: 'visible' });
  }

  async waitForUrl(predicate) {
    await this.page.waitForURL(predicate);
  }

  async waitForCondition(condition, timeout = 15_000) {
    const deadline = Date.now() + timeout;
    while (Date.now() < deadline) {
      if (await condition()) return;
      await this.page.waitForTimeout(200);
    }
    throw new Error(`La condición no se cumplió en ${timeout} ms`);
  }
}

module.exports = { BasePage };
