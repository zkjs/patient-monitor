package com.fintech.hospital.push.consumer.yunba;

import com.alibaba.fastjson.JSON;
import com.fintech.hospital.domain.APMsg;
import com.fintech.hospital.push.PushService;
import com.fintech.hospital.push.model.PushMsg;
import com.fintech.hospital.push.supplier.yunba.YunbaOpts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static com.fintech.hospital.push.model.PushType.BROADCAST;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * @author baoqiang
 */
@Component("yunbaConsumer4APIOS")
@Scope(SCOPE_SINGLETON)
public class YunbaConsumer4APIOS extends YunbaConsumer {


  YunbaConsumer4APIOS(@Value("${yunba.server.url}") String yunbaServerUrl,
                      @Value("${yunba.appkey.ap.ios}") String yunbaAppKey) {
    super(yunbaServerUrl, yunbaAppKey);
  }

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

  @Override
  public void onConnAck(Object json) throws Exception {
    LOG.info("yunba for ap {} connected {}", current, json);
    subscribe(RESCUE_TOPIC);
  }



}
