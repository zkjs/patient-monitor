package com.fintech.hospital.api;

import com.fintech.hospital.data.MongoDB;
import com.fintech.hospital.domain.AP;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author baoqiang
 */
@RestController
@RequestMapping("/ap")
public class APRest {

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
      @RequestBody AP ap
  ) {
    if(ap.reqValid()) {
      boolean updated = mongo.updateAP(ap);
      if(updated) return new Object();
    }
    return "{\"status\": \"err\", \"error\": \"only cameras can be triggered\"}";
  }

}
