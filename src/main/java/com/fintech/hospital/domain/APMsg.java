package com.fintech.hospital.domain;

/**
 * @author baoqiang
 */
public class APMsg {

  private String apid;
  private String payload;
  private Integer rssi;
  private String bandId;

  private byte[] pkg;

  public String getApid() {
    return apid;
  }

  public void setApid(String apid) {
    this.apid = apid;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    if (payload == null || payload.length() != 12)
      throw new IllegalArgumentException("payload format err");
    this.payload = payload;
    this.pkg = new byte[4];
    pkg[0] = Byte.valueOf(payload.substring(2, 4));
    this.bandId = payload.substring(4, 6);
    for (int i = 3, j = 1; i < 6; i++) {
      pkg[j++] = Byte.valueOf(payload.substring(i * 2, (i + 1) * 2));
    }
  }

  public String bracelet() {
    return bandId;
  }

  public Integer getRssi() {
    return rssi;
  }

  public void setRssi(Integer rssi) {
    this.rssi = rssi;
  }

  public boolean binded() {
    return pkg[2] == 1;
  }

  public boolean urgent() {
    return pkg[0] == 66;
  }

}
