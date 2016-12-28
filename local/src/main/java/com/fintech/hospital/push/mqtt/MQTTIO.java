package com.fintech.hospital.push.mqtt;

import com.alibaba.fastjson.JSON;
import org.bson.types.ObjectId;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author baoqiang
 */
public abstract class MQTTIO implements MqttCallback{

  protected final Logger LOG;

  protected MqttCustomClient client;
  protected MqttConnectOptions options = new MqttConnectOptions();

  @Autowired
  private MQTTDataStore dataStore;

  protected MQTTIO(String uri, String clientId, String user, String pass){
    LOG = LoggerFactory.getLogger(this.getClass());
    try {
      client = new MqttCustomClient(uri, clientId, dataStore);
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

  protected void init(){
    LOG.info("empty init in mqtt client");
  }

  protected abstract void subscribe(String topic);

  protected abstract void publish(String topic, String msg);

  @Override
  public void connectionLost(Throwable cause) {
    LOG.warn("mqtt connection lost: {}", cause);
    try{
      client = new MqttCustomClient(client.getServerURI(), client.getClientId(), dataStore);
      client.setCallback(this);
      client.connect(options, new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
          LOG.info("reconnected to mqtt server {}", client.getServerURI() );
          init();
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
          LOG.warn("failed to reconnect");
        }
      });
    }catch (Exception e){
      LOG.error("failed to reconnect mqtt server", e);
    }
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {
    LOG.trace("mqtt msg on [{}]: {}", topic, message);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    LOG.trace("mqtt msg deliverred {}", token.getMessageId());
  }


  protected static class MqttCustomClient extends MqttClient {

    private MqttCustomClient(String serverURI, String clientId, MqttClientPersistence dataStore) throws MqttException {
      super(serverURI, clientId, dataStore);
    }

    void connect(MqttConnectOptions options, IMqttActionListener listener) throws MqttSecurityException, MqttException {
      aClient.connect(options, null, listener).waitForCompletion(getTimeToWait());
    }


  }
}
