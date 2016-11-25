package com.fintech.hospital.push.consumer;

import com.alibaba.fastjson.JSON;
import com.fintech.hospital.domain.APMsg;
import com.fintech.hospital.push.PushConsumer;
import com.fintech.hospital.push.PushService;
import com.fintech.hospital.push.model.PushMsg;
import com.fintech.hospital.push.supplier.yunba.YunbaOpts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.fintech.hospital.push.model.PushType.BROADCAST;

/**
 * @author baoqiang
 */
@Service("IOSAPConsumer")
public class IOSAPConsumer implements PushConsumer {

  private Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired
  private PushService pushService;

  @Value("${yunba.rescue.topic}")
  private String RESCUE_TOPIC;

  @Override
  public void consume(String msg) {
    LOG.debug("consuming ios ap msg... {}", msg);
    APMsg apMsg = JSON.parseObject(msg, APMsg.class);

    if (apMsg.urgent()) {
      LOG.info("bracelet {}(BLE-ID) in emergency, detected by ap {}", apMsg.getBracelet(), apMsg.getApid());
      String alertMsg = String.format("%s (%s) 求救 ", "张三", apMsg.getBracelet());
      apMsg.setMessage(alertMsg);
      String broadcast = JSON.toJSONString(apMsg);
      pushService.push2Mon(new PushMsg(BROADCAST, RESCUE_TOPIC, broadcast, new YunbaOpts(new YunbaOpts.YunbaAps(
          broadcast, alertMsg
      ))));
    }
  }

}
