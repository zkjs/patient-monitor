package com.fintech.hospital.domain;

import java.util.Date;

/**
 * @author baoqiang
 */
public class AP {

  private String id;
  private LngLat gps;
  private String status;
  private Date create;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public LngLat getGps() {
    return gps;
  }

  public void setGps(LngLat gps) {
    this.gps = gps;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Date getCreate() {
    return create;
  }

  public void setCreate(Date create) {
    this.create = create;
  }


}
