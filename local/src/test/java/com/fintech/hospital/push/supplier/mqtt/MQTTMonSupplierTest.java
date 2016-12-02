package com.fintech.hospital.push.supplier.mqtt;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author baoqinag
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class MQTTMonSupplierTest {

  @Autowired
  private MQTTMonSupplier supplier;

  @Value("${mqtt.topic.rescue}")
  private String RESCUE;


  @Test
  public void publish() throws Exception {
    supplier.publish(RESCUE, "{\"apid\": \"ap110\", \"floor\": 2, \"address\": \"West 208\", \"alert\": \"y\", \"message\": \"someone's calling for help\", \"payload\": \"126683000000\", \"bracelet\": \"581b1a6542aa101eebc77e60\", \"position\": { \"ap\": \"nearest ap addr\", \"floor\": 2, \"timestamp\": 1478159590006, \"radius\": 23.121, \"gps\": {\"lng\" : 104.061346, \"lat\" : 30.641574} }");
  }

}