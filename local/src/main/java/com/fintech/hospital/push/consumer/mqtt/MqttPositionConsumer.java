package com.fintech.hospital.push.consumer.mqtt;

import com.fintech.hospital.push.PushConsumer;
import com.fintech.hospital.push.mqtt.MQTTIO;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author baoqiang
 */
//@Component
public class MqttPositionConsumer extends MQTTIO {

  protected MqttPositionConsumer(@Value("${mqtt.local.uri}") String uri) {
    super(uri);
    try {
      //TODO options configuration
      client.connect(options);
    } catch (MqttException e) {
      LOG.error("mqtt {} connection failed: {}", uri, e);
    }
    LOG.info("mqtt {} connected", uri);
  }

  @Value("${mqtt.topic.trace}")
  private String TRACE;

  @Autowired
  @Qualifier("APListConsumer")
  private PushConsumer consumer;

  @PostConstruct
  @Override
  protected void init() {
    subscribe(TRACE);
  }

  @Override
  protected void subscribe(String topic) {
    try {
      client.subscribe(topic, (t, m) -> consumer.consume(m.toString()));
    } catch (MqttException e) {
      LOG.error("subscription on {} failed: {}", topic, e);
    }
  }

  @Override
  protected void publish(String topic, String msg) {
    throw new UnsupportedOperationException("consumer does no publishing jobs");
  }

}
