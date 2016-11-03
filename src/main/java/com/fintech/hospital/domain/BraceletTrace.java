package com.fintech.hospital.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;

/**
 * @author baoqiang
 */
public class BraceletTrace {

  public BraceletTrace() {
  }

  public BraceletTrace(String apid, int rssi, LngLat gps) {
    this.ap = apid;
    this.rssi = rssi;
    this.gps = gps;
    this.create = new Date();
  }

  @Id
  private String id;
  @Indexed
  private String bracelet;
  private String ap;
  private Integer rssi;
  private LngLat gps;
  private Date create;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBracelet() {
    return bracelet;
  }

  public void setBracelet(String bracelet) {
    this.bracelet = bracelet;
  }

  public Date getCreate() {
    return create;
  }

  public void setCreate(Date create) {
    this.create = create;
  }

  public String getAp() {
    return ap;
  }

  public void setAp(String ap) {
    this.ap = ap;
  }

  public Integer getRssi() {
    return rssi;
  }

  public void setRssi(Integer rssi) {
    this.rssi = rssi;
  }

  public LngLat getGps() {
    return gps;
  }

  public void setGps(LngLat gps) {
    this.gps = gps;
  }
}
