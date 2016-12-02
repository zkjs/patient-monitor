package com.fintech.hospital.push.model;

import com.alibaba.fastjson.JSON;

/**
 * push message for <a href="yunba.io">Yunba</a> push service
 *
 * @author baoqiang
 * @since 0.0.3
 */
public class PushMsg {

  public PushMsg() {
  }

  public PushMsg(final String subject, final String message) {
    this.subject = subject;
    this.message = message;
  }

  public PushMsg(final PushType type, final String subject, final String message) {
    this.type = type;
    this.subject = subject;
    this.message = message;
  }

  /**
   * push type: topic, alias
   */
  private PushType type;

  /**
   * message subject
   */
  private String subject;

  /**
   * message body
   */
  private String message;

  public PushType getType() {
    return type;
  }

  public void setType(PushType type) {
    this.type = type;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return JSON.toJSONString(this);
  }
}
