# Patient Monitor

## Urgent Rescue

- listen for socket.io messages, redirect to frontend
- gather all messages to detect exact position
- when responded, cancel alert for ap
- save current position to tracks


## Patient Tracing

- listen for socket.io messages, cache the tracks for given periods ( 5s )
- positioning and save to tracks

>
  - input> ap: bracelet:rssi:time
  - cache> bracelet stack of size 5 (when full or expired, pop all): ap-lnglat:distance
  - db> bracelet: position


## API

- map objects [GET /map]

            {
              "status": "ok",
              "data": {
                "list": [{
                  "obj": "e23",
                  "gps": {"lng" : 104.061346, "lat" : 30.641574},
                  "floors": 4,
                  "drawables": [{
                    "title": "emergency",
                    "path": [
                      [104.203348, 30.432538],
                      [104.203229, 30.432341],
                      [104.203962, 30.431997],
                      [104.204082, 30.4322]
                    ],
                    "lng": 104.203655,
                    "lat": 30.432268,
                    "type": "AMap.Polygon"
                  }]
                }]
              }
            }

- map layers(floor starts from 0) [GET /map/{obj}/{floor}]

            {
              "status": "ok",
              "data": {
                "list": [{
                  "obj": "e24",
                  "floor": 2,
                  "drawables": [{
                    "title": "emergency",
                    "type": "AMap.Polygon",
                    "lng": 104.203355,
                    "lat": 30.432499,
                    "path": [
                      [104.203348, 30.432538],
                      [104.203229, 30.432341],
                      [104.203962, 30.431997],
                      [104.204082, 30.4322]
                    ]
                  }]
                }]
              }
            }

- respond to emergency [PUT /rescue]

  - request body

            {
              "apid": "ap110",
              "response": "doctorid"
            }

  - response body

            {
              "status": "ok"
            }

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

- bind bracelet to patient [PUT /bracelet]

  - request body (gender: 0-female, 1-male)

            {
              "bracelet": "581b1a6542aa101eebc77e60",
              "patientName": "Wang Nima",
              "patientGender": "1",
              "patientAge": 32,
              "patientRemark": "leg broken",
              "patientRoom": "302",
              "patientPhone": "13800138000"
            }

  - response body

            {
              "status": "ok"
            }

- unbind bracelet [PUT /bracelet/binded/{bracelet}]

  - request body

            {
              "patientName": "Wang Nima"
              "patientGender": "1",
              "patientRoom": "302"
            }

  - response body

            {
              "status": "ok",
              "data": {
                "bracelet": "581b1a6542aa101eebc77e60",
                "name": "82"
              }
            }

- bracelet list [GET /bracelet{?binded}] (binded: 0,1; true,false)

  - response body (not binded)

           {
             "status": "ok",
             "data": {
              "list": [{
                "bracelet": "581b1a6542aa101eebc77e60",
                "name": "82"
              }]
             }
           }

  - response body (binded)

           {
             "status": "ok",
             "data": {
              "list": [{
                "bracelet": "581b1a6542aa101eebc77e60",
                "name": "82",
                "patientName": "Wang Nima",
                "patientGender": "1",
                "patientAge": 32,
                "patientRoom": "320",
                "patientRemark": "leg broken",
                "patientPhone": "13800138000"
              }]
             }
           }


## Yunba.io

- broadcast msg for AP detected emergency:

            {
              "topic": "demo",
              "message": {
                "apid": "ap110",
                "floor": 2,
                "address": "West 208",
                "alert": "y",
                "message": "someone's calling for help",
                "payload": "126683000000",
                "bracelet": "581b1a6542aa101eebc77e60",
                "position": {
                  "ap": "nearest ap addr",
                  "floor": 2,
                  "timestamp": 1478159590006,
                  "radius": 23.121,
                  "gps": {"lng" : 104.061346, "lat" : 30.641574}
                }
              }
            }

- emergency response alias push:

            {
              "alias": "ap110",
              "message": "Medic on the way"
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
