package com.fintech.hospital.push.mqtt;

import com.fintech.hospital.data.MqttMongo;
import com.fintech.hospital.data.MqttMongo.MqttData;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttPersistable;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * @author baoqiang
 */
@Component
public class MQTTDataStore implements MqttClientPersistence {

  private Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private MqttMongo mongo;

  private String uri;

  private static MqttData toObject(MqttPersistable persistable) throws MqttPersistenceException {
    return new MqttData(persistable.getHeaderBytes(), persistable.getHeaderLength(),
        persistable.getPayloadBytes(), persistable.getPayloadLength());
  }

  @Override
  public void open(String clientId, String serverURI) throws MqttPersistenceException {
    this.uri = clientId + "." + serverURI;
    mongo.create(this.uri);
  }

  @Override
  public void close() throws MqttPersistenceException {
    mongo.close(this.uri);
    LOG.info("closed mqtt data store");
  }

  @Override
  public void put(String key, MqttPersistable persistable) throws MqttPersistenceException {
    MqttData data = toObject(persistable);
    data.setKey(key);
    mongo.add(data, this.uri);
  }

  @Override
  public MqttPersistable get(String key) throws MqttPersistenceException {
    return mongo.get(key, this.uri);
  }

  @Override
  public void remove(String key) throws MqttPersistenceException {
    mongo.del(key, this.uri);
  }

  @Override
  public Enumeration keys() throws MqttPersistenceException {
    List<String> keys = mongo.keys(this.uri);
    return new Enumeration() {
      private Iterator<String> iterator = keys.iterator();

      @Override
      public boolean hasMoreElements() {
        return iterator.hasNext();
      }

      @Override
      public Object nextElement() {
        return iterator.next();
      }
    };
  }

  @Override
  public void clear() throws MqttPersistenceException {
    mongo.clear(this.uri);
  }

  @Override
  public boolean containsKey(String key) throws MqttPersistenceException {
    return mongo.exists(key, this.uri);
  }
}
