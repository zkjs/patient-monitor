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
  @JSONField(name = "bcid", serialize = false)
  private String bandId;

  @JSONField(serialize = false)
  private Long timestamp;

  private String alert;
  private String address;
  private Integer floor;
  private String message;

  @JSONField(deserialize = false)
  private TimedPosition position;

  private String bracelet;

  public void fillAP(AP ap) {
    this.address = ap.getAddress();
    this.floor = ap.getFloor();
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

  public TimedPosition getPosition() {
    return position;
  }

  public void setPosition(TimedPosition position) {
    this.position = position;
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

  private byte[] pkg;

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
    if (payload.length() != 52) {
      this.pkg = new byte[4];
      pkg[0] = Byte.valueOf(payload.substring(2, 4));
      this.bandId = payload.substring(4, 6);
      for (int i = 3, j = 1; i < 6; i++) {
        pkg[j++] = Byte.valueOf(payload.substring(i * 2, (i + 1) * 2));
      }
    }
  }

  public String braceletBleId() {
    return bandId;
  }

  public Integer getRssi() {
    return rssi;
  }

  public void setRssi(Integer rssi) {
    this.rssi = rssi;
  }

  public boolean binded() {
    return pkg[2] == 1;
  }

  public boolean urgent() {
    return (payload.length() == 52 && payload.substring(20, 22).equalsIgnoreCase("A0")) || pkg[0] == 66;
  }

  public boolean dropped() {
    return pkg[0] == 68;
  }

}
