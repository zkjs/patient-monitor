package com.fintech.hospital.domain;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Date;

/**
 * @author baoqiang
 */
public class TimedPosition {

  @Override
  public String toString() {
    return String.format("%s@%s, near: %s-floor[%s]",
        gps.toString(), new Date(timestamp).toInstant().toString(),
        ap, floor
    );
  }

  public TimedPosition() {
  }

  public TimedPosition(AP ap, long timestamp) {
    this.gps = ap.getGps();
    this.floor = ap.getFloor();
    this.ap = ap.getAlias();
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
    AP nearestAP = new AP();
    if (ratios == null) {
      ratios = new double[allpos.length];
      Arrays.fill(ratios, 1.0 / allpos.length);
    }
    if (allpos.length != ratios.length) {
      throw new IllegalArgumentException("pos and ratio size should match: pos="
          + allpos.length + ", ratios=" + ratios.length);
    }
    double time = 0.0, lng = 0.0, lat = 0.0, maxratio = 0.0;
    int nearestAPIndex = 0;
    for (int i = 0; i < allpos.length; i++) {
      time += allpos[i].getTimestamp() * ratios[i];
      lng = +allpos[i].getGps().getLng() * ratios[i];
      lat = +allpos[i].getGps().getLat() * ratios[i];
      if(ratios[i]>maxratio){
        maxratio = ratios[i];
        nearestAPIndex = i;
      }
    }
    nearestAP.setGps(lng, lat);
    nearestAP.setAlias(allpos[nearestAPIndex].getAp());
    nearestAP.setFloor(allpos[nearestAPIndex].getFloor());
    return new TimedPosition(nearestAP, (long) time);
  }

}
