
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

io.of('/app').on('connection', app => {

  app.on('role', role => {
    app.join(role);

    if('app'===role){

      app.on('sub', topic => {
        debug('socket listening on %s', topic);
        app.join(topic);
        app.to('server').emit('sub', topic);
      });
      debug('app ready');

    }else if('server'===role){

      debug('server ready');
      app.on('position', data => {
        debug('providing new pos for %s', data.id);
        app.to(data.id).emit('position', data);
      });

    }

    app.emit('ready');
  });

  
  debug('connected ' + app.id);

});

debug('server started...');
