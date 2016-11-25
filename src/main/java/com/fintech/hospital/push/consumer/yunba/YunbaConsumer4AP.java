package com.fintech.hospital.push.consumer.yunba;

import com.fintech.hospital.push.PushConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * @author baoqiang
 */
//@Service("yunbaConsumer4AP")
//@Scope(SCOPE_SINGLETON)
public class YunbaConsumer4AP extends YunbaConsumer {


  YunbaConsumer4AP(@Value("${yunba.server.url}") String yunbaServerUrl,
                   @Value("${yunba.appkey.ap}") String yunbaAppKey) {
    super(yunbaServerUrl, yunbaAppKey);
  }


  @Value("${yunba.trace.alias}")
  private String TRACE_ALIAS;

  @Value("${yunba.rescue.topic}")
  private String RESCUE_TOPIC;

  @Autowired
  @Qualifier("APConsumer")
  private PushConsumer consumer;

  @Override
  public void consume(String msg) {
    consumer.consume(msg);
  }

  @Override
  public void onConnAck(Object json) throws Exception {
    LOG.info("yunba for ap {} connected {}", current, json);
    alias(TRACE_ALIAS);
    subscribe(RESCUE_TOPIC);
  }

}
