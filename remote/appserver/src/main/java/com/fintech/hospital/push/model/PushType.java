package com.fintech.hospital.push.model;

/**
 * push type, provided by push service supplier
 *
 * @author baoqiang
 */
public enum PushType {

  BROADCAST("topic"), ALIAS("alias");

  private String type;

  PushType(final String type) {
    this.type = type;
  }

  public String type() {
    return type;
  }

}
