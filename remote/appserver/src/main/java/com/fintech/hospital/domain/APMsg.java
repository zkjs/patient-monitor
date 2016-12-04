package com.fintech.hospital.domain;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author baoqiang
 */
public class APMsg {

  @JSONField(name = "bsid")
  private String apid;
  @JSONField(name = "data")
  private String payload;
  private Integer rssi;
  @JSONField(name = "bcid")
  private String bandId;

  @JSONField(serialize = false)
  private Long timestamp;

  private String address;
  private Integer floor;

  @JSONField(deserialize = false)
  private TimedPosition position;

  public String getApid() {
    return apid;
  }

  public void setApid(String apid) {
    this.apid = apid;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public Integer getRssi() {
    return rssi;
  }

  public void setRssi(Integer rssi) {
    this.rssi = rssi;
  }

  public String getBandId() {
    return bandId;
  }

  public void setBandId(String bandId) {
    this.bandId = bandId;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Integer getFloor() {
    return floor;
  }

  public void setFloor(Integer floor) {
    this.floor = floor;
  }

  public TimedPosition getPosition() {
    return position;
  }

  public void setPosition(TimedPosition position) {
    this.position = position;
  }
}
