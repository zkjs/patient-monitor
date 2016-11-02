package com.fintech.hospital.push;

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
    LOG.info("init yunba {} socket with key {}...", current, yunbaAppKey);
    this.yunbaSocket = new SocketIO();
    try {
      yunbaSocket.connect(yunbaServerUrl, this);
      LOG.info("yunba {} socket {} connected", current, yunbaAppKey);
    } catch (MalformedURLException e) {
      LOG.error("illegal yunba {} url", current, e);
    }
    LOG.info("yunba {} {} init done!", current, yunbaAppKey);
  }

  @Override
  public void onMessage(String data, IOAcknowledge ack) {
    LOG.info("server said : {}", data);
  }

  @Override
  public void onMessage(JSONObject jsonObject, IOAcknowledge ioAcknowledge) {
    LOG.info("server said json: {}", jsonObject);
  }

  @Override
  public void onError(SocketIOException socketIOException) {
    LOG.warn("an {} error occurred", current, socketIOException);
    IOCallback yunbaSocketCallback = this;
    LOG.info("re-connecting {} in 2 seconds", current);
    new Timer("ReconnectYunbaSocketIn2Seconds").schedule(
        new TimerTask() {
          @Override
          public void run() {
            LOG.info("re-instantiating yunba {} socket for fresh connection", current);
            yunbaSocket = new SocketIO();
            try {
              yunbaSocket.connect(yunbaServerUrl, yunbaSocketCallback);
            } catch (MalformedURLException e) {
              LOG.error("illegal yunba {} url", current, e);
            }
          }
        }
        , 2000L);
  }

  private void connect2Yunba() {
    LOG.info("server connected, connecting to yunba for {}...", current);
    String customid = UUID.randomUUID().toString();
        /* emit connect */
    JSONObject connJson = new JSONObject();
    connJson.put("appkey", yunbaAppKey);
    connJson.put("customid", customid);
    yunbaSocket.emit("connect", connJson);
    LOG.info("yunba {} socket connected", current);
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
    LOG.info("server {} triggered event {}", current, event);
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
        default:
          LOG.warn("{} {} cannot be handled", current, event);
          break;
      }
    } catch (Exception e) {
      LOG.error("error on yunba {} event {}: {}", current, event, e);
    }
  }

  public void onSocketConnectAck() throws Exception {
    LOG.info("{} onSocketConnectAck", current);
    connect2Yunba();
  }

  public void onConnAck(Object json) throws Exception {
    LOG.info("{} onConnAck success {}", current, json);
  }

  public void onPubAck(Object json) throws Exception {
    LOG.info("{} conPubAck success {}", current, json);
  }
}
