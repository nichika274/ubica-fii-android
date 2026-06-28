module.exports = (fotoUrl, req) => {
  if (!fotoUrl) return '';
  const baseUrl = `${req.protocol}://${req.get('host')}`;
  if (fotoUrl.startsWith('http')) return fotoUrl.replace(/^https?:\/\/[^\/]+/, baseUrl);
  return `${baseUrl}${fotoUrl}`;
};