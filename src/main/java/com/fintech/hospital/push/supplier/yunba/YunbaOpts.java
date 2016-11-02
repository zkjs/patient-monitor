package com.fintech.hospital.push.supplier.yunba;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author baoqiang
 */
public class YunbaOpts {

  @JSONField(name = "time_to_live")
  public Integer ttl;
  @JSONField(name = "apn_json")
  public YunbaApn apn;
  public String qos;

  public YunbaOpts() {
  }

  public YunbaOpts(YunbaAps aps) {
    this.apn = new YunbaApn();
    this.apn.aps = aps;
  }

  public void qos(String qos) {
    this.qos = qos;
  }

  public static class YunbaApn {
    public YunbaAps aps;
  }

  public static class YunbaAps {
    public YunbaAps(Object message, String alert) {
      this.alert = alert;
      this.message = message;
    }

    @JSONField(name = "content-available")
    public Integer available;
    public String alert;
    public Object message;
  }

}
