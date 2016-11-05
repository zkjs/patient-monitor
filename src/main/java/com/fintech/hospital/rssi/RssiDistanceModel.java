package com.fintech.hospital.rssi;

/**
 * @author baoqiang
 */
public class RssiDistanceModel {

  public RssiDistanceModel(double interceptor, double multiplier, double power, double reference) {
    this.interceptor = interceptor;
    this.multiplier = multiplier;
    this.reference = reference;
    this.power = power;
  }

  public RssiDistanceModel() {
  }

  private double interceptor;

  private double multiplier;

  private double reference;

  private double power;

  public double getInterceptor() {
    return interceptor;
  }

  public void setInterceptor(double interceptor) {
    this.interceptor = interceptor;
  }

  public double getMultiplier() {
    return multiplier;
  }

  public void setMultiplier(double multiplier) {
    this.multiplier = multiplier;
  }

  public double getReference() {
    return reference;
  }

  public void setReference(double reference) {
    this.reference = reference;
  }

  public double getPower() {
    return power;
  }

  public void setPower(double power) {
    this.power = power;
  }

  public double distance(double rssi) {
    return this.getMultiplier() * Math.pow(rssi / this.getReference(), this.getPower()) + this.getInterceptor();
  }

}
