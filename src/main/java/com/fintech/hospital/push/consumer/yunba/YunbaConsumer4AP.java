package com.fintech.hospital.push.consumer.yunba;

import com.alibaba.fastjson.JSON;
import com.fintech.hospital.data.Cache;
import com.fintech.hospital.data.MongoDB;
import com.fintech.hospital.domain.APMsg;
import com.fintech.hospital.domain.BraceletTrace;
import com.fintech.hospital.domain.TimedPosition;
import com.fintech.hospital.push.PushService;
import com.fintech.hospital.push.model.PushMsg;
import com.fintech.hospital.push.supplier.yunba.YunbaOpts;
import com.fintech.hospital.rssi.RssiDistanceModel;
import com.fintech.hospital.rssi.RssiMeasure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import static com.fintech.hospital.domain.TimedPosition.mean;
import static com.fintech.hospital.push.model.PushType.BROADCAST;
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

  final RssiDistanceModel RSSI_MODEL = new RssiDistanceModel(0.1820634, 0.8229884, 6.6525179, -75);

  @Value("${yunba.rescue.topic}")
  private String RESCUE_TOPIC;

  @Override
  public void consume(String msg) {
    LOG.info("consuming ap msg... {}", msg);
    APMsg apMsg = JSON.parseObject(msg, APMsg.class);


    final long current = System.currentTimeMillis();
    final String bracelet = mongo.getBracelet(apMsg.bracelet()).getId().toHexString();

    /* query ap */
    supplyAsync(() -> mongo.getAP(apMsg.getApid())
    ).thenCompose(ap -> {
      if (ap == null) throw new IllegalArgumentException("ap not exists " + apMsg.getApid());

      /* categorize msg type: urgency (push to mon immediately for alert), tracing */
      if (apMsg.urgent()) {
        LOG.info("band {} in emergency, detected by ap {}", apMsg.bracelet(), apMsg.getApid());
        apMsg.fillAP(ap);
        String broadcast = JSON.toJSONString(apMsg);
        pushService.push2Mon(new PushMsg(BROADCAST, RESCUE_TOPIC, broadcast, new YunbaOpts(new YunbaOpts.YunbaAps(
            broadcast, String.format("(%s)%s 位置有人呼救", apMsg.getApid(), ap.getAddress())
        ))));
      }

      runAsync(() -> mongo.addBraceletTrace(
          apMsg.bracelet(),
          new BraceletTrace(apMsg.getApid(), apMsg.getRssi(), ap.getGps())
      ));
      /* pop all latest positions */
      return supplyAsync(() ->
          cache.push(bracelet, ap.getAlias(), new TimedPosition(ap, current), RSSI_MODEL.distance(apMsg.getRssi()))
      );
    }).thenAccept(positions -> {
      /* cache bandid, lnglatDistance and ap lnglat to list */
      if (positions == null || positions.isEmpty()) return;
      LOG.info("positioning bracelet {}", bracelet);
      TimedPosition braceletPosition = null;
      switch (positions.size()) {
        case 1:
          braceletPosition = positions.get(0).getKey();
          break;
        case 2:
          double apDist0 = positions.get(0).getValue(),
              apDist1 = positions.get(1).getValue(),
              distRatio0 = apDist0 / (apDist0 + apDist1);
          braceletPosition = mean(new TimedPosition[]{positions.get(0).getKey(), positions.get(1).getKey()},
              new double[]{distRatio0, 1 - distRatio0});
          break;
        default:
          braceletPosition = RssiMeasure.positioning(positions, bracelet);
          break;
      }
      mongo.addBraceletPosition(bracelet, braceletPosition);
      LOG.info("new position {} for bracelet {} ", braceletPosition, bracelet);
    }).exceptionally(t -> {
      LOG.error("bracelet " + bracelet + " err...: ", t);
      return null;
    });

  }

  @Override
  public void onConnAck(Object json) throws Exception {
    LOG.info("yunba for ap {} connected {}", current, json);
    alias("alias2");
  }

}
