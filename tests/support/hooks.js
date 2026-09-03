const { Before, After, Status, setDefaultTimeout } = require('@cucumber/cucumber');
const { chromium } = require('playwright');
const settings = require('../../config/settings');

setDefaultTimeout(120_000);

Before(async function () {
  this.browser = await chromium.launch({
    channel: 'chrome',
    headless: process.env.HEADED !== 'true',
    args: ['--disable-blink-features=AutomationControlled']
  });
  this.context = await this.browser.newContext({
    baseURL: settings.baseURL,
    locale: 'es-MX',
    timezoneId: 'America/Mexico_City',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36',
    extraHTTPHeaders: { 'Accept-Language': 'es-MX,es;q=0.9,en;q=0.8' },
    recordVideo: process.env.RECORD_VIDEO === 'true' ? { dir: 'test-results/videos' } : undefined
  });
  const page = await this.context.newPage();
  page.setDefaultTimeout(15_000);
  page.setDefaultNavigationTimeout(30_000);
  this.initialize(page);
});

After(async function ({ result }) {
  if (result?.status === Status.FAILED && this.page) {
    await this.attach(await this.page.screenshot({ fullPage: true }), 'image/png');
  }
  await this.context?.close();
  await this.browser?.close();
});
