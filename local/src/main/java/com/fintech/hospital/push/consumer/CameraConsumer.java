package com.fintech.hospital.push.consumer;

import com.alibaba.fastjson.JSON;
import com.fintech.hospital.data.Cache;
import com.fintech.hospital.data.MongoDB;
import com.fintech.hospital.domain.AP;
import com.fintech.hospital.domain.Bracelet;
import com.fintech.hospital.domain.BraceletTrace;
import com.fintech.hospital.push.PushConsumer;
import com.fintech.hospital.push.model.APMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.fintech.hospital.domain.TimedPosition.RSSI_MODEL;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @author baoqiang
 */
@Component("camConsumer")
@Scope(SCOPE_PROTOTYPE)
public class CameraConsumer implements PushConsumer {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private MongoDB mongo;

  @Autowired
  private Cache cache;

  @Value("${distance.range.shot}")
  private Double SHOT_RANGE;

  @Override
  public void consume(String msg) {
    APMsg apMsg = JSON.parseObject(msg, APMsg.class);

    /* who is using the bracelet */
    final Bracelet bracelet = mongo.getBracelet(apMsg.getBandId());

    if (bracelet == null) {
      LOG.warn("bracelet {} not found", apMsg.getBandId());
      return;
    }

    final String braceletId = bracelet.getId().toHexString();

    /* where the ap is located */
    AP ap = mongo.getAP(apMsg.getApid());

    if (ap == null) {
      LOG.warn("ap {} not found", apMsg.getApid());
      return;
    }

    double distance = recordTrace(ap, apMsg.getRssi(), braceletId);

    if (ap.shotEnabled() || SHOT_RANGE >= distance) {
      LOG.debug("{} distance: {}, ready to roll", apMsg.getBandId(), distance);
      /* based on ap signals, try to locate and take a photo of the patient */
      cache.push(apMsg.getApid(), braceletId, System.currentTimeMillis());
    }

  }

  private double recordTrace(AP ap, int rssi, String braceletId) {
    runAsync(() -> mongo.addBraceletTrace(
        braceletId,
        new BraceletTrace(ap.getAlias(), rssi)
    ));
      /* pop all latest positions */
    return RSSI_MODEL.distance(rssi);
  }

}
