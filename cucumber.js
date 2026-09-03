module.exports = {
  default: {
    paths: ['tests/features/**/*.feature'],
    require: ['tests/support/**/*.js', 'tests/step-definitions/**/*.js'],
    format: ['progress', 'html:reports/cucumber-report.html'],
    publishQuiet: true
  }
};
