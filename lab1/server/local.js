import { createServer } from 'node:http';

const PORT = process.env.PORT ?? 3000;

createServer(async (req, res) => {
  res.status = (code) => {
    res.statusCode = code;
    return res;
  };
  res.json = (data) => {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(data));
  };

  const name = new URL(req.url, 'http://localhost').pathname.replace('/api/', '');
  try {
    const chunks = [];
    for await (const chunk of req) chunks.push(chunk);
    req.body = chunks.length ? JSON.parse(Buffer.concat(chunks)) : {};
    const { default: handler } = await import(`./api/${name}.js`);
    await handler(req, res);
  } catch (e) {
    res.status(404).json({ error: e.message });
  }
}).listen(PORT, '127.0.0.1', () => console.log(`passkey server on http://127.0.0.1:${PORT}`));
