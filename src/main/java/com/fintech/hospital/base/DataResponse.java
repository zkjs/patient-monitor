package com.fintech.hospital.base;

import com.alibaba.fastjson.JSON;

/**
 * @author baoqiang
 */
public class DataResponse {

  private Object data;
  private String status;

  public DataResponse(Object data) {
    this.status = "ok";
    this.data = data;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return JSON.toJSONString(data);
  }
}
