package com.fintech.hospital.push.supplier.mqtt;

import com.fintech.hospital.push.PushSupplier;
import com.fintech.hospital.push.model.PushMsg;
import com.fintech.hospital.push.mqtt.MQTTIO;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author baoqiang
 */
@Component("mqttMonSupplier")
public class MQTTMonSupplier extends MQTTIO implements PushSupplier{

  protected MQTTMonSupplier(@Value("${mqtt.remote.uri}") String uri) {
    super(uri);
    try {
      //TODO options configuration
      client.connect(options);
    } catch (MqttException e) {
      LOG.error("mqtt {} connection failed: {}", uri, e);
    }
    LOG.info("mqtt {} connected", uri);
  }

  @Value("${yunba.trace.alias}")
  private String TRACE;

  @Override
  protected void subscribe(String topic) {
    throw new UnsupportedOperationException("publisher does no subscribing jobs");
  }

  @Override
  protected void publish(String topic, String msg) {
    try {
      client.publish(topic, new MqttMessage(msg.getBytes(UTF_8)));
    } catch (MqttException e) {
      LOG.error("publishing on {} failed: {}", e);
    }
  }

  @Override
  public void publish(PushMsg pushMsg) {
    publish(pushMsg.getSubject(), pushMsg.getMessage());
  }

  @Override
  public List<String> publishBatch(List<String> targets, PushMsg pushMsg) {
    throw new UnsupportedOperationException("no such requirements yet");
  }
}
