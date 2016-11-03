package com.fintech.hospital.domain;

import java.util.Arrays;
import java.util.Date;

/**
 * @author baoqiang
 */
public class TimedPosition {

  @Override
  public String toString() {
    return gps.toString() + "@" + new Date(timestamp).toInstant().toString();
  }

  public TimedPosition() {
  }

  public TimedPosition(LngLat gps, long timestamp) {
    this.gps = gps;
    this.timestamp = timestamp;
  }

  private LngLat gps;
  private Integer floor;
  private String ap;
  private long timestamp;

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

  public static TimedPosition mean(TimedPosition[] allpos, double[] ratios) {
    if (allpos == null) throw new IllegalArgumentException("pos should not be null!");
    if (ratios == null) {
      ratios = new double[allpos.length];
      Arrays.fill(ratios, 1.0 / allpos.length);
    }
    if (allpos.length != ratios.length) {
      throw new IllegalArgumentException("pos and ratio size should match: pos="
          + allpos.length + ", ratios=" + ratios.length);
    }
    double time = 0.0, lng = 0.0, lat = 0.0;
    for (int i = 0; i < allpos.length; i++) {
      time += allpos[i].getTimestamp() * ratios[i];
      lng = +allpos[i].getGps().getLng() * ratios[i];
      lat = +allpos[i].getGps().getLat() * ratios[i];
    }
    return new TimedPosition(new LngLat(lng, lat), (long) time);
  }

}
