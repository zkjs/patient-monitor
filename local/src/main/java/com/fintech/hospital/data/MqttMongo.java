package com.fintech.hospital.data;

import com.mongodb.BasicDBObject;
import org.eclipse.paho.client.mqttv3.MqttPersistable;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author baoqiang
 *
 * use a mongo to store mqtt messages for QoS
 */
@Component
public class MqttMongo {

  @Autowired
  @Qualifier("mqttMongoTemplate")
  private MongoTemplate mongo;

  public synchronized void create(String name) {
    if (!mongo.collectionExists(name)) {
      CollectionOptions options = new CollectionOptions(102400, 100, true);
      mongo.createCollection(name, options);
    }
  }

  public void close(String name) {
    mongo.findAllAndRemove(new Query(), name);
  }

  public void add(MqttData data, String name) {
    mongo.insert(data, name);
  }

  public MqttData get(String key, String name) {
    return mongo.findOne(
        new Query(Criteria.where("_id").is(key)),
        MqttData.class,
        name
    );
  }

  public void del(String key, String name) {
    mongo.remove(
        new Query(Criteria.where("_id").is(key)),
        name
    );
  }


  private final AggregationOperation GROUP_BY_KEY = c -> c.getMappedObject(
      new BasicDBObject("$group", new BasicDBObject("_id", "$_id"))
  );

  public List<String> keys(String name) {
    if(mongo.collectionExists(name)) {
      return mongo.aggregate(Aggregation.newAggregation(GROUP_BY_KEY), name, String.class).getMappedResults();
    }
    return new ArrayList<>(0);
  }

  public void clear(String name) {
    //mongo.dropCollection(name);
  }

  public boolean exists(String key, String name) {
    return mongo.exists(
        new Query(Criteria.where("_id").is(key)),
        name
    );
  }

  public static class MqttData implements MqttPersistable {
    private byte[] headerBytes;
    private byte[] payloadBytes;
    private int headerLength;
    private int payloadLength;

    @Id
    @Field("_id")
    private String key;

    public MqttData() {
    }

    public MqttData(byte[] headerBytes, int headerLength, byte[] payloadBytes, int payloadLength) {
      this.headerBytes = headerBytes;
      this.headerLength = headerLength;
      this.payloadBytes = payloadBytes;
      this.payloadLength = payloadLength;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public byte[] getHeaderBytes() {
      return headerBytes;
    }

    public void setHeaderBytes(byte[] headerBytes) {
      this.headerBytes = headerBytes;
    }

    public byte[] getPayloadBytes() {
      return payloadBytes;
    }

    public void setPayloadBytes(byte[] payloadBytes) {
      this.payloadBytes = payloadBytes;
    }

    public int getHeaderLength() {
      return headerLength;
    }

    @Override
    public int getHeaderOffset() throws MqttPersistenceException {
      return 0;
    }

    public void setHeaderLength(int headerLength) {
      this.headerLength = headerLength;
    }

    public int getPayloadLength() {
      return payloadLength;
    }

    @Override
    public int getPayloadOffset() throws MqttPersistenceException {
      return 0;
    }

    public void setPayloadLength(int payloadLength) {
      this.payloadLength = payloadLength;
    }
  }
}
