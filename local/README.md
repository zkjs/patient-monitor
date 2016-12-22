# Local Resource Management

- listen for mqtt messages
  - `nursecall`: push to agent and wait for response
  - `position`: redirect to remote socket.io server
  - `common`: band drop off, update band status
- when `nursecall` responded by agent, send ack/reply to ap
- resource API

## mqtt

- `apstat` (heartbeat)

            {
              "bsid": "ap110",
              "timestamp": 1478102400,
              "version": 1.1,
              "macaddr": "82:e6:50:70:86:00",
              "temperature": 33.2,
              "disk": "40%",
              "mem": "40%",
              "cpu": "21%"
            }

- `ap` (publish format)

            {
              "ap": "ap110",
              "bracelet": "00",
              "cmd":
            }

- `nursecall` (publish format)

            {
              "apid": "ap110",
              "floor": 2,
              "zone": "581b1a6542aa101eebc77e60",
              "address": "West 208",
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

- `position` (receive and redirect format)

            {
              "ap": "nearest ap addr",
              "floor": 3,
              "timestamp": 1478102400,
              "radius": 23.121,
              "gps": {"lng": 104.2312, "lat": 30.39102}
            }

- `nursecallreply` (publish format) TODO

            {
              "alias": "ap110",
              "message": "Medic on the way"
            }

## API

- upload AP photo shots [POST /photo/{ap}{?bracelets,time}]

  - request body

              content-type: image/jpeg
              photo stream

  - response body

              {
                "status": "ok"
              }

- browse photos [GET /bracelet/photos/{bracelet}]

              {
                "status": "ok",
                "data": {
                  "list": [{
                    "id": "e23",
                    "path": "/photos/bracelet/ap/2018-01-01-111111.jpg"
                    "timestamp": 1478102400
                  }]
                }
              }


- map objects [GET /map]

            {
              "status": "ok",
              "data": {
                "list": [{
                  "id": "e23",
                  "title": "Building",
                  "lng" : 104.061346,
                  "lat" : 30.641574,
                  "floors": 4,
                  "drawables": [{
                    "title": "Block",
                    "data":{
                      "path": [
                        [104.203348, 30.432538],
                        [104.203229, 30.432341],
                        [104.203962, 30.431997],
                        [104.204082, 30.4322]
                      ]
                    },
                    "lng": 104.203655,
                    "lat": 30.432268,
                    "type": "AMap.Polygon"
                  }]
                }]
              }
            }

- map layers(floor starts from 0) [GET /map/{objid}]

            {
              "status": "ok",
              "data": {
                "list": [{
                  "id": "e24",
                  "floor": 2,
                  "title": "Medicine",
                  "drawables": [{
                    "title": "emergency",
                    "type": "AMap.Polygon",
                    "data":{
                      "path": [
                        [104.203348, 30.432538],
                        [104.203229, 30.432341],
                        [104.203962, 30.431997],
                        [104.204082, 30.4322]
                      ]
                    }
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

  - response body (binded): `attached` 标记离体状态: 0-离体, 1-正常

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
                "patientPhone": "13800138000",
                "attached": 1
              }]
             }
           }


## Data Model

- Bracelet

column | description
------|------------
id | -
name | 编号
status | 状态: 空闲/使用/离体
patient | 绑定人信息


- Patient

column | description
------|------------
name | 姓名
gender | 性别
room | 房号
phone | 联系电话
remark | 病因/备注

