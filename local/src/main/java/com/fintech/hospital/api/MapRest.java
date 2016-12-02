package com.fintech.hospital.api;

import com.fintech.hospital.data.MongoDB;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.apache.commons.lang3.StringUtils.isBlank;

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
    if(isBlank(part) || part.length() != 24)
      return "{'status': 'err', 'error': 'object not found'}";

    return ImmutableMap.of(
       "list",  mongo.mapParts(part)
    );
  }


}
