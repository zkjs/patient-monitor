package com.fintech.hospital.api;

import com.alibaba.fastjson.JSON;
import com.fintech.hospital.data.MongoDB;
import com.fintech.hospital.domain.AP;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author baoqiang
 */
@RestController
@RequestMapping("/ap")
public class APRest {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());


  @Autowired
  private MongoDB mongo;

  @GetMapping
  public Object apList() {
    return ImmutableMap.of(
        "list", mongo.apList()
    );
  }

  @PutMapping
  public Object updateAP(
      HttpServletRequest req
  ) {
    try {
      String s = IOUtils.toString(req.getInputStream());
      AP ap = JSON.parseObject(s, AP.class);
      if (ap.reqValid()) {
        boolean updated = mongo.updateAP(ap);
        if (updated) return new Object();
      }
    } catch (IOException e) {
      LOG.warn("failed to parse request ap");
    }

    return "{\"status\": \"err\", \"error\": \"only cameras can be triggered\"}";
  }

//  public static void main(String[] args) {
//    String a = "{\"apid\": \"581b1a6542aa101eebc77e61\"}";
//    String b = "{\"camera\":\"1\",\"apid\":\"585b93bfa964c77d3f75a0c5\",\"triggers\":[\"585b93bfa964c77d3f75a0c5\",\"5860c22ce2efbcf6d5e48b4e\"],\"triggerLogic\":\"and\"}";
//    AP ap = JSON.parseObject(b, AP.class);
//    System.out.println(ap);
//  }

}
