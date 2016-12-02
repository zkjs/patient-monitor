package com.fintech.hospital.domain;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author baoqiang
 */
public class MapDrawable {

  @JSONField(serialize = false)
  @Id
  private String id;
  private String title;
  private String type;
  @Field("longitude")
  private Double lng;
  @Field("latitude")
  private Double lat;
  private JSONObject data;

  @JSONField(serialize = false)
  private ObjectId part;

  public ObjectId getPart() {
    return part;
  }

  public void setPart(ObjectId part) {
    this.part = part;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Double getLng() {
    return lng;
  }

  public void setLng(Double lng) {
    this.lng = lng;
  }

  public Double getLat() {
    return lat;
  }

  public void setLat(Double lat) {
    this.lat = lat;
  }

  public JSONObject getData() {
    return data;
  }

  public void setData(JSONObject data) {
    this.data = data;
  }
}
