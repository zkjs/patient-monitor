package com.fintech.hospital.base;

import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * @author baoqiang
 */
@Configuration
public class AppConfig {

  /**
   * mqtt message store
   */
  @Bean(name = "mqttDB")
  MongoClient mapDBSource(@Value("${mongo.mqtt.host}") String host, @Value("${mongo.mqtt.port}") int port){
    return new MongoClient(host, port);
  }

  @Bean(name = "mqttMongoTemplate")
  MongoTemplate mapMongoTemplate(@Qualifier("mqttDB") MongoClient client, @Value("${mongo.mqtt.database}") String dbname){
    return new MongoTemplate(client, dbname);
  }

}
