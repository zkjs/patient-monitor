# socket.io server

- namespaces (hospital id)

  - between servers: `/sever`
  - between server and APP: `/app`

- rooms

  - bracelet: `$bracelet_id`
  - server: `server`

- events

  - position collection: `('aprssi', [ap rssi list])`
  - new position push: `('position', { bracelet: $bracelet_id, position: $position data } )`
  - listen on a bracelet's position: `('sub', $bracelet_id)`

## remote & admin APP

position track pushing: (APP - admin app, SVR - remote server, SIO - socket.io server), see [socket.io@app](./mwe/app)

            APP: connect to socket.io server on namespace '/app'
            APP: emit('role', 'app')
            APP: emit('sub', $bracelet_id)
            APP: on( 'position', pos => render(pos) )

            SIO: app_socket.join('app')
            SIO: app_socket.on( 'sub', () => app_socket.to('server').emit('sub', $bracelet_id) )
            SIO: app_socket.join($bracelet_id) 

            SIO: svr_socket.join('server')
            SIO: svr_socket.on('position', pos => svr_socket.to($bracelet_id).emit('position', pos) )

            SVR: connect to socket.io server on namespace  '/app'
            APP: emit('role', 'server')
            SVR: on('sub', $bracelet_id => intercept_on($bracelet_id))
            SVR: emit('position', {id: $bracelet_id, position: $pos})


## remote & local

position data collecting: (RMT - remote, LOC - local, SIO - socket.io server), see [socket.io@server](./mwe/server)

            RMT: connect to socket.io server on namespace  '/server/'
            RMT: on('aprssi', (pos) => process(pos))

            LOC: connect to socket.io server on namespace  '/server/'
            LOC: emit('aprssi', $pos)

            SIO: rmt_socket.join('aprssi')
            SIO: loc_socket.on('aprssi', (pos) => loc_socket.to('aprssi').emit('aprssi', pos)
