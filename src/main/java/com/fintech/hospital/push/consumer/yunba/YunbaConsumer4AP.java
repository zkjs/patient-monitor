package com.fintech.hospital.push.consumer.yunba;

import com.alibaba.fastjson.JSON;
import com.fintech.hospital.ap.APMsg;
import com.fintech.hospital.push.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

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

  @Override
  public void consume(String msg) {
    LOG.debug("consuming ap msg... {}", msg);
    APMsg apMsg = JSON.parseObject(msg, APMsg.class);
    /* categorize msg type: urgency (push to mon immediately for alert), tracing */
    if(apMsg.urgent()){
      LOG.info("band {} in emergency, detected by ap {}", apMsg.bandId(), apMsg.getApid());
      //TODO push to mon for urgent band id
    }
    /**
     * get rssi and cache
     * get the last rssi for the device
     * calculate distance to ap
     * when detected ap reaches >= 3, try positioning the device
     */

  }

  @Override
  public void onConnAck(Object json) throws Exception {
    LOG.info("yunba for ap {} connected {}", current, json);
    alias("alias2");
  }

}
