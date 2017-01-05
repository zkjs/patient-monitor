package com.fintech.hospital.push;

import com.alibaba.fastjson.JSONObject;
import com.fintech.hospital.push.model.PushMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.util.concurrent.CompletableFuture.runAsync;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

/**
 * @author baoqiang
 */
@Service
@Scope(SCOPE_SINGLETON)
public class PushService {

  private final Logger LOG = LoggerFactory.getLogger(PushService.class);

  @Value("${mqtt.topic.ap}")
  private String AP;

  @Value("${ap.cmd.camera}")
  private String CMD_CAMERA;

  @Value("${server.addr}")
  private String SERVER_ADDR;

  @Value("${server.port}")
  private String SERVER_PORT;

  @Autowired
  @Qualifier("camConsumer")
  private PushConsumer positionConsumer;

  @Autowired
  @Qualifier("supplierMqtt")
  private PushSupplier pushSupplier4AP;

  public void relay(String msg) {
    LOG.info("pushing msg {}", msg);
    runAsync(
        () -> positionConsumer.consume(msg)
    ).exceptionally(t -> {
      LOG.error("failed to relay position msg {}: {}", msg, t);
      return null;
    });
  }

  public void alert(PushMsg msg) {
    LOG.info("pushing response {}", msg);
    runAsync(
        () -> pushSupplier4AP.publish(msg)).exceptionally(t -> {
      LOG.error("failed to push msg {}: {}", msg, t);
      return null;
    });
  }

  public void shot(String shotAP, String bracelet) {
    JSONObject shotMsg = new JSONObject();
    shotMsg.put("ap", shotAP);
    shotMsg.put("bracelet", bracelet);
    shotMsg.put("cmd", CMD_CAMERA);
    shotMsg.put("url", String.format("http://%s:%s/photo/%s?bracelets=%s&time=",
        SERVER_ADDR, SERVER_PORT, shotAP, bracelet));
    pushSupplier4AP.publish(new PushMsg(AP, shotMsg.toJSONString()));
  }

}
