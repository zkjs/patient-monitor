package com.fintech.hospital.push;

import com.fintech.hospital.push.model.PushMsg;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

/**
 * @author baoqiang
 */
@Service
@Scope(SCOPE_SINGLETON)
public class PushService {

  static final Logger LOG = LoggerFactory.getLogger(PushService.class);

  @Autowired
  @Qualifier("yunbaSupplier4Mon")
  private PushSupplier pushSupplier4Mon;

  @Autowired
  @Qualifier("yunbaRestSupplier4Mon")
  private PushSupplier pushRestSupplier4Mon;

  @Autowired
  @Qualifier("yunbaSupplier4AP")
  private PushSupplier pushSupplier4AP;

  @Autowired
  @Qualifier("yunbaRestSupplier4AP")
  private PushSupplier pushRestSupplier4AP;

  private ThreadLocal<Boolean> tlUsingRest = new ThreadLocal<>();

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

  public List<String> newPushTaskBatch(List<String> targets, PushMsg msg, boolean usingRest) {
    LOG.info("pushing batch msg {}", msg);
    final List<String> successfulPushes = new ArrayList<>();
    successfulPushes.addAll(
        (usingRest ? pushRestSupplier4AP : pushSupplier4AP)
            .publishBatch(targets, msg)
    );
    if (successfulPushes.isEmpty() && tlUsingRest.get() == null) {
      LOG.warn("previous method (using rest: {}) failed, try another", usingRest);
      tlUsingRest.set(usingRest);
      newPushTaskBatch(targets, msg, !usingRest);
      tlUsingRest.set(null);
    }
    LOG.trace("push done for {}", successfulPushes);
    return successfulPushes;
  }

  @Value("${push.apns.sound}")
  private String apnsSound;

  @Value("${push.apns.badge}")
  private String apnsBadge;

  @Autowired
  private Environment environment;

  /**
   * set current active profile for validator sdk
   */
  @PostConstruct
  private void setProps() {
    System.setProperty("profile.active", StringUtils.join(environment.getActiveProfiles(), ','));
    LOG.info("system active profile set to {}", System.getProperty("profile.active"));
  }

}
