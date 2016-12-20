package com.fintech.hospital.domain;

import com.alibaba.fastjson.annotation.JSONField;
import org.bson.types.ObjectId;

/**
 * @author baoqiang
 */
public class BraceletPhoto {

  private String id;
  @JSONField(serialize = false)
  private ObjectId bracelet;
  private String path;
  private Long timestamp;

  public BraceletPhoto(String bracelet, String path, long timestamp) {
    this.bracelet = new ObjectId(bracelet);
    this.path = path;
    this.timestamp = timestamp;
  }

  public BraceletPhoto() {
  }

  public ObjectId getBracelet() {
    return bracelet;
  }

  public void setBracelet(ObjectId bracelet) {
    this.bracelet = bracelet;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

}
