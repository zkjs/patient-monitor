package com.fintech.hospital.push.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fintech.hospital.data.Cache;
import com.fintech.hospital.data.MongoDB;
import com.fintech.hospital.domain.*;
import com.fintech.hospital.push.PushConsumer;
import com.fintech.hospital.push.PushService;
import com.fintech.hospital.push.model.PushMsg;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.fintech.hospital.domain.TimedPosition.mean;
import static com.fintech.hospital.rssi.RssiMeasure.positionByTriangleGradient;
import static com.fintech.hospital.rssi.RssiMeasure.positionFromDistribution;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @author baoqiang
 */
@Service("PosConsumer")
@Scope(SCOPE_PROTOTYPE)
public class PositionConsumer implements PushConsumer {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private PushService pushService;

  @Autowired
  private MongoDB mongo;

  @Autowired
  private Cache cache;

  @Value("${distance.coords.euclidean}")
  private boolean USE_EUCLIDEAN;

  @Value("${measure.method.triangle}")
  private boolean USE_TRIANGLE;

  @Value("${socket.io.app.event.position}")
  private String posChannel4App;

  @Override
  public void consume(String msg) {
    LOG.debug("consuming ap msg... {}", msg);

    try {
      APMsg apMsg = JSON.parseObject(msg, APMsg.class);

      final Bracelet bracelet = mongo.getBracelet(apMsg.getBandId());
      final String braceletId = bracelet.getId().toHexString();

      /* where the ap is located */
      AP ap = mongo.getAP(apMsg.getApid());
      /* who is using the bracelet */
      List<TimedPosition> positions = recordTrace(ap, apMsg.getRssi(), braceletId);
      /* based on ap signals, try to locate the patient */
      TimedPosition pos = position(positions, braceletId);

      //TODO push the position
      if (pos != null) {
        JSONObject positionMsg = new JSONObject();
        positionMsg.put("id", pos.getId());
        positionMsg.put("position", pos);
        pushService.notifyPosition(new PushMsg(posChannel4App, positionMsg.toJSONString()));
      }

    } catch (Exception e) {
      LOG.error("while consuming {} :", msg, e);
    }
  }

  private TimedPosition position(List<TimedPosition> positions, String braceletId) {
    /* cache bandid, lnglatDistance and ap lnglat to list */
    if (positions == null || positions.isEmpty()) return null;
    LOG.debug("positioning bracelet {}", braceletId);
    TimedPosition braceletPosition;
    BraceletPosition lastPos = mongo.getBraecletLastPos(braceletId);
    switch (positions.size()) {
      case 1:
        braceletPosition = positions.get(0);
        break;
      case 2:
        TimedPosition pos0 = positions.get(0),
            pos1 = positions.get(1);
        double distRatio0 = pos0.getRadius() / (pos0.getRadius() + pos1.getRadius());
        braceletPosition = mean(positions, new double[]{1 - distRatio0, distRatio0});
        break;
      default:
        List<AP> apList = mongo.getAPByNames(mongo.tracedAP(braceletId));
        LOG.debug("positions: {}", positions);
        LOG.debug("aps: {}", apList);
        braceletPosition =
            USE_TRIANGLE ?
//                  positioning(positions, braceletId, null, USE_EUCLIDEAN) :
                positionByTriangleGradient(positions, apList) :
                positionFromDistribution(positions, mongo.getAPByNames(mongo.tracedAP(braceletId)));
        break;
    }
    if (lastPos != null) braceletPosition =
        mean(Lists.newArrayList(braceletPosition,
            lastPos.getPosition().get(lastPos.getPosition().size() - 1)),
            new double[]{0.7, 0.3}
        );
    mongo.addBraceletPosition(braceletId, braceletPosition);
    LOG.info("BASED ON {}, GOT NEW POS {} 4-bracelet {} ", positions.size(), braceletPosition, braceletId);
    braceletPosition.setId(braceletId);
    return braceletPosition;
  }

  private List<TimedPosition> recordTrace(AP ap, int rssi, String braceletId) {
    runAsync(() -> mongo.addBraceletTrace(
        braceletId,
        new BraceletTrace(ap.getId().toHexString(), rssi, ap.getGps())
    ));
      /* pop all latest positions */
    return cache.push(braceletId, ap.getAlias(), new TimedPosition(ap, System.currentTimeMillis(), rssi));
  }

}
