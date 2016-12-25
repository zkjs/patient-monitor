package com.fintech.hospital.data;

import com.fintech.hospital.domain.AP;
import com.fintech.hospital.push.PushService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author baoqiang
 */
@Component
public class Cache {
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Value("${cache.camera.expire}")
  private long PERIOD;

  @Autowired
  private PushService pushService;

  @Autowired
  private MongoDB mongo;

  private static final ConcurrentHashMap<String, Set<AP>> TRIGGERS = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Pair<Long, Set<String>>> AP_STANDBYS = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, APCameraCache> CACHE = new ConcurrentHashMap<>();

  /**
   * init ap cameras
   */
  @PostConstruct
  public void loadCameras() {
    List<AP> cameras = mongo.getAPCameras();
    LOG.debug("loaded ap cameras: {}", cameras);
    buildCacheIndex(cameras);
  }

  private void buildCacheIndex(List<AP> cameras) {
    cameras.stream().filter(
        c -> c.getTriggers() != null && !c.getTriggers().isEmpty()
    ).forEach(c -> {
      c.getTriggers().forEach(t -> {
        Set<AP> aps = new HashSet<>(1);
        aps.add(c);
        aps = TRIGGERS.putIfAbsent(t, aps);
        if (aps != null) TRIGGERS.get(t).add(c);
      });
    });
  }

  /**
   * reload ap cameras every 3 minutes
   */
  @Scheduled(fixedRate = 60000 * 3)
  public void refreshCameras() {
    List<AP> cameras = mongo.getAPCameras();
    LOG.debug("reloaded ap cameras: {}", cameras);
    buildCacheIndex(cameras);
    long currentTime = System.currentTimeMillis();
    AtomicInteger count = new AtomicInteger();
    AP_STANDBYS.forEach((k, v) -> {
      /* clear expired keys */
      if (currentTime - v.getKey() > 2 * PERIOD) {
        AP_STANDBYS.remove(k);
        count.incrementAndGet();
      }
    });
    LOG.info("cleared {} expired standbys (half triggered aps)", count.intValue());
  }

  @Scheduled(fixedRate = 1200)
  public void checkShot() {
    LOG.debug("checking camera commands cache :)");
    if (!CACHE.isEmpty()) {
      CACHE.entrySet().forEach(c -> {
        String ap = c.getKey();
        APCameraCache cameraCache = c.getValue();
        if (expired(cameraCache.timestamp) || cameraCache.bracelets.size() >= 4) {
          LOG.info("let AP {} take a photo for {} ", ap, c.getValue().bracelets);
          pushService.shot(ap, StringUtils.join(c.getValue().bracelets, ","));
          CACHE.remove(ap);
        }
      });
    }
  }

  public void push(String apAlias, String bracelet, long time) {
    if (TRIGGERS.containsKey(apAlias)) {
      /* current ap is a trigger */
      Set<AP> targetAPs = TRIGGERS.get(apAlias);
      targetAPs.forEach(targetAP -> {
        final String logic = targetAP.getTriggerLogic();
        if ("OR".equalsIgnoreCase(logic)) {

          /* or logic triggers a camera shot instantly */
          updateCache(apAlias, bracelet, time);

        } else if ("AND".equalsIgnoreCase(logic)) {

          /* and logic waits for all triggers to be pulled before firing a shot */
          final String standbyKey = apAlias + "-" + bracelet;
          AP_STANDBYS.computeIfPresent(standbyKey, (k, v) -> {
            if (expired(v.getKey())) {
              /* remove expired standbys */
              AP_STANDBYS.remove(standbyKey);
              return null;
            } else {
              v.getValue().remove(apAlias);
              return v;
            }
          });
          Pair<Long, Set<String>> standByTriggers = AP_STANDBYS.computeIfAbsent(standbyKey,
              alias -> {
                Set<String> triggers = new HashSet<>();
                triggers.addAll(targetAP.getTriggers());
                triggers.remove(apAlias);
                return new Pair<>(time, triggers);
              }
          );
          if (standByTriggers != null && standByTriggers.getValue().isEmpty()) {
            updateCache(apAlias, bracelet, time);
          }

        }
      });
    }
  }

  private void updateCache(String ap, String bracelet, long time) {
    /* update camera shot cache */
    APCameraCache c = new APCameraCache(time, bracelet);
    c = CACHE.putIfAbsent(ap, c);
    if (c != null) c.bracelets.add(bracelet);
  }

  private boolean expired(long timestamp) {
    return System.currentTimeMillis() - timestamp > PERIOD;
  }

  private static class APCameraCache {
    Set<String> bracelets;
    long timestamp;

    APCameraCache(long timestamp, String bracelet) {
      this.timestamp = timestamp;
      this.bracelets = new HashSet<>();
      this.bracelets.add(bracelet);
    }

  }

}
