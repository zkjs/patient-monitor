package com.fintech.hospital.push.consumer.yunba;

import com.alibaba.fastjson.JSON;
import com.fintech.hospital.data.Cache;
import com.fintech.hospital.data.MongoDB;
import com.fintech.hospital.domain.*;
import com.fintech.hospital.push.PushService;
import com.fintech.hospital.push.model.PushMsg;
import com.fintech.hospital.push.supplier.yunba.YunbaOpts;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.fintech.hospital.domain.TimedPosition.mean;
import static com.fintech.hospital.push.model.PushType.BROADCAST;
import static com.fintech.hospital.rssi.RssiMeasure.positionByTriangleGradient;
import static com.fintech.hospital.rssi.RssiMeasure.positionFromDistribution;
import static com.fintech.hospital.rssi.RssiMeasure.positioning;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * @author baoqiang
 */
@Service("yunbaConsumer4AP")
@Scope(SCOPE_SINGLETON)
public class YunbaConsumer4AP extends YunbaConsumer {


  YunbaConsumer4AP(@Value("${yunba.server.url}") String yunbaServerUrl,
                   @Value("${yunba.appkey.ap}") String yunbaAppKey) {
    super(yunbaServerUrl, yunbaAppKey);
  }

  @Autowired
  private PushService pushService;

  @Autowired
  private MongoDB mongo;

  @Autowired
  private Cache cache;

  @Value("${yunba.rescue.topic}")
  private String RESCUE_TOPIC;

  @Value("${yunba.trace.alias}")
  private String TRACE_ALIAS;

  @Value("${distance.coords.euclidean}")
  private boolean USE_EUCLIDEAN;

  @Value("${measure.method.triangle}")
  private boolean USE_TRIANGLE;


  @Override
  public void consume(String msg) {
    LOG.info("consuming ap msg... {}", msg);
    APMsg apMsg = JSON.parseObject(msg, APMsg.class);

    final long current = System.currentTimeMillis();
    final Bracelet bracelet = mongo.getBracelet(apMsg.braceletBleId());
    final String braceletId = bracelet.getId().toHexString();

    /* query ap */
    supplyAsync(() -> mongo.getAP(apMsg.getApid())
    ).thenCompose(ap -> {
      if (ap == null) throw new IllegalArgumentException("ap not exists " + apMsg.getApid());

      /* categorize msg type: urgency (push to mon immediately for alert), tracing */
      if (apMsg.urgent()) {
        LOG.info("bracelet {}(BLE-ID) in emergency, detected by ap {}", apMsg.braceletBleId(), apMsg.getApid());
        apMsg.fillAP(ap);
        List<TimedPosition> positionList = mongo.getBraceletTrack(braceletId).getPosition();
        apMsg.setPosition(positionList.get(positionList.size() - 1));
        apMsg.setBracelet(braceletId);
        String alertMsg = String.format("%s (%s) 求救 ", bracelet.getPatientName(), apMsg.braceletBleId());
        apMsg.setMessage(alertMsg);
        String broadcast = JSON.toJSONString(apMsg);
        pushService.push2Mon(new PushMsg(BROADCAST, RESCUE_TOPIC, broadcast, new YunbaOpts(new YunbaOpts.YunbaAps(
            broadcast, alertMsg
        ))));
      }

      runAsync(() -> mongo.addBraceletTrace(
          braceletId,
          new BraceletTrace(apMsg.getApid(), apMsg.getRssi(), ap.getGps())
      ));
      /* pop all latest positions */
      return supplyAsync(() ->
          cache.push(braceletId, ap.getAlias(), new TimedPosition(ap, current, apMsg.getRssi()))
      );
    }).thenAccept(positions -> {
      /* cache bandid, lnglatDistance and ap lnglat to list */
      if (positions == null || positions.isEmpty()) return;
      LOG.debug("positioning bracelet {}", bracelet);
      TimedPosition braceletPosition = null;
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
              USE_TRIANGLE?
//                  positioning(positions, braceletId, null, USE_EUCLIDEAN) :
              positionByTriangleGradient(positions, apList) :
              positionFromDistribution(positions, mongo.getAPByNames(mongo.tracedAP(braceletId)));
          break;
      }
      if(lastPos!=null) braceletPosition =
          TimedPosition.mean(Lists.newArrayList(braceletPosition,
              lastPos.getPosition().get(lastPos.getPosition().size()-1)),
              new double[]{0.7, 0.3}
          );
      mongo.addBraceletPosition(braceletId, braceletPosition);
      LOG.info("BASED ON {}, GOT NEW POS {} 4-bracelet {} ", positions.size(), braceletPosition, bracelet);
    }).exceptionally(t -> {
      LOG.error("bracelet " + bracelet + " err...: ", t);
      return null;
    });

  }

  @Override
  public void onConnAck(Object json) throws Exception {
    LOG.info("yunba for ap {} connected {}", current, json);
    alias(TRACE_ALIAS);
  }

}
