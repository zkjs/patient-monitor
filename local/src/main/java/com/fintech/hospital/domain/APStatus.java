package com.fintech.hospital.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @author baoqiang
 */
public class APStatus {

  private String bsid;

  @JSONField(name = "os_uptime")
  private String osUpTime;

  private List<String> features;

  private String ip;

  private String mac;

  private float version;

  @JSONField(name = "script_uptime")
  private String pyUpTime;

  @JSONField(name = "cpu_load")
  private String cpuLoad;

  private long timestamp;

  private Ram ram;

  @JSONField(name = "DiskInfo")
  private Disk disk;

  private Temp temp;

  public static class Ram {
    @JSONField(name = "buffercache")
    public String cache;
    public String free;
    public String total;
    public String used;
    @JSONField(name = "used_perc")
    public String usedPerc;
  }

  public static class Temp {
    public String gpu;
    public String cpu;
  }

  public static class Disk {
    @JSONField(name = "DiskUsed")
    public String used;
    @JSONField(name = "DiskUsedPerc")
    public String usedPerc;
    @JSONField(name = "DiskTotal")
    public String total;
  }

  public String getBsid() {
    return bsid;
  }

  public void setBsid(String bsid) {
    this.bsid = bsid;
  }

  public String getOsUpTime() {
    return osUpTime;
  }

  public void setOsUpTime(String osUpTime) {
    this.osUpTime = osUpTime;
  }

  public List<String> getFeatures() {
    return features;
  }

  public void setFeatures(List<String> features) {
    this.features = features;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getMac() {
    return mac;
  }

  public void setMac(String mac) {
    this.mac = mac;
  }

  public float getVersion() {
    return version;
  }

  public void setVersion(float version) {
    this.version = version;
  }

  public String getPyUpTime() {
    return pyUpTime;
  }

  public void setPyUpTime(String pyUpTime) {
    this.pyUpTime = pyUpTime;
  }

  public String getCpuLoad() {
    return cpuLoad;
  }

  public void setCpuLoad(String cpuLoad) {
    this.cpuLoad = cpuLoad;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public Ram getRam() {
    return ram;
  }

  public void setRam(Ram ram) {
    this.ram = ram;
  }

  public Disk getDisk() {
    return disk;
  }

  public void setDisk(Disk disk) {
    this.disk = disk;
  }

  public Temp getTemp() {
    return temp;
  }

  public void setTemp(Temp temp) {
    this.temp = temp;
  }

  public static void main(String[] args) {
   String s = "{\n" +
       "    \"os_uptime\":\"1745.9 min\",\n" +
       "    \"DiskInfo\":{\n" +
       "        \"DiskUsed\":\"3.7GB\",\n" +
       "        \"DiskUsedPerc\":\"51%\",\n" +
       "        \"DiskTotal\":\"7.2GB\"\n" +
       "    },\n" +
       "    \"bsid\":\"pinky\",\n" +
       "    \"features\":[\n" +
       "        \"camera\"\n" +
       "    ],\n" +
       "    \"temp\":{\n" +
       "        \"gpu\":\"52.6\",\n" +
       "        \"cpu\":\"52.6\"\n" +
       "    },\n" +
       "    \"ip\":\"127.0.0.1\",\n" +
       "    \"ram\":{\n" +
       "        \"buffcache\":\"608.0 MB\",\n" +
       "        \"total\":\"883.0 MB\",\n" +
       "        \"free\":\"44.0 MB\",\n" +
       "        \"used\":\"838.0 MB\",\n" +
       "        \"used_perc\":\"94.9 %\"\n" +
       "    },\n" +
       "    \"mac\":\"b8:27:eb:2e:72:1a\",\n" +
       "    \"version\":1.1,\n" +
       "    \"cpu_load\":\"0.3 %\",\n" +
       "    \"timestamp\":1482831620,\n" +
       "    \"script_uptime\":\"0.6 mins\"\n" +
       "}";

    System.out.println(JSON.toJSONString(JSON.parseObject(s, APStatus.class)));
  }

}
