package com.fintech.hospital.push.mqtt;

import org.bson.types.ObjectId;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author baoqiang
 */
public abstract class MQTTIO implements MqttCallback{

  protected final Logger LOG;

  protected MqttClient client;
  protected MqttConnectOptions options = new MqttConnectOptions();

  protected MQTTIO(String uri, String clientId, String user, String pass){
    LOG = LoggerFactory.getLogger(this.getClass());
    try {
      client = new MqttClient(uri, clientId);
      client.setCallback(this);
      options.setCleanSession(true);
      options.setAutomaticReconnect(true);
      options.setConnectionTimeout(30);
    } catch (MqttException e) {
      LOG.error("failed to connect to broker {}: {}", uri, e);
    }
  }

  protected MQTTIO(String uri){
    this(uri, new ObjectId().toHexString(), null, null);
  }

  protected abstract void subscribe(String topic);

  protected abstract void publish(String topic, String msg);

  @Override
  public void connectionLost(Throwable cause) {
    LOG.warn("mqtt connection lost: {}", cause);
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    LOG.trace("mqtt msg on [{}]: {}", topic, message);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    LOG.trace("mqtt msg deliverred {}", token);
  }

}
