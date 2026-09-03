const { defineConfig } = require('@playwright/test');
const settings = require('./config/settings');

module.exports = defineConfig({
  testDir: './tests/quality',
  timeout: 90_000,
  fullyParallel: true,
  workers: process.env.CI ? 2 : 3,
  retries: 0,
  reporter: [['list'], ['html', { outputFolder: 'reports/quality', open: 'never' }]],
  expect: { timeout: 15_000, toHaveScreenshot: { maxDiffPixelRatio: 0.01 } },
  use: {
    baseURL: settings.baseURL,
    headless: true,
    locale: 'es-MX',
    timezoneId: 'America/Mexico_City',
    viewport: { width: 1280, height: 900 },
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    actionTimeout: 15_000
  },
  projects: [
    { name: 'chrome', use: {
      browserName: 'chromium', channel: 'chrome',
      userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36',
      launchOptions: { args: ['--disable-blink-features=AutomationControlled'] }
    } },
    { name: 'firefox', use: { browserName: 'firefox' } },
    { name: 'webkit', use: { browserName: 'webkit' } }
  ]
});
