package com.fintech.hospital.push.consumer;

import com.alibaba.fastjson.JSON;
import com.fintech.hospital.data.MongoDB;
import com.fintech.hospital.push.PushConsumer;
import com.fintech.hospital.domain.APStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @author baoqiang
 */
@Service("APStatConsumer")
@Scope(SCOPE_PROTOTYPE)
public class APStatusConsumer implements PushConsumer {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private MongoDB mongo;

  @Override
  public void consume(String msg) {
    LOG.debug("consuming ap heartbeat... {}", msg);
    APStatus apStatus = JSON.parseObject(msg, APStatus.class);
    LOG.info("ap {} alive", apStatus.getBssid());
    mongo.updateAP(apStatus);
  }

}
