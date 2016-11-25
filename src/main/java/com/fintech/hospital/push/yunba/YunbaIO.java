package com.fintech.hospital.push.yunba;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * <b>DO NOT MODIFY THIS FILE: this is a direct copy from URSAMAJOR, refactor needed</b>
 */
public abstract class YunbaIO implements IOCallback {

  protected final Logger LOG;
  protected final String current;

  protected SocketIO yunbaSocket;

  protected YunbaIO(String yunbaServerUrl, String yunbaAppKey) {
    LOG = LoggerFactory.getLogger(this.getClass());
    current = this.getClass().getSimpleName();
    this.yunbaServerUrl = yunbaServerUrl;
    this.yunbaAppKey = yunbaAppKey;
  }

  private String yunbaServerUrl;
  private String yunbaAppKey;

  @PostConstruct
  private void init() {
    LOG.info("init {} socket with yunba key {}...", current, yunbaAppKey);
    this.yunbaSocket = new SocketIO();
    try {
      yunbaSocket.connect(yunbaServerUrl, this);
      LOG.info("{} connecting server with yunba key {}...", current, yunbaAppKey);
    } catch (MalformedURLException e) {
      LOG.error("illegal {} url", current, e);
    }
    LOG.info("{} with yunba key {} init done!", current, yunbaAppKey);
  }

  @Override
  public void onMessage(String data, IOAcknowledge ack) {
    LOG.info("{} server said : {}", current, data);
  }

  @Override
  public void onMessage(JSONObject jsonObject, IOAcknowledge ioAcknowledge) {
    LOG.info("{} server said json: {}", current, jsonObject);
  }

  @Override
  public void onError(SocketIOException socketIOException) {
    LOG.warn("{} error occurred: {}", current, socketIOException);
    IOCallback yunbaSocketCallback = this;
    LOG.info("{} re-connecting in 2 seconds", current);
    new Timer("ReconnectYunbaSocketIn2Seconds").schedule(
        new TimerTask() {
          @Override
          public void run() {
            LOG.info("{} re-instantiating for fresh connection", current);
            yunbaSocket = new SocketIO();
            try {
              yunbaSocket.connect(yunbaServerUrl, yunbaSocketCallback);
            } catch (MalformedURLException e) {
              LOG.error("illegal {} url", current, e);
            }
          }
        }
        , 2000L);
  }

  protected void subscribe(String topic) {
    yunbaSocket.emit("subscribe", JSON.parseObject(String.format("{'topic': '%s'}", topic)));
  }

  protected void alias(String alias) {
    yunbaSocket.emit("set_alias", JSON.parseObject(String.format("{'alias': '%s'}", alias)));
  }

  private void connect2Yunba() {
    String customid = UUID.randomUUID().toString();
    /* emit connect */
    JSONObject connJson = new JSONObject();
    connJson.put("appkey", yunbaAppKey);
    connJson.put("customid", customid);
    yunbaSocket.emit("connect", connJson);
    LOG.info("{} connected server, yunba authenticating ...", current);
  }

  @Override
  public void onDisconnect() {
    LOG.info("connection {} terminated.", current);
  }

  @Override
  public void onConnect() {
    LOG.info("connection {} established", current);
  }

  @Override
  public void on(String event, IOAcknowledge ack, Object... args) {
    LOG.debug("{} got event {}", current, event);
    try {
      switch (event) {
        case "socketconnectack":
          onSocketConnectAck();
          break;
        case "connack":
          onConnAck(args[0]);
          break;
        case "puback":
          onPubAck(args[0]);
          break;
        case "set_alias_ack":
          onAliasAck(args[0]);
          break;
        case "message":
          onMessage((JSONObject) args[0], ack);
          break;
        default:
          LOG.warn("{} {} cannot be handled", current, event);
          break;
      }
    } catch (Exception e) {
      LOG.error("error on yunba {} event {}: {}", current, event, e);
    }
  }

  protected void onSocketConnectAck() throws Exception {
    LOG.info("{} onSocketConnectAck", current);
    connect2Yunba();
  }

  protected void onConnAck(Object json) throws Exception {
    LOG.info("{} connected: {}", current, json);
  }

  protected void onPubAck(Object json) throws Exception {
    LOG.info("{} published: {}", current, json);
  }

  public void onAliasAck(Object json) throws Exception {
    LOG.info("{} alias set: {}", current, json);
  }

}
