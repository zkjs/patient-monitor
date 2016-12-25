package com.fintech.hospital.push.consumer;

import com.alibaba.fastjson.JSON;
import com.fintech.hospital.data.MongoDB;
import com.fintech.hospital.domain.AP;
import com.fintech.hospital.push.model.APMsg;
import com.fintech.hospital.domain.Bracelet;
import com.fintech.hospital.push.PushConsumer;
import com.fintech.hospital.push.PushService;
import com.fintech.hospital.push.model.PushMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import static com.fintech.hospital.push.model.PushType.BROADCAST;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @author baoqiang
 */
@Service("APConsumer")
@Scope(SCOPE_PROTOTYPE)
public class APConsumer implements PushConsumer {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private PushService pushService;

  @Autowired
  private MongoDB mongo;

  @Value("${mqtt.topic.rescue.app}")
  private String RESCUE_TOPIC;

  @Override
  public void consume(String msg) {
    LOG.debug("consuming ap msg... {}", msg);

    try {
      APMsg apMsg = JSON.parseObject(msg, APMsg.class);

      final Bracelet bracelet = mongo.getBracelet(apMsg.getBandId());
      if (bracelet == null) {
        LOG.warn("{} not registered yet", apMsg.getBandId());
        //TODO add new bracelet
        return;
      }
      final String braceletId = bracelet.getId().toHexString();

      if (apMsg.dropped()) {
        /* dropped */
        mongo.braceletDropped(bracelet);
      } else if (apMsg.urgent()) {
        /* emergency */
        AP ap = mongo.getAP(apMsg.getApAlias());
        if (ap == null) {
          LOG.warn("ap {} not found in db, check ap local configuration", apMsg.getApAlias());
          return;
        }
        apMsg.fillAP(ap);
        notifyEmergency(apMsg, braceletId, bracelet.getPatientName());
      } else {
        pushService.relay(JSON.toJSONString(apMsg));
      }
    } catch (Exception e) {
      LOG.error("while consuming {} : {}", msg, e);
    }
  }

  private void notifyEmergency(APMsg apMsg, String braceletId, String patient) {
    /* categorize msg type: urgency (push to mon immediately for alert), tracing */
    LOG.info("bracelet {}(BLE-ID) in emergency, detected by ap {}", apMsg.getBandId(), apMsg.getApAlias());
    apMsg.setBracelet(braceletId);
    String alertMsg = String.format("%s (%s) 求救 ", patient, apMsg.getBandId());
    apMsg.setMessage(alertMsg);
    String broadcast = JSON.toJSONString(apMsg);
    pushService.alert(new PushMsg(BROADCAST, RESCUE_TOPIC, broadcast));
  }

}
