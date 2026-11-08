const { exec } = require('child_process');
const express = require('express');

const app = express();
// Behind a reverse proxy that passes X-Forwarded-For through, req.ip is
// the address the header names, not the proxy's socket address.
app.set('trust proxy', 1);

app.get('/whoami', (req, res) => {
  const ip = req.ip;
  exec('ping -c 1 ' + ip, (err, stdout) => {
    res.send(stdout);
  });
});

app.listen(3000);
