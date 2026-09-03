class PlpResponseCollector {
  constructor(page) {
    this.page = page;
    this.responses = [];
    this.pending = [];
  }

  start() {
    this.page.on('response', (response) => {
      if (!response.url().includes('/api/plp/search') || response.status() !== 200) return;

      const capture = response.json()
        .then((body) => this.responses.push({ url: response.url(), body }))
        .catch(() => undefined);
      this.pending.push(capture);
    });
  }

  async getLatestResponse() {
    await Promise.all(this.pending);
    const latest = this.responses.at(-1);
    return latest ?? null;
  }

  extractProducts(payload) {
    const candidates = [];
    const visited = new Set();

    const walk = (value) => {
      if (!value || typeof value !== 'object' || visited.has(value)) return;
      visited.add(value);

      if (!Array.isArray(value)) {
        const name = this.readName(value);
        const prices = this.readPrices(value);
        if (name && prices.length) candidates.push({ name, prices });
      }

      Object.values(value).forEach(walk);
    };

    walk(payload);
    return [...new Map(candidates.map((product) => [this.normalize(product.name), product])).values()];
  }

  readName(product) {
    const keys = ['productDisplayName', 'productName', 'displayName', 'title', 'name'];
    const key = keys.find((candidate) => typeof product[candidate] === 'string');
    return key ? product[key].trim() : null;
  }

  readPrices(product) {
    const prices = [];
    const visit = (value, key = '') => {
      if (value == null) return;
      if (/price|precio/i.test(key) && (typeof value === 'number' || typeof value === 'string')) {
        const parsed = Number(String(value).replace(/[^\d.]/g, ''));
        if (Number.isFinite(parsed) && parsed > 0) prices.push(parsed);
      } else if (typeof value === 'object') {
        Object.entries(value).forEach(([childKey, child]) => visit(child, childKey));
      }
    };
    visit(product);
    return [...new Set(prices)];
  }

  normalize(text) {
    return text.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().replace(/[^a-z0-9]/g, '');
  }
}

module.exports = { PlpResponseCollector };
