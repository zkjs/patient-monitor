package com.fintech.hospital.push.consumer.yunba;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * @author baoqiang
 */
@Component("yunbaConsumer4C")
@Scope(SCOPE_SINGLETON)
public class YunbaConsumer4C extends YunbaConsumer{


  YunbaConsumer4C(@Value("${yunba.server.url}") String yunbaServerUrl,
                  @Value("${yunba.appkey.c}") String yunbaAppKey) {
    super(yunbaServerUrl, yunbaAppKey);
  }

  @Override
  public void consume(String msg) {
    LOG.debug("consuming message from client ");
  }


}
