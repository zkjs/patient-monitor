package com.fintech.hospital.domain;

import com.alibaba.fastjson.annotation.JSONField;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

/**
 * @author baoqiang
 */
public class AP {

  @Override
  public int hashCode(){
    return this.getId().hashCode();
  }

  @Override
  public boolean equals(Object ap) {
    return ap == this || ap instanceof AP && ((AP) ap).getId().toHexString().equals(this.getId().toHexString());
  }

  @Override
  public String toString(){
    return alias;
  }

  @JSONField(serialize = false, deserialize = false)
  @Id
  private ObjectId id;

  @JSONField(name = "apid")
  public String getIdString(){
    return this.id.toHexString();
  }

  @JSONField(name = "apid")
  public void setId(String id) {
    this.id = new ObjectId(id);
  }

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
  private Date update;

  private List<String> triggers;
  @Field("trigger_logic")
  private String triggerLogic;

  private int camera;

  public String getTriggerLogic() {
    return triggerLogic;
  }

  public void setTriggerLogic(String triggerLogic) {
    this.triggerLogic = triggerLogic;
  }

  public Date getUpdate() {
    return update;
  }

  public void setUpdate(Date update) {
    this.update = update;
  }

  public List<String> getTriggers() {
    return triggers;
  }

  public void setTriggers(List<String> triggers) {
    this.triggers = triggers;
  }

  @JSONField(serialize = false)
  public boolean shotEnabled(){
    return camera ==1;
  }

  public int getCamera() {
    return camera;
  }

  public void setCamera(int camera) {
    this.camera = camera;
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

  @JSONField(deserialize = false)
  public void setId(ObjectId id) {
    this.id = id;
  }

  @JSONField(serialize = false)
  public LngLat getGps() {
    return new LngLat(longitude, latitude);
  }

  @JSONField(deserialize = false)
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

  @JSONField(serialize = false)
  public boolean reqValid() {
    return !(camera==0&&!triggers.isEmpty());
  }
}
