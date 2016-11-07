package com.fintech.hospital.data;

import com.fintech.hospital.domain.TimedPosition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author baoqiang
 */
@Component
public class Cache {

  @Value("${cache.position.expire}")
  private long PERIOD;

  public List<TimedPosition> push(String bracelet, String apid, TimedPosition pos) {
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
    TimedPosition previousPos = c.data.get(apid);
    if(previousPos!=null){
      pos.setRssi(previousPos.getRssi() - 0.8 * (previousPos.getRssi() -pos.getRssi()));
    }
    c.data.put(apid, pos);

    if (c.data.size() >= 3 || expired(c.timestamp)) {
      c = CACHE.remove(bracelet);
      return new ArrayList<>(c.data.values());
    }
    return null;
  }

  private boolean expired(long timestamp) {
    return System.currentTimeMillis() - timestamp > PERIOD;
  }

  private static final ConcurrentHashMap<String, BraceletPosCache> CACHE = new ConcurrentHashMap<>();

  private static class BraceletPosCache {
    ConcurrentHashMap<String, TimedPosition> data = new ConcurrentHashMap<>(5);
    long timestamp;


    BraceletPosCache(long timestamp, ConcurrentHashMap<String, TimedPosition> data) {
      this.timestamp = timestamp;
      this.data.putAll(data);
    }

  }

}
