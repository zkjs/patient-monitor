package com.fintech.hospital.push;

import com.fintech.hospital.push.model.PushMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

/**
 * @author baoqiang
 */
@Service
@Scope(SCOPE_SINGLETON)
public class PushService {

  private final Logger LOG = LoggerFactory.getLogger(PushService.class);

  @Autowired
  @Qualifier("mqttMonSupplier")
  private PushSupplier pushSupplier4Mon;

//  @Autowired
//  @Qualifier("yunbaSupplier4AP")
  private PushSupplier pushSupplier4AP;

  public void push2Mon(PushMsg msg) {
    LOG.info("pushing msg {}", msg);
    CompletableFuture.runAsync(() -> {
      pushSupplier4Mon.publish(msg);
    }).exceptionally(t -> {
      LOG.error("failed to push msg {}: {}", msg, t);
      return null;
    });
  }

  public void push2AP(PushMsg msg) {
    LOG.info("pushing response {}", msg);
    CompletableFuture.runAsync(() -> {
      pushSupplier4AP.publish(msg);
    }).exceptionally(t -> {
      LOG.error("failed to push msg {}: {}", msg, t);
      return null;
    });
  }

}
