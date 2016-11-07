package com.fintech.hospital.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.fintech.hospital.rssi.RssiDistanceModel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
  final RssiDistanceModel RSSI_MODEL = new RssiDistanceModel(-0.7801144,2.3889582, 3.6463172,-62) ;

  public TimedPosition() {
  }

  public TimedPosition(AP ap, long timestamp, double rssi) {
    this.gps = ap.getGps();
    this.floor = ap.getFloor();
    this.ap = ap.getAlias();
    this.timestamp = timestamp;
    this.radius = RSSI_MODEL.distance(rssi);
    this.rssi = rssi;
  }

  public TimedPosition(AP ap, long timestamp){
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

  public static TimedPosition mean(List<TimedPosition> allpos, double[] ratios) {
    if (allpos == null) throw new IllegalArgumentException("pos should not be null!");
    AP nearestAP = new AP();
    if (ratios == null) {
      ratios = new double[allpos.size()];
      Arrays.fill(ratios, 1.0 / allpos.size());
    }
    if (allpos.size() != ratios.length) {
      throw new IllegalArgumentException("pos and ratio size should match: pos="
          + allpos.size() + ", ratios=" + ratios.length);
    }
    double time = 0.0, lng = 0.0, lat = 0.0, maxratio = 0.0;
    int nearestAPIndex = 0;
    for (int i = 0; i < allpos.size(); i++) {
      time += allpos.get(i).getTimestamp() * ratios[i];
      lng += allpos.get(i).getGps().getLng() * ratios[i];
      lat += allpos.get(i).getGps().getLat() * ratios[i];
      if (ratios[i] > maxratio) {
        maxratio = ratios[i];
      }
      if(allpos.get(i).getRssi()<allpos.get(nearestAPIndex).getRssi())
        nearestAPIndex = i;
    }
    nearestAP.setGps(lng, lat);
    nearestAP.setAlias(allpos.get(nearestAPIndex).getAp());
    nearestAP.setFloor(allpos.get(nearestAPIndex).getFloor());
    TimedPosition meaned = new TimedPosition(nearestAP, (long) time);
    meaned.setRadius(allpos.get(0).getRadius());
    meaned.setRssi(allpos.get(nearestAPIndex).getRssi());
    return meaned;
  }

}
