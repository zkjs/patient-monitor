# Remote Positioning

- listen for socket.io position messages:
  - cache for realtime positioning
  - save to local db ( tracks )
- socket.io server (node.js): listen on local-position tracks for different hospitals


## socket.io

### config

            'connect': { token: $hospital, id: $custom_id }
            'subscribe': { bracelet: $bracelet_id }
            'publish': { topic: $topic, msg: $msg }


### messages

- `position` (publish on bracelet {bracelet_id})

            {
              "ap": "nearest ap addr",
              "floor": 3,
              "timestamp": 1478102400,
              "radius": 23.121,
              "gps": {"lng": 104.2312, "lat": 30.39102}
            }

## API

- bracelet tracks [GET /track/{bracelet}]

            {
              "status": "ok",
              "data": {
                "list": [{
                  "ap": "nearest ap addr",
                  "floor": 2,
                  "timestamp": 1478159590006,
                  "radius": 23.121,
                  "gps": {"lng" : 104.061346, "lat" : 30.641574}
                }]
            }

- bracelet tracks(relative coords) [GET /track/rel/{bracelet}]

            {
              "status": "ok",
              "data": {
                "list": [{
                  "ap": "ap112",
                  "floor": 2,
                  "timestamp": 1478159590006,
                  "radius": 23.121,
                  "gps": {"lng" : 10.0, "lat" : 0.641574}
                }],
                "aps": [{
                  "address": "middle",
                  "alias": "ap112",
                  "floor": 2,
                  "gps": {"lng": 12.212, "lat": 11.231}
                }]
            }

- bracelet last position [GET /track/{bracelet}/last]

            {
              "status": "ok",
              "data": {
                "address": "West 204",
                "floor": 2,
                "timestamp": 1478159590006,
                "radius": 23.121,
                "gps": {"lng" : 104.061346, "lat" : 30.641574}
              }
            }


## Data Model

- Bracelet Trace

column | type | description
-------|------|------------
id | ObjectId | mongo default `_id`
bracelet | string | bracelet id
ap | string | ap id
rssi | number | detected RSSI
create | Date | create date of the trace


- Bracelet Position

column | type | description
-------|------|------------
id | string | bracelet id
position | array | LngLat with timestamp, ap, floor, position tracks of the bracelet

