package com.fintech.hospital.domain;

import com.alibaba.fastjson.annotation.JSONField;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * @author baoqiang
 */
public class AP {

  @Override
  public String toString(){
    return alias;
  }

  @JSONField(serialize = false)
  @Id
  private ObjectId id;

  @JSONField(serialize = false)
  private Double longitude;
  @JSONField(serialize = false)
  private Double latitude;

  private String zone;

  private String address;
  private Integer floor;
  @Field("name")
  private String alias;
  @JSONField(serialize = false)
  private Integer status;
  @JSONField(serialize = false)
  @Field("create_on")
  private Date create;

  @Field("camera")
  private int enableShot;

  public boolean shotEnabled(){
    return enableShot==1;
  }

  public int getEnableShot() {
    return enableShot;
  }

  public void setEnableShot(int enableShot) {
    this.enableShot = enableShot;
  }

  public String getZone() {
    return zone;
  }

  public void setZone(String zone) {
    this.zone = zone;
  }

  public Integer getFloor() {
    return floor;
  }

  public void setFloor(Integer floor) {
    this.floor = floor;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Integer getStatus() {
    return status;
  }


  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }


  public LngLat getGps() {
    return new LngLat(longitude, latitude);
  }

  public void setGps(double longitude, double latitude) {
    this.longitude = longitude;
    this.latitude = latitude;
  }

  public Date getCreate() {
    return create;
  }

  public void setCreate(Date create) {
    this.create = create;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }
}
