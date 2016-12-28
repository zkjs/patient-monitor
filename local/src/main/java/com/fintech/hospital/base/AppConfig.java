package com.fintech.hospital.base;

import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.net.InetAddress;

/**
 * @author baoqiang
 */
@Configuration
public class AppConfig {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

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

  private JmDNS jmDNS;

  @Value("${svr.mqtt.port}")
  private int MQTT_PORT;

  @Value("${server.port}")
  private int HTTP_PORT;

  /**
   * suppose that the mqtt server is on the same machine
   */
//  @PostConstruct
  private void multicastMqttServerAddr() {
    try {
      jmDNS = JmDNS.create(InetAddress.getLocalHost());
      ServiceInfo mqttSvr = ServiceInfo.create("_mqtt", "fintech mqtt", MQTT_PORT, "");
      ServiceInfo httpSvr = ServiceInfo.create("_http", "fintech http", HTTP_PORT, "");
      jmDNS.registerService(mqttSvr);
      jmDNS.registerService(httpSvr);
      LOG.info("registered mqtt service on current host, port 1883");
    } catch (Exception e) {
      LOG.warn("failed to broadcast mqtt service to local LAN:", e);
    }
  }

//  @PreDestroy
  private void unregistermDNSSvrs() {
    LOG.trace("trying to unregister mdns services");
    if (jmDNS != null) {
      LOG.info("un-register multicast dns services...");
      jmDNS.unregisterAllServices();
    }
  }

}
