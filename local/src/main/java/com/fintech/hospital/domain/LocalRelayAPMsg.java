package com.fintech.hospital.domain;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author baoqiang
 */
public class LocalRelayAPMsg {

  @JSONField(name = "apmsg")
  private APMsg apMsg;

  private Bracelet bracelet;

  public APMsg getApMsg() {
    return apMsg;
  }

  public void setApMsg(APMsg apMsg) {
    this.apMsg = apMsg;
  }

  public Bracelet getBracelet() {
    return bracelet;
  }

  public void setBracelet(Bracelet bracelet) {
    this.bracelet = bracelet;
  }
}
