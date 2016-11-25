package com.fintech.hospital.push.supplier.yunba;

import com.alibaba.fastjson.JSONObject;
import com.fintech.hospital.push.PushSupplier;
import com.fintech.hospital.push.yunba.YunbaIO;
import com.fintech.hospital.push.model.PushMsg;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author baoqiang
 */
abstract class YunbaSupplier extends YunbaIO implements PushSupplier {

  @Value("${push.qos}")
  private String pushQos;

  protected YunbaSupplier(String yunbaServerUrl, String yunbaAppKey) {
    super(yunbaServerUrl, yunbaAppKey);
  }

  public void publish(@NotNull PushMsg pushMsg) {
    JSONObject parameter = new JSONObject();
    parameter.put("msg", pushMsg.getMessage());
    /* add qos(according to MQTT protocol) */
    if (pushMsg.getOpts() != null) pushMsg.getOpts().qos(pushQos);
    parameter.put("opts", pushMsg.getOpts());
    switch (pushMsg.getType()) {
      case BROADCAST:
        parameter.put(pushMsg.getType().type(), pushMsg.getSubject());
        LOG.debug("yunba is broadcasting msg {} on topic {}", pushMsg.getMessage(), pushMsg.getSubject());
        yunbaSocket.emit("publish2", parameter);
        break;
      case ALIAS:
        parameter.put(pushMsg.getType().type(), pushMsg.getSubject());
        LOG.debug("yunba is sending msg {} to {}", pushMsg.getMessage(), pushMsg.getSubject());
        yunbaSocket.emit("publish2_to_alias", parameter);
        break;
      default:
        throw new UnsupportedOperationException("yunba does not support this:" + pushMsg.getType());
    }
  }

  public List<String> publishBatch(List<String> targets, PushMsg pushMsg) {
    List<String> pushedTargets = new CopyOnWriteArrayList<>();
    JSONObject parameter = new JSONObject();
    parameter.put("msg", pushMsg.getMessage());
        /* add qos(according to MQTT protocol) */
    if (pushMsg.getOpts() != null) pushMsg.getOpts().qos(pushQos);
    parameter.put("opts", pushMsg.getOpts());
    switch (pushMsg.getType()) {
      case BROADCAST:
        LOG.debug("yunba is broadcasting msg {} on topics {}", pushMsg.getMessage(), targets);
        targets.stream().forEach(t -> CompletableFuture.runAsync(
            () -> {
              parameter.put(pushMsg.getType().type(), t);
              yunbaSocket.emit("publish2", parameter);
              pushedTargets.add(t);
            }
        ));
        break;
      case ALIAS:
        LOG.debug("yunba is sending msg {} to {}", pushMsg.getMessage(), targets);
        targets.stream().forEach(t -> CompletableFuture.runAsync(() -> {
          parameter.put(pushMsg.getType().type(), t);
          yunbaSocket.emit("publish2_to_alias", parameter);
          pushedTargets.add(t);
        }));
        break;
      default:
        throw new UnsupportedOperationException("yunba does not support this:" + pushMsg.getType());
    }
    return pushedTargets;
  }

}
