package com.fintech.hospital.api;

import com.alibaba.fastjson.JSONObject;
import com.fintech.hospital.data.MongoDB;
import com.fintech.hospital.domain.BraceletPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author baoqiang
 */
@RestController
@RequestMapping("/track")
public class BraceletTrackRest {

  @Autowired
  private MongoDB mongo;

  @GetMapping("{bid}")
  public Object tracks(@PathVariable("bid") String bracelet) {
    BraceletPosition position = mongo.getBraceletTrack(bracelet);
    JSONObject result = new JSONObject();
    result.put("status", "ok");
    result.put("data", position.getPosition());
    return result;
  }
}


