
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

      app.on('unsub', topic => {
        debug('socket %s off %s', app.id, topic);
        app.leave(topic);
        app.to('server').emit('unsub', topic);
      });
      app.on('sub', topic => {
        debug('socket %s listening on %s', app.id, topic);
        app.join(topic);
        app.to('server').emit('sub', topic);
      });
      debug('app ready');

    }else if('server'===role){

      debug('app server ready');
      app.on('position', data => {
        var obj = eval('(' + data + ')');
        debug('providing new pos for %s', obj.id);
        app.to(obj.id).emit('position', obj);
      });

    }

    app.emit('ready');
  });

  
  debug('%s connected from %s ', app.id, app.request.connection.remoteAddress;);

});

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

  
  debug('%s connected from %s ', svr.id, svr.request.connection.remoteAddress;);

});


debug('server started...');

