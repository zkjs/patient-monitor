package com.fintech.hospital.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.fintech.hospital.rssi.RssiDistanceModel;
import org.springframework.data.annotation.Transient;

import java.util.Date;

/**
 * @author baoqiang
 */
public class TimedPosition {

  @Override
  public String toString() {
    return String.format("%s (radius: %s), near: %s-floor[%s] @%s",
        gps.toString(), radius, ap, floor,
        new Date(timestamp).toInstant().toString()
    );
  }

  @JSONField(serialize = false)
  @Transient
  public static final RssiDistanceModel RSSI_MODEL = new RssiDistanceModel(-0.2105552, 1.2269221, 6.0833473, -57);

  public TimedPosition() {
  }

  public TimedPosition(AP ap, long timestamp, double rssi) {
    //this.gps = ap.getGps();
    this.floor = ap.getFloor();
    this.ap = ap.getAlias();
    this.timestamp = timestamp;
    this.radius = RSSI_MODEL.distance(rssi);
    this.rssi = rssi;
  }

  public TimedPosition(AP ap, long timestamp) {
    this.gps = ap.getGps();
    this.floor = ap.getFloor();
    this.ap = ap.getAlias();
    this.timestamp = timestamp;
  }

  private Double rssi;
  /**
   * search radius centering@gps
   */
  private Double radius;

  private LngLat gps;
  private Integer floor;
  private String ap;
  private long timestamp;

  @Transient
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Double getRssi() {
    return rssi;
  }

  public void setRssi(Double rssi) {
    this.rssi = rssi;
    this.radius = RSSI_MODEL.distance(rssi);
  }

  public Double getRadius() {
    return radius;
  }

  public void setRadius(Double radius) {
    this.radius = radius;
  }

  public Integer getFloor() {
    return floor;
  }

  public void setFloor(Integer floor) {
    this.floor = floor;
  }

  public String getAp() {
    return ap;
  }

  public void setAp(String ap) {
    this.ap = ap;
  }

  public LngLat getGps() {
    return gps;
  }

  public void setGps(LngLat gps) {
    this.gps = gps;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

}
