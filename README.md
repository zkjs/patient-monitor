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

- respond to emergency [PUT /rescue]

  - request body
  
      {
        "apid": "83",
        "response": "doctorid"
      }
  
  
  - response body
  
      {
        "status": "ok"
      }
      
- broadcast msg for AP detected emergency:

      {
        "topic": "demo",
        "message": {"apid": "ap110", "payload": "126683000000", "rssi": -32, "alert": "y", "bracelet": "82"}
      }


## Data Model

- AP

column | type | description
-------|------|------------
id | string | AP id
gps | LngLat | AP location
status | string | 
create | Date | create date


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
position | array | LngLat with timestamp, position tracks of the bracelet

