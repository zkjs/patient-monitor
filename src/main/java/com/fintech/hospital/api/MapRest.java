package com.fintech.hospital.api;

import com.fintech.hospital.data.MongoDB;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author baoqiang
 */
@RestController
@RequestMapping("/map")
public class MapRest {

  @Autowired
  private MongoDB mongo;

  @GetMapping
  public Object objects(){
    return ImmutableMap.of(
        "list", mongo.mapObjs()
    );
  }

  @GetMapping("/{part}")
  public Object layers(
      @PathVariable String part
  ){
    return ImmutableMap.of(
       "list",  mongo.mapParts(part)
    );
  }


}
