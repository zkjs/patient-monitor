package com.fintech.hospital.push.model;

import com.alibaba.fastjson.annotation.JSONField;
import com.fintech.hospital.domain.AP;

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

  private String alert;
  private String address;
  private Integer floor;
  private String message;

  private String bracelet;

  public void fillAP(AP ap) {
    this.address = ap.getAddress();
    this.floor = ap.getFloor();
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

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getAlert() {
    return alert;
  }

  public void setAlert(String alert) {
    this.alert = alert;
  }

  public String getBracelet() {
    return bracelet;
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

  public void setBracelet(String bracelet) {
    this.bracelet = bracelet;
  }

  private byte pkg;

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
    if (payload == null)
      throw new IllegalArgumentException("payload format err");
    this.payload = payload;
    pkg = Byte.valueOf(payload.substring(2, 4));
  }

  public Integer getRssi() {
    return rssi;
  }

  public void setRssi(Integer rssi) {
    this.rssi = rssi;
  }

  public boolean urgent() {
    return pkg == 66;
  }

  public boolean dropped(){
    return pkg == 68;
  }

}
