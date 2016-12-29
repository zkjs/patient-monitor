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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static java.net.NetworkInterface.getNetworkInterfaces;

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
  @PostConstruct
  private void multicastMqttServerAddr() {
    try {
      List<InetAddress> addresses = new ArrayList<>();
      Enumeration<NetworkInterface> interfaces = getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        Enumeration<InetAddress> interfaceInets = interfaces.nextElement().getInetAddresses();
        while (interfaceInets.hasMoreElements()) {
          addresses.add(interfaceInets.nextElement());
        }
      }
      addresses.stream().filter(a->
          a instanceof Inet4Address
          && (a.isLinkLocalAddress() || a.isSiteLocalAddress() || a.isLoopbackAddress())
      ).forEach(address -> {
        try {
          jmDNS = JmDNS.create(address);
          ServiceInfo mqttSvr = ServiceInfo.create("_mqtt", "fintech mqtt", MQTT_PORT, "");
          ServiceInfo httpSvr = ServiceInfo.create("_http", "fintech http", HTTP_PORT, "");
          jmDNS.registerService(mqttSvr);
          LOG.info("registered mqtt service on {}:{}", address, MQTT_PORT);
          jmDNS.registerService(httpSvr);
          LOG.info("registered http service on {}:{}", address, HTTP_PORT);
        } catch (Exception e) {
          LOG.warn("failed to register service on {}: ", address, e);
        }
      });

    } catch (Exception e) {
      LOG.warn("failed to broadcast mqtt/http service to local LAN:", e);
    }
  }

  @PreDestroy
  private void unregistermDNSSvrs() {
    LOG.trace("trying to unregister mdns services");
    if (jmDNS != null) {
      LOG.info("un-register multicast dns services...");
      jmDNS.unregisterAllServices();
    }
  }

}
