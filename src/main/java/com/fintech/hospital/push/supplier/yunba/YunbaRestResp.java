package com.fintech.hospital.push.supplier.yunba;

import com.alibaba.fastjson.JSONObject;

/**
 * @author baoqiang
 */
public class YunbaRestResp {

  private int status;
  private String error;
  private String alias;
  private String messageId;
  private JSONObject results;

  public JSONObject getResults() {
    return results;
  }

  public void setResults(JSONObject results) {
    this.results = results;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }
}
