package com.fintech.hospital.push.consumer;

import com.alibaba.fastjson.JSONArray;
import com.fintech.hospital.push.PushConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import static com.alibaba.fastjson.JSON.parseArray;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @author baoqiang
 */
@Service("PosListConsumer")
@Scope(SCOPE_PROTOTYPE)
public class PositionListConsumer implements PushConsumer {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Autowired
  @Qualifier("PositionConsumer")
  private PositionConsumer consumer;

  @Override
  public void consume(String msg) {
    LOG.info("consuming ap list msg... {}", msg);
    try {
      JSONArray dataList = parseArray(msg);
      dataList.forEach(data -> consumer.consume(data.toString()));
    } catch (Exception e) {
      LOG.error("check msg format {}: {}", msg, e);
    }
  }

}
