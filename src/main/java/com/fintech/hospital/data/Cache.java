package com.fintech.hospital.data;

import com.fintech.hospital.domain.TimedPosition;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author baoqiang
 */
@Component
public class Cache {

  public List<TimedPosition> push(String bracelet, String apid, TimedPosition pos){
    BraceletPosCache c = CACHE.get(bracelet);
    synchronized (CACHE) {
      if (c == null) {
        ConcurrentHashMap<String, TimedPosition> m = new ConcurrentHashMap<>();
        m.put(apid, pos);
        c = new BraceletPosCache(pos.getTimestamp(), m);
        CACHE.put(bracelet, c);
        return null;
      }
    }
    c.data.put(apid, pos);
    if (c.data.size() > 4 || c.expired()) {
      c = CACHE.remove(bracelet);
      return new ArrayList<>(c.data.values());
    }
    return null;
  }

  private static final ConcurrentHashMap<String, BraceletPosCache> CACHE = new ConcurrentHashMap<>();

  private static class BraceletPosCache {
    ConcurrentHashMap<String, TimedPosition> data = new ConcurrentHashMap<>(5);
    long timestamp;

    boolean expired() {
      return (System.currentTimeMillis() - this.timestamp) / 1000 > 5;
    }

    BraceletPosCache(long timestamp, ConcurrentHashMap<String, TimedPosition> data) {
      this.timestamp = timestamp;
      this.data.putAll(data);
    }

  }

}
