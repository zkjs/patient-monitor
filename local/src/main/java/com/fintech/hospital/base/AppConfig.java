package com.fintech.hospital.base;

import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

/**
 * @author baoqiang
 */
@Configuration
public class AppConfig {

  /**
   * mqtt message store
   */
  @Bean(name = "mqttDB")
  MongoClient mqttDBSource(@Value("${mongo.mqtt.host}") String host, @Value("${mongo.mqtt.port}") int port) {
    return new MongoClient(host, port);
  }

  @Bean(name = "mqttMongoTemplate")
  MongoTemplate mqttMongoTemplate(@Qualifier("mqttDB") MongoClient client, @Value("${mongo.mqtt.database}") String dbname) {
    return new MongoTemplate(client, dbname);
  }

  /**
   * mqtt message store
   */
  @Bean(name = "mapDB")
  @Primary
  MongoClient mapDBSource(@Value("${mongo.map.host}") String host, @Value("${mongo.map.port}") int port) {
    return new MongoClient(host, port);
  }

  @Bean(name = "mapTemplate")
  @Primary
  MongoTemplate mapMongoTemplate(@Qualifier("mapDB") MongoClient client, @Value("${mongo.map.database}") String dbname) {
    return new MongoTemplate(client, dbname);
  }

}
