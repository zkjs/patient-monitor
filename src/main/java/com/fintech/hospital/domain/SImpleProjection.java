package com.fintech.hospital.domain;

import org.springframework.data.annotation.Id;

/**
 * @author baoqiang
 */
public class SimpleProjection {
  @Id
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
