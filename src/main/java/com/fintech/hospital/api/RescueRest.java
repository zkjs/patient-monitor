package com.fintech.hospital.api;

import com.fintech.hospital.push.PushService;
import com.fintech.hospital.push.model.PushMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fintech.hospital.push.model.PushType.ALIAS;

/**
 * @author baoqiang
 */
@RestController
@RequestMapping("/rescue")
public class RescueRest {

  final Logger LOG = LoggerFactory.getLogger(RescueRest.class);

  @Autowired
  private PushService pushService;

  @PutMapping
  public Object respond(@RequestBody RescueRespond respond) {

    pushService.push2AP(new PushMsg(ALIAS, respond.apid, "医护人员马上赶到", null));

    LOG.info(" doctor {} responded to rescue on ap {}", respond.response, respond.apid);
    return new Object();
  }

  public static class RescueRespond {
    public String apid;
    public String response;
  }

}
