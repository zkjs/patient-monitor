package com.fintech.hospital.domain;

import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * @author baoqiang
 */
public class BraceletPosition {

  @Id
  private String id;
  private List<TimedPosition> position;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<TimedPosition> getPosition() {
    return position;
  }

  public void setPosition(List<TimedPosition> position) {
    this.position = position;
  }


}

