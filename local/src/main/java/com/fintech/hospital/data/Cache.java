package com.fintech.hospital.data;

import com.fintech.hospital.push.PushService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

  @Scheduled(fixedRate = 1200)
  public void checkShot() {
    LOG.debug("checking camera commands cache :)");
    if (!CACHE.isEmpty()) {
      CACHE.entrySet().forEach(c -> {
        String ap = c.getKey();
        APCameraCache cameraCache = c.getValue();
        if (cameraCache.data.size() >= 4 || expired(cameraCache.timestamp)) {
          LOG.info("let AP {} take a photo for {} ", ap, c.getValue().data);
          pushService.shot(ap, StringUtils.join(c.getValue().data, ","));
          CACHE.remove(ap);
        }
      });
    }
  }

  public void push(String apid, String bracelet, long time) {
    APCameraCache c = new APCameraCache(time, bracelet);
    c = CACHE.putIfAbsent(apid, c);
    if (c != null) {
      c.data.add(bracelet);
    }
  }

  private boolean expired(long timestamp) {
    return System.currentTimeMillis() - timestamp > PERIOD;
  }

  private static final ConcurrentHashMap<String, APCameraCache> CACHE = new ConcurrentHashMap<>();

  private static class APCameraCache {
    Set<String> data;
    long timestamp;

    APCameraCache(long timestamp, String bracelet) {
      this.timestamp = timestamp;
      this.data = new HashSet<>();
      this.data.add(bracelet);
    }

  }

}
