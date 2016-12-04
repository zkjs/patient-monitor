const bunyan = require('bunyan');
const debug = require('debug')('socket.io');
const accesslog = bunyan.createLogger({
  name: 'socket.io-access',
  streams: [{
    type: 'rotating-file',
    path: 'log/sio-access.log',
    period: '1d', // daily rotation 
    count: 30 // keep 3 back copies 
  }]
}), svrlog = bunyan.createLogger({
  name: 'socket.io-server',
  streams: [{
    type: 'rotating-file',
    path: 'log/sio-server.log',
    period: '1d', // daily rotation 
    count: 30 // keep 3 back copies 
  }]
}), applog = bunyan.createLogger({
  name: 'socket.io-app',
  streams: [{
    type: 'rotating-file',
    path: 'log/sio-app.log',
    period: '1d', // daily rotation 
    count: 30 // keep 3 back copies 
  }]
});

var app = require('http').createServer(handler);
var io = require('socket.io')(app);
var fs = require('fs');

app.listen(9000);

function handler (req, res) {
  fs.readFile(__dirname + '/index.html',
    function (err, data) {
      if (err) {
        accesslog.info(err);
        res.writeHead(500);
        return res.end('Error loading index.html');
      }

      res.writeHead(200);
      res.end(data);
    });
}

io.of('/app').on('connection', app => {

  applog.info('%s connected from %s ', app.id, app.request.connection.remoteAddress);

  app.on('role', role => {
    app.join(role);

    if('app'===role){

      app.on('unsub', topic => {
        debug('socket %s off %s', app.id, topic);
        applog.info('%s off topic: %s', app.id, topic);
        app.leave(topic);
        app.to('server').emit('unsub', topic);
      });
      app.on('sub', topic => {
        debug('socket %s listening on %s', app.id, topic);
        applog.info('%s on topic: %s', app.id, topic);
        app.join(topic);
        app.to('server').emit('sub', topic);
      });
      debug('app ready');
      applog.info('admin app %s ready', app.request.connection.remoteAddress);

    }else if('server'===role){

      debug('app server ready');
      applog.info('app server %s ready', app.request.connection.remoteAddress);
      app.on('position', data => {
        var obj = eval('(' + data + ')');
        applog.info('new position for %s', obj.id);
        debug('providing new pos for %s', obj.id);
        app.to(obj.id).emit('position', obj);
      });

    }

    app.emit('ready');
  });

  
  debug('%s connected from %s ', app.id, app.request.connection.remoteAddress);

});

io.of('/server').on('connection', svr => {

  svrlog.info('%s connected from %s ', svr.id, svr.request.connection.remoteAddress);

  svr.on('role', role => {
    svr.join(role);

    if('local'===role){

      debug('local server ready');
      svrlog.info('local svr %s ready', svr.request.connection.remoteAddress);

      svr.on('aprssi', data => {

        svr.to('aprssi').emit('aprssi', data);
        svrlog.info('local server %s reporting position', svr.request.connection.remoteAddress);

      });

    }else if('remote'===role){

      svr.join('aprssi');
      debug('remote server ready');
      svrlog.info('remote svr %s ready', svr.request.connection.remoteAddress);

    }

    svr.emit('ready');
  });

  
  debug('%s connected from %s ', svr.id, svr.request.connection.remoteAddress);

});


debug('server started...');
accesslog.info('server started');

