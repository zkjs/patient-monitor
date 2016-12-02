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
public class MqttUrgencyConsumer extends MQTTIO {

  protected MqttUrgencyConsumer(@Value("${mqtt.local.uri}") String uri) {
    super(uri);
    try {
      //TODO options configuration
      client.connect(options);
    } catch (MqttException e) {
      LOG.error("mqtt {} connection failed: {}", uri, e);
    }
    LOG.info("mqtt {} connected", uri);
  }

  @Value("${mqtt.topic.rescue}")
  private String RESCUE;

  @Value("${mqtt.topic.drop}")
  private String DROP;

  @Autowired
  @Qualifier("APConsumer")
  private PushConsumer consumer;

  @PostConstruct
  @Override
  protected void init() {
    subscribe(RESCUE);
    subscribe(DROP);
  }

  @Override
  protected void subscribe(String topic) {
    try {
      client.subscribe(topic, (t, m) -> {
        LOG.info("consumer {}: {}", consumer, consumer.toString());
        consumer.consume(m.toString());
      });
    } catch (MqttException e) {
      LOG.error("subscription on {} failed: {}", topic, e);
    }
  }

  @Override
  protected void publish(String topic, String msg) {
    throw new UnsupportedOperationException("consumer does no publishing jobs");
  }

}
