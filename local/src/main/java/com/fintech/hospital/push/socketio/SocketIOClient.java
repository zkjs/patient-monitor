package com.fintech.hospital.push.socketio;

import io.socket.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public abstract class SocketIOClient implements IOCallback {

  protected final Logger LOG;
  private final String current;

  protected Socket socket;

  protected SocketIOClient(String serverUri, String appKey) {
    LOG = LoggerFactory.getLogger(this.getClass());
    current = this.getClass().getSimpleName();
    this.serverUri = serverUri;
    this.appKey = appKey;
  }

  private String serverUri;
  private String appKey;

  @PostConstruct
  private void init() {
    try {
      socket = IO.socket(serverUri);
      socket.setCallback(this);

      socket = socket.connect();
      LOG.info("{} connecting server with key {}...", current, appKey);
    } catch (URISyntaxException e) {
      LOG.error("illegal {} url", current, e);
    }
    LOG.info("{} with key {} init done!", current, appKey);
  }

  private void auth() {
    socket.emit("role", appKey);
  }

  private void subEvents() {
    socket.on("ready", args -> {
      LOG.info("ready to send data");
      socket.emit("aprssi", "{id: 'test', data: 'omg'}");
    });
  }

  @Override
  public void onError(SocketIOException socketIOException) {
    LOG.warn("{} error occurred: ", current, socketIOException);
    LOG.info("{} re-connecting in 2 seconds", current);
    new Timer("ReconnectYunbaSocketIn2Seconds").schedule(
        new TimerTask() {
          @Override
          public void run() {
            LOG.info("{} re-instantiating for fresh connection", current);
            try {
              socket = IO.socket(serverUri);
              socket.setCallback(SocketIOClient.this);
            } catch (URISyntaxException e) {
              LOG.error("illegal {} url", current, e);
            }
          }
        }
        , 2000L);
  }

  @Override
  public void onDisconnect() {
    LOG.info("connection {} terminated.", current);
  }

  @Override
  public void onConnect() {
    LOG.info("connection {} established", current);
    auth();
    subEvents();
  }

  @Override
  public void on(String event, Ack ack, Object... args) {
    LOG.debug("{} got event {}", current, event);
  }

}
