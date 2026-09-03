const settings = {
  baseURL: process.env.BASE_URL || 'https://www.liverpool.com.mx',
  searchTerms: JSON.parse(process.env.SEARCH_TERMS || '["playstation 5","xbox series x","nintendo switch"]'),
  maxSearchSeconds: Number(process.env.MAX_SEARCH_SECONDS || 20)
};
if (!Array.isArray(settings.searchTerms) || !settings.searchTerms.length ||
    settings.searchTerms.some(term => typeof term !== 'string' || !term.trim())) {
  throw new Error('SEARCH_TERMS debe ser un arreglo JSON de textos no vacíos');
}
if (!Number.isFinite(settings.maxSearchSeconds) || settings.maxSearchSeconds <= 0) {
  throw new Error('MAX_SEARCH_SECONDS debe ser mayor que cero');
}
module.exports = settings;
