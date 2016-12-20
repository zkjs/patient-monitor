package com.fintech.hospital.domain;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * @author baoqiang
 */
public class LngLat {

  @Override
  public String toString() {
    return String.format("[%s,%s]", lng, lat);
  }

  private double lng;
  private double lat;

  public LngLat() {
  }

  public LngLat(double lng, double lat) {
    this.lng = lng;
    this.lat = lat;
  }

  public LngLat(double[] lnglat) {
    this.lng = lnglat[0];
    this.lat = lnglat[1];
  }

  public void set(double lng, double lat) {
    this.lng = lng;
    this.lat = lat;
  }

  public double[] arr() {
    return new double[]{lng, lat};
  }

  public Vector2D vector() {
    return new Vector2D(lng, lat);
  }

  public double getLng() {
    return lng;
  }

  public void setLng(double lng) {
    this.lng = lng;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }
}
