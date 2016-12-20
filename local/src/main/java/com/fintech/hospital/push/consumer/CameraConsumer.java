package com.fintech.hospital.push.consumer;

import com.alibaba.fastjson.JSON;
import com.fintech.hospital.data.MongoDB;
import com.fintech.hospital.domain.*;
import com.fintech.hospital.push.PushConsumer;
import com.fintech.hospital.push.PushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
  private PushService pushService;

  @Autowired
  private MongoDB mongo;

  @Value("${distance.range.shot}")
  private Double SHOT_RANGE;

  @Override
  public void consume(String msg) {
    APMsg apMsg = JSON.parseObject(msg, APMsg.class);

    /* who is using the bracelet */
    final Bracelet bracelet = mongo.getBracelet(apMsg.getBandId());
    final String braceletId = bracelet.getId().toHexString();

    /* where the ap is located */
    AP ap = mongo.getAP(apMsg.getApid());

    TimedPosition position = recordTrace(ap, apMsg.getRssi(), braceletId);

    /* based on ap signals, try to locate and take a photo of the patient */

    if (ap.shotEnabled() && SHOT_RANGE >= position.getRadius()) {
      LOG.info("let AP {} take a photo for {} ", ap.getAlias(), apMsg.getBandId());
      pushService.shot(ap.getAlias(), braceletId);
    }

  }

  private TimedPosition recordTrace(AP ap, int rssi, String braceletId) {
    runAsync(() -> mongo.addBraceletTrace(
        braceletId,
        new BraceletTrace(ap.getAlias(), rssi)
    ));
      /* pop all latest positions */
    return new TimedPosition(ap, System.currentTimeMillis(), rssi);
  }

}
