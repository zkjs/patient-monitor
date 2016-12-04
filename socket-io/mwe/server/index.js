
const debug = require('debug')('socket.io');
var app = require('http').createServer(handler);
var io = require('socket.io')(app);
var fs = require('fs');

app.listen(8000);

function handler (req, res) {
  fs.readFile(__dirname + '/index.html',
    function (err, data) {
      if (err) {
        res.writeHead(500);
        return res.end('Error loading index.html');
      }

      res.writeHead(200);
      res.end(data);
    });
}

io.of('/server').on('connection', svr => {

  svr.on('role', role => {
    svr.join(role);

    if('local'===role){
      debug('local server ready');

      svr.on('aprssi', data => {
        svr.to('aprssi').emit('aprssi', data);
      });

    }else if('remote'===role){
      debug('remote server ready');

      svr.join('aprssi');

    }

    svr.emit('ready');
  });

  
  debug('connected ' + svr.id);

});

debug('server started...');
