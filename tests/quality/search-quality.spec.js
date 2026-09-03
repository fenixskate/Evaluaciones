const { test, expect } = require('@playwright/test');
const AxeBuilder = require('@axe-core/playwright').default;
const { HomePage } = require('../../src/pages/HomePage');
const { SearchResultsPage } = require('../../src/pages/SearchResultsPage');
const settings = require('../../config/settings');

for (const term of settings.searchTerms) {
  test.describe(term, () => {
    test('búsqueda parametrizada y presupuesto de carga', async ({ page }, testInfo) => {
      const home = new HomePage(page);
      const results = new SearchResultsPage(page);
      await home.navigate();
      const started = performance.now();
      await home.searchFor(term);
      await results.waitForResults(5);
      const seconds = (performance.now() - started) / 1000;
      await testInfo.attach('search-performance.json', {
        body: JSON.stringify({ term, seconds, budget: settings.maxSearchSeconds }),
        contentType: 'application/json'
      });
      expect((await results.getHeading()).toLowerCase()).toContain(term.toLowerCase());
      expect(seconds).toBeLessThan(settings.maxSearchSeconds);
      expect(await results.getFirstProducts(5)).toHaveLength(5);
    });

    test('accesibilidad axe-core', async ({ page }, testInfo) => {
      await new HomePage(page).navigate();
      await new HomePage(page).searchFor(term);
      await new SearchResultsPage(page).waitForResults(5);
      const scan = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa']).analyze();
      await testInfo.attach('axe-results.json', {
        body: JSON.stringify(scan, null, 2), contentType: 'application/json'
      });
      console.table(scan.violations.map(({ id, impact, nodes }) => ({ id, impact, affected: nodes.length })));
      // Producción es ajena al equipo: reportar por defecto; gate explícito sin ocultar hallazgos.
      if (process.env.AXE_STRICT === 'true') expect(scan.violations).toEqual([]);
    });

    test('regresión visual de resultados @visual', async ({ page }) => {
      await new HomePage(page).navigate();
      await new HomePage(page).searchFor(term);
      const results = new SearchResultsPage(page);
      await results.waitForResults(5);
      const { region, dynamicContent } = results.visualRegion();
      const snapshotName = `${term.replace(/[^a-z0-9]+/gi, '-').toLowerCase()}-results.png`;
      await expect(region).toHaveScreenshot(snapshotName, {
        animations: 'disabled', mask: dynamicContent
      });
    });
  });
}
